package com.luketran.identity.infrastructure.security;

import com.luketran.identity.application.interfaces.TokenService;
import com.luketran.identity.domain.entities.Account;
import com.luketran.identity.domain.entities.App;
import com.luketran.identity.domain.entities.AppRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token generation bằng JJWT library.
 *
 * Mapping từ source mẫu (.NET):
 *   - issuer = access.App.Code           → app.getCode()
 *   - exp = App.TokenLifetime            → app.getTokenLifetimeMinutes()
 *   - sign = App.SigningKey (HMAC-SHA256) → app.getSigningKey()
 *   - claims: id, name, avatarLink, roleCode, roleName, scope
 *   - BỎ: lastSiteId, sites (không có phân quyền chi nhánh)
 *
 * Mỗi App có signing key riêng → token app A không valid cho app B (security isolation).
 */
@Service
public class TokenServiceImpl implements TokenService {

    @Override
    public String generateAccessToken(Account account, App app, AppRole role, String effectiveScope) {

        // 1. Tạo signing key từ app.signingKey (128 chars → 512 bits, đủ cho HS256)
        SecretKey key = Keys.hmacShaKeyFor(app.getSigningKey().getBytes(StandardCharsets.UTF_8));

        // 2. Tính thời gian (tương đương: DateTime.UtcNow.AddMinutes(access.App.TokenLifetime))
        Instant now = Instant.now();
        Instant expiry = now.plus(app.getTokenLifetimeMinutes(), ChronoUnit.MINUTES);

        // 3. Build claims
        //    Source mẫu: new Claim("id", account.Id), new Claim("name", account.Name), ...
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", account.getId().toString());
        claims.put("name", account.getName());

        if (account.getAvatarUrl() != null && !account.getAvatarUrl().isBlank()) {
            claims.put("avatarUrl", account.getAvatarUrl());
        }

        if (role != null) {
            claims.put("roleCode", role.getCode());
            claims.put("roleName", role.getName());
        }

        claims.put("scope", effectiveScope);

        // 4. Build JWT (tương đương: new JwtSecurityToken(issuer, expires, signingCredentials, claims))
        return Jwts.builder()
                .issuer(app.getCode())           // issuer = app code (dùng để resolve signing key khi validate)
                .issuedAt(Date.from(now))         // iat
                .expiration(Date.from(expiry))    // exp
                .claims(claims)                   // custom claims
                .signWith(key)                    // HMAC-SHA256
                .compact();                       // serialize → "eyJhbG..."
    }
}
