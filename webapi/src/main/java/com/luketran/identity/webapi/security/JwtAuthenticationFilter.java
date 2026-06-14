package com.luketran.identity.webapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT Authentication Filter.
 *
 * Chạy mỗi request 1 lần (OncePerRequestFilter).
 * Extract Bearer token từ Authorization header → validate → set SecurityContext.
 *
 * Hiện tại là skeleton — sẽ implement đầy đủ khi có TokenService.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Extract token từ header
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Không có token → cho request đi tiếp (sẽ là anonymous)
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // bỏ "Bearer "

        // 2. TODO: Validate token (khi có TokenService)
        //    - Parse token → lấy issuer (app code)
        //    - Resolve signing key từ DB by app code
        //    - Validate signature + expiry
        //    - Extract claims: id, name, scope, roleCode

        // 3. TODO: Tạo Authentication object và set vào SecurityContext
        //    Ví dụ khi implement xong:
        //
        //    List<SimpleGrantedAuthority> authorities = scopeList.stream()
        //        .map(SimpleGrantedAuthority::new)
        //        .toList();
        //
        //    UsernamePasswordAuthenticationToken authentication =
        //        new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
        //
        //    SecurityContextHolder.getContext().setAuthentication(authentication);

        // 4. Cho request đi tiếp trong filter chain
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Những path không cần check JWT (performance optimization)
        String path = request.getServletPath();
        return path.startsWith("/auth/")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs");
    }
}
