package com.luketran.identity.application.services;

import com.luketran.identity.application.dto.request.LoginAppInputRequest;
import com.luketran.identity.application.dto.request.LoginPasswordInputRequest;
import com.luketran.identity.application.dto.response.TokenDataResponse;
import com.luketran.identity.application.helpers.PasswordHelper;
import com.luketran.identity.application.helpers.ScopeHelper;
import com.luketran.identity.application.interfaces.AccountSessionService;
import com.luketran.identity.application.interfaces.TokenService;
import com.luketran.identity.domain.entities.*;
import com.luketran.identity.domain.enums.AuthFieldType;
import com.luketran.identity.domain.enums.SettingCode;
import com.luketran.identity.domain.exceptions.AuthenticationException;
import com.luketran.identity.domain.exceptions.BruteForceException;
import com.luketran.identity.domain.exceptions.ResourceNotFoundException;
import com.luketran.identity.domain.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IdentityService implements com.luketran.identity.application.interfaces.IdentityService {
    private final AppRepository appRepository;
    private final AccountAuthRepository accountAuthRepository;
    private final AccountRepository accountRepository;
    private final SettingRepository settingRepository;
    private final AccountSessionService accountSessionService;
    private final AppAccessRepository appAccessRepository;
    private final AppRoleRepository appRoleRepository;
    private final AppRolePermissionRepository appRolePermissionRepository;
    private final AppPermissionRepository appPermissionRepository;
    private final TokenService tokenService;

    @Override
    public TokenDataResponse loginByPassword(LoginAppInputRequest appInputRequest, LoginPasswordInputRequest passwordInputRequest) {
        // 1. Tìm App
        App app = appRepository.findByCode(appInputRequest.getAppCode())
                .orElseThrow(() -> new ResourceNotFoundException("App not found"));

        // 2. Tìm AccountAuth (username → account)
        AccountAuth accountAuth = accountAuthRepository.findByFieldTypeAndFieldValue(AuthFieldType.USERNAME, passwordInputRequest.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        // 3. Load Account
        Account account = accountRepository.findById(accountAuth.getAccountId())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        // 4. Check brute force lock
        if (account.getAccessDeniedUntil() != null && account.getAccessDeniedUntil().isAfter(LocalDateTime.now())) {
            throw new BruteForceException(account.getAccessDeniedUntil());
        }

        // 5. Verify password
        if (!PasswordHelper.verifyPassword(passwordInputRequest.getPassword(), account.getSecretKey(), account.getPassword())) {
            onWrongLogin(account);
        }
        onSuccessLogin(account);

        // 6. Load AppAccess (account ↔ app relationship + role + scope)
        AppAccess access = appAccessRepository.findByAccountIdAndAppId(account.getId(), app.getId())
                .orElseThrow(() -> new AuthenticationException("No access to this app"));

        // 7. Resolve role + effective scope
        AppRole role = null;
        Set<String> rolePermissionCodes = new HashSet<>();

        // Load all permissions for this app (dùng cho scope resolution + mapping IDs → codes)
        List<AppPermission> allPermissions = appPermissionRepository.findAllActiveByAppId(app.getId());

        if (access.getRoleId() != null) {
            // Load AppRole entity (cần code + name cho JWT claims: roleCode, roleName)
            role = appRoleRepository.findById(access.getRoleId()).orElse(null);

            // Load role's permission IDs
            List<AppRolePermission> rolePermissions = appRolePermissionRepository.findAllByRoleId(access.getRoleId());
            Set<java.util.UUID> permissionIds = new HashSet<>();
            for (AppRolePermission rp : rolePermissions) {
                permissionIds.add(rp.getPermissionId());
            }

            // Map permissionIds → permission codes (bằng cách filter allPermissions)
            for (AppPermission perm : allPermissions) {
                if (permissionIds.contains(perm.getId())) {
                    rolePermissionCodes.add(perm.getCode());
                }
            }
        }

        // Resolve effective scope: merge role permissions + scope overrides + parent-child expansion
        String effectiveScope = ScopeHelper.resolveEffectiveScope(
                access.getScope(), rolePermissionCodes, allPermissions);

        // 8. Create/extend session (refresh token)
        AccountSession session = accountSessionService.createOrExtend(account.getId(), app.getId());

        // 9. Generate access token
        String accessToken = tokenService.generateAccessToken(account, app, role, effectiveScope);

        // 10. Return response
        TokenDataResponse response = new TokenDataResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(session.getId().toString());
        return response;
    }

    private void onWrongLogin(Account account) {
        int maxAllowed = settingRepository.getIntValueOrDefault(SettingCode.MAX_WRONG_LOGIN_ALLOWED);
        int waitMinute = settingRepository.getIntValueOrDefault(SettingCode.WAIT_MINUTE_PER_WRONG_LOGIN);

        int wrongCount = account.getWrongLoginCount() + 1;
        LocalDateTime accessDeniedUntil = null;

        if (wrongCount >= maxAllowed && waitMinute > 0) {
            int lockMinutes = (wrongCount - maxAllowed + 1) * waitMinute;
            accessDeniedUntil = LocalDateTime.now().plusMinutes(lockMinutes);
        }

        account.setWrongLoginCount(wrongCount);
        account.setAccessDeniedUntil(accessDeniedUntil);
        accountRepository.save(account);

        throw new BruteForceException(accessDeniedUntil);
    }

    private void onSuccessLogin(Account account) {
        account.setWrongLoginCount(0);
        account.setAccessDeniedUntil(null);
        accountRepository.save(account);
    }
}
