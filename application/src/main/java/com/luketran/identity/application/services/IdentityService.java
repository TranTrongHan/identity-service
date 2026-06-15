package com.luketran.identity.application.services;

import com.luketran.identity.application.dto.request.LoginPasswordRequest;
import com.luketran.identity.application.dto.request.RefreshTokenRequest;
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
import com.luketran.identity.domain.exceptions.SessionInvalidException;
import com.luketran.identity.domain.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    private final AccountLogoutRepository accountLogoutRepository;


    @Override
    public TokenDataResponse loginByPassword(LoginPasswordRequest request) {
        // 1. Tìm App
        App app = appRepository.findByCode(request.getAppCode())
                .orElseThrow(() -> new ResourceNotFoundException("App not found"));

        // 2. Tìm AccountAuth (username → account)
        AccountAuth accountAuth = accountAuthRepository.findByFieldTypeAndFieldValue(AuthFieldType.USERNAME, request.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        // 3. Load Account
        Account account = accountRepository.findById(accountAuth.getAccountId())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        // 4. Check brute force lock
        if (account.getAccessDeniedUntil() != null && account.getAccessDeniedUntil().isAfter(LocalDateTime.now())) {
            throw new BruteForceException(account.getAccessDeniedUntil());
        }

        // 5. Verify password
        if (!PasswordHelper.verifyPassword(request.getPassword(), account.getSecretKey(), account.getPassword())) {
            onWrongLogin(account);
        }
        onSuccessLogin(account);

        // Clear force-logout flag nếu có (khi user login lại thành công)
        accountLogoutRepository.deleteByAccountId(account.getId());

        // 6. Create/extend session (refresh token)
        AccountSession session = accountSessionService.createOrExtend(account.getId(), app.getId());

        // 7. Generate access token
        String accessToken = generateAccessToken(account, app);

        // 8. Return response
        TokenDataResponse response = new TokenDataResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(session.getId().toString());
        return response;
    }

    /**
     * @param input
     * @return
     */
    @Override
    public TokenDataResponse refreshToken(RefreshTokenRequest input) {
        UUID sessionId;
        try {
            sessionId = UUID.fromString(input.getRefreshToken());
        } catch (IllegalArgumentException e) {
            throw new SessionInvalidException();
        }

        AccountSession session;
        try {
            session = accountSessionService.getById(sessionId);
        } catch (ResourceNotFoundException e) {
            throw new SessionInvalidException();
        }

        if (session == null || LocalDateTime.now().isAfter(session.getExpiredAt())) {
            throw new SessionInvalidException();
        }

        // Load Account & App to generate access token
        Account account = accountRepository.findById(session.getAccountId())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));
        App app = appRepository.findById(session.getAppId())
                .orElseThrow(() -> new ResourceNotFoundException("App not found"));

        // Extend session
        session = accountSessionService.extend(session.getId());

        // Generate access token
        String accessToken = generateAccessToken(account, app);

        TokenDataResponse tokenDataResponse = new TokenDataResponse();
        tokenDataResponse.setRefreshToken(session.getId().toString());
        tokenDataResponse.setAccessToken(accessToken);
        return tokenDataResponse;
    }

    private String generateAccessToken(Account account, App app) {
        // Load AppAccess (account ↔ app relationship + role + scope)
        AppAccess access = appAccessRepository.findByAccountIdAndAppId(account.getId(), app.getId())
                .orElseThrow(() -> new AuthenticationException("No access to this app"));

        // Resolve role + effective scope
        AppRole role = null;
        Set<String> rolePermissionCodes = new HashSet<>();

        // Load all permissions for this app (dùng cho scope resolution + mapping IDs → codes)
        List<AppPermission> allPermissions = appPermissionRepository.findAllActiveByAppId(app.getId());

        if (access.getRoleId() != null) {
            // Load AppRole entity (cần code + name cho JWT claims: roleCode, roleName)
            role = appRoleRepository.findById(access.getRoleId()).orElse(null);

            // Load role's permission IDs
            List<AppRolePermission> rolePermissions = appRolePermissionRepository.findAllByRoleId(access.getRoleId());
            Set<UUID> permissionIds = new HashSet<>();
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

        return tokenService.generateAccessToken(account, app, role, effectiveScope);
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

        if (accessDeniedUntil != null) {
            throw new BruteForceException(accessDeniedUntil);
        } else {
            throw new AuthenticationException("Invalid credentials");
        }
    }

    private void onSuccessLogin(Account account) {
        account.setWrongLoginCount(0);
        account.setAccessDeniedUntil(null);
        accountRepository.save(account);
    }
}
