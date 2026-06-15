package com.luketran.identity.webapi.config;

import com.luketran.identity.webapi.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration cho JWT stateless API.
 *
 * Giải thích từng cấu hình:
 * - csrf.disable()       → REST API không dùng cookie/session, CSRF không cần
 * - sessionCreationPolicy(STATELESS) → Không tạo HttpSession, mỗi request tự authenticate qua JWT
 * - formLogin.disable()  → Không cần form login HTML (API trả JSON)
 * - httpBasic.disable()  → Không dùng HTTP Basic Auth
 * - addFilterBefore(jwtFilter, ...) → Chèn JWT filter VÀO TRƯỚC UsernamePasswordAuthenticationFilter
 * - authorizeHttpRequests → Rules phân quyền theo URL
 * - exceptionHandling    → Trả JSON 401/403 thay vì redirect
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // === 1. Disable CSRF ===
            // CSRF protection chỉ cần cho cookie-based auth (browser form submit).
            // JWT Bearer token không bị CSRF attack vì browser không tự gắn vào request.
            .csrf(csrf -> csrf.disable())

            // === 2. Stateless session ===
            // Không tạo HttpSession. Mỗi request phải mang JWT riêng.
            // SecurityContextHolderFilter sẽ dùng NullSecurityContextRepository.
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // === 3. Disable form login & HTTP Basic ===
            // Không cần vì API authenticate qua JWT trong Authorization header.
            // Disable cũng loại bỏ DefaultLoginPageGeneratingFilter khỏi chain.
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            // === 4. Chèn JWT filter vào Security Filter Chain ===
            // Đặt TRƯỚC UsernamePasswordAuthenticationFilter (vị trí authentication).
            // Khi JWT filter set Authentication vào SecurityContext,
            // AuthorizationFilter phía sau sẽ thấy user đã authenticated.
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // === 5. URL-based authorization rules ===
            .authorizeHttpRequests(auth -> auth
                // Public: ai cũng truy cập được (không cần token)
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/Identity/Login/**").permitAll()
                .requestMatchers("/Identity/RefreshToken").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // Protected: tối thiểu phải có valid JWT
                .anyRequest().authenticated()
            )

            // === 6. Custom exception handling ===
            // Trả JSON response thay vì redirect tới login page.
            .exceptionHandling(ex -> ex
                // AuthenticationEntryPoint: khi request chưa authenticated (401)
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                        "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"Authentication required\"}}"
                    );
                })
                // AccessDeniedHandler: khi đã authenticated nhưng thiếu quyền (403)
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                        "{\"success\":false,\"error\":{\"code\":\"FORBIDDEN\",\"message\":\"Access denied\"}}"
                    );
                })
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
