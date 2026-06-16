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
     * @param account        account dang login (lay id, name, avatarUrl)
     * @param app            app dang login vao (lay code, signingKey, tokenLifetimeMinutes)
     * @param role           role trong app (nullable — lay roleCode, roleName)
     * @param effectiveScope scope da resolved (VD: "admin order.create order.view")
     * @return JWT access token string
     */
    String generateAccessToken(Account account, App app, AppRole role, String effectiveScope);
}
