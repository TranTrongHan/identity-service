package com.luketran.identity.webapi.security;

import com.luketran.identity.domain.entities.App;
import com.luketran.identity.domain.repositories.AppRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * JWT Authentication Filter — chạy mỗi request 1 lần.
 *
 * Flow:
 * 1. Extract Bearer token từ Authorization header
 * 2. Decode payload (unsigned) để lấy issuer (app code)
 * 3. Load App từ DB bằng app code → lấy signing key
 * 4. Verify JWT signature + expiry bằng JJWT
 * 5. Extract claims: id (account UUID), scope (space-separated permissions)
 * 6. Tạo Authentication object (principal = accountId, authorities = scope items)
 * 7. Set vào SecurityContextHolder → các endpoint phía sau có thể dùng
 *
 * Nếu token invalid/expired → không set auth → request sẽ là anonymous
 * → SecurityConfig sẽ trả 401 cho các endpoint authenticated.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AppRepository appRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // === STEP 1: Extract token từ header ===
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // === STEP 2: Decode payload (chưa verify) để lấy issuer ===
            // JWT format: header.payload.signature (3 phần ngăn bằng dấu chấm)
            // Payload là base64url-encoded JSON chứa claims
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                filterChain.doFilter(request, response);
                return;
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            // Parse issuer thủ công từ JSON payload
            // Format: {"iss":"APP_CODE","id":"uuid","scope":"admin order.view",...}
            String issuer = extractJsonString(payloadJson, "iss");
            if (issuer == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // === STEP 3: Load App từ DB → lấy signing key ===
            // Mỗi App có signing key riêng (multi-tenant isolation)
            App app = appRepository.findByCode(issuer).orElse(null);
            if (app == null) {
                filterChain.doFilter(request, response);
                return;
            }

            SecretKey key = Keys.hmacShaKeyFor(app.getSigningKey().getBytes(StandardCharsets.UTF_8));

            // === STEP 4: Verify signature + expiry ===
            // JJWT tự động check: signature hợp lệ? token hết hạn chưa?
            // Nếu bất kỳ check nào fail → throw exception → catch block bên dưới
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // === STEP 5: Extract claims ===
            String accountId = claims.get("id", String.class);
            String scope = claims.get("scope", String.class);

            if (accountId == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // === STEP 6: Tạo authorities từ scope ===
            // Scope format: "admin order.create order.view" (space-separated)
            // Mỗi permission code trở thành 1 GrantedAuthority
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (scope != null && !scope.isBlank()) {
                for (String permission : scope.split(" ")) {
                    if (!permission.isBlank()) {
                        authorities.add(new SimpleGrantedAuthority(permission));
                    }
                }
            }

            // === STEP 7: Set Authentication vào SecurityContext ===
            // Principal = accountId (String UUID) → controller lấy bằng auth.getPrincipal()
            // Credentials = null (không cần password, đã verify qua JWT)
            // Authorities = danh sách permissions → dùng cho @PreAuthorize("hasAuthority('admin')")
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(accountId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // Token invalid, expired, signature mismatch, hoặc lỗi parse
            // → Không set Authentication → request sẽ là anonymous
            // → SecurityConfig .anyRequest().authenticated() sẽ reject với 401
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    /**
     * Extract một string value từ JSON đơn giản (không dùng Jackson — tránh thêm dependency vào filter).
     * Chỉ dùng cho trường hợp đơn giản: {"iss":"VALUE",...}
     */
    private String extractJsonString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }
}
