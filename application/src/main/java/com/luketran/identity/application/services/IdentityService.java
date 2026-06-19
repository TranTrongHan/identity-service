package com.luketran.identity.application.services;

import com.luketran.identity.application.dto.request.*;
import com.luketran.identity.application.dto.response.GoogleAuthUrlResponse;
import com.luketran.identity.application.dto.response.ResetPasswordDataResponse;
import com.luketran.identity.application.dto.response.TokenDataResponse;
import com.luketran.identity.application.helpers.PasswordHelper;
import com.luketran.identity.application.helpers.RandomHelper;
import com.luketran.identity.application.helpers.ScopeHelper;
import com.luketran.identity.application.interfaces.AccountSessionService;
import com.luketran.identity.application.interfaces.GoogleOAuthClient;
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
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

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
    private final AppGoogleStateRepository appGoogleStateRepository;
    private final ResetPasswordRequestRepository resetPasswordRequestRepository;
    private final GoogleOAuthClient googleOAuthClient;


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

    // ==================== API #33: Google Auth URL ====================

    @Override
    public GoogleAuthUrlResponse getGoogleAuthUrl(GoogleAuthRequest request) {
        App app = appRepository.findByCode(request.getAppCode())
                .orElseThrow(() -> new ResourceNotFoundException("App not found"));

        if (app.getGoogleClientId() == null || app.getGoogleClientId().isBlank()) {
            throw new AuthenticationException("App chưa cấu hình Google OAuth");
        }

        // Tạo state random để chống CSRF
        UUID state = UUID.randomUUID();

        // Lưu state vào DB (expire 10 phút)
        AppGoogleState googleState = new AppGoogleState();
        googleState.setAppId(app.getId());
        googleState.setState(state);
        googleState.setRedirectUri(request.getRedirectUri());
        googleState.setExpiredAt(LocalDateTime.now().plusMinutes(10));
        appGoogleStateRepository.save(googleState);

        // Build Google OAuth URL
        String url = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + URLEncoder.encode(app.getGoogleClientId(), StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(request.getRedirectUri(), StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode("openid email profile", StandardCharsets.UTF_8)
                + "&state=" + state.toString()
                + "&access_type=offline"
                + "&prompt=consent";

        GoogleAuthUrlResponse response = new GoogleAuthUrlResponse();
        response.setUrl(url);
        return response;
    }

    // ==================== API #30: Google Login WithAccess ====================

    @Override
    @Transactional
    public TokenDataResponse loginByGoogleWithAccess(GoogleLoginWithAccessRequest request) {
        // 1. Tìm App
        App app = appRepository.findByCode(request.getAppCode())
                .orElseThrow(() -> new ResourceNotFoundException("App not found"));

        if (app.getGoogleClientId() == null || app.getGoogleClientSecret() == null) {
            throw new AuthenticationException("App chưa cấu hình Google OAuth");
        }

        // 2. Exchange code → user info
        GoogleOAuthClient.GoogleUserInfo userInfo = googleOAuthClient.exchangeCodeForUserInfo(
                app.getGoogleClientId(),
                app.getGoogleClientSecret(),
                request.getCode(),
                request.getRedirectUri()
        );

        // 3. Verify domain nếu có cấu hình
        if (app.getGoogleAllowedDomain() != null && !app.getGoogleAllowedDomain().isBlank()) {
            String emailDomain = userInfo.email().substring(userInfo.email().indexOf("@") + 1);
            if (!emailDomain.equalsIgnoreCase(app.getGoogleAllowedDomain())) {
                throw new AuthenticationException("Email domain không được phép: " + emailDomain);
            }
        }

        // 4. Tìm hoặc tạo Account
        Account account;
        Optional<AccountAuth> existingAuth = accountAuthRepository.findByFieldTypeAndFieldValue(
                AuthFieldType.EMAIL, userInfo.email());

        if (existingAuth.isPresent()) {
            account = accountRepository.findById(existingAuth.get().getAccountId())
                    .orElseThrow(() -> new AuthenticationException("Account not found"));
        } else {
            // Tạo account mới (không có password — chỉ login qua Google)
            account = new Account();
            account.setName(userInfo.name() != null ? userInfo.name() : userInfo.email());
            account.setAvatarUrl(userInfo.picture());
            account.setSecretKey(RandomHelper.generateSecretKey());
            account = accountRepository.save(account);

            // Tạo AccountAuth (EMAIL)
            AccountAuth auth = new AccountAuth();
            auth.setAccountId(account.getId());
            auth.setFieldType(AuthFieldType.EMAIL);
            auth.setFieldValue(userInfo.email());
            accountAuthRepository.save(auth);
        }

        // 5. Tìm hoặc tạo AppAccess
        Optional<AppAccess> existingAccess = appAccessRepository.findByAccountIdAndAppId(account.getId(), app.getId());
        if (existingAccess.isEmpty()) {
            AppAccess newAccess = new AppAccess();
            newAccess.setAccountId(account.getId());
            newAccess.setAppId(app.getId());
            newAccess.setScope(request.getScope() != null ? request.getScope() : "");

            // Resolve role nếu có
            if (request.getRoleCode() != null && !request.getRoleCode().isBlank()) {
                AppRole role = appRoleRepository.findByCodeAndAppId(request.getRoleCode(), app.getId())
                        .orElse(null);
                if (role != null) {
                    newAccess.setRoleId(role.getId());
                }
            }

            appAccessRepository.save(newAccess);
        }

        // 6. Clear force-logout flag
        accountLogoutRepository.deleteByAccountId(account.getId());

        // 7. Create session + generate token
        AccountSession session = accountSessionService.createOrExtend(account.getId(), app.getId());
        String accessToken = generateAccessToken(account, app);

        TokenDataResponse response = new TokenDataResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(session.getId().toString());
        return response;
    }

    // ==================== API #36: Create Reset Password Request ====================

    @Override
    @Transactional
    public ResetPasswordDataResponse createResetPasswordRequest(CreateResetPasswordRequest request) {
        App app = appRepository.findByCode(request.getAppCode())
                .orElseThrow(() -> new ResourceNotFoundException("App not found"));

        // Tìm account qua email
        AccountAuth accountAuth = accountAuthRepository.findByFieldTypeAndFieldValue(
                AuthFieldType.EMAIL, request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Account with this email not found"));

        // Tạo reset password request (expire 30 phút)
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setAppId(app.getId());
        resetRequest.setAccountId(accountAuth.getAccountId());
        resetRequest.setExpiredAt(LocalDateTime.now().plusMinutes(30));

        ResetPasswordRequest saved = resetPasswordRequestRepository.save(resetRequest);

        ResetPasswordDataResponse response = new ResetPasswordDataResponse();
        response.setId(saved.getId());
        return response;
    }

    // ==================== API #37: Execute Reset Password ====================

    @Override
    @Transactional
    public void executeResetPassword(ExecuteResetPasswordRequest request) {
        ResetPasswordRequest resetRequest = resetPasswordRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Reset password request not found"));

        // Check hết hạn
        if (LocalDateTime.now().isAfter(resetRequest.getExpiredAt())) {
            resetPasswordRequestRepository.deleteById(resetRequest.getId());
            throw new AuthenticationException("Reset password request đã hết hạn");
        }

        // Load account
        Account account = accountRepository.findById(resetRequest.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // Hash và set password mới
        account.setPassword(PasswordHelper.hashPassword(request.getNewPassword(), account.getSecretKey()));
        accountRepository.save(account);

        // Xóa request (one-time use)
        resetPasswordRequestRepository.deleteById(resetRequest.getId());
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
