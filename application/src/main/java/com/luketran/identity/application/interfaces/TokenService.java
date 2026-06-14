package com.luketran.identity.application.interfaces;

import com.luketran.identity.domain.entities.Account;
import com.luketran.identity.domain.entities.App;
import com.luketran.identity.domain.entities.AppRole;

/**
 * Service sinh và validate JWT access token.
 * Interface ở application layer, implementation ở infrastructure (dùng JJWT library).
 */
public interface TokenService {

    /**
     * Sinh JWT access token.
     *
     * Mapping từ source mẫu GenerateAccessToken(AppAccess access, string signKey, ...):
     * - Bỏ siteSessionHeader, siteId (không có phân quyền chi nhánh)
     * - Claims: id, name, avatarUrl, roleCode, roleName, scope
     * - Issuer = app.code
     * - Expiry = app.tokenLifetimeMinutes
     * - Sign = app.signingKey (HMAC-SHA256)
     *
     * @param account        account đang login (lấy id, name, avatarUrl)
     * @param app            app đang login vào (lấy code, signingKey, tokenLifetimeMinutes)
     * @param role           role trong app (nullable — lấy roleCode, roleName)
     * @param effectiveScope scope đã resolved (VD: "admin order.create order.view")
     * @return JWT access token string
     */
    String generateAccessToken(Account account, App app, AppRole role, String effectiveScope);
}
