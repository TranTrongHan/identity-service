package com.luketran.identity.webapi.security;

import com.luketran.identity.domain.entities.App;
import com.luketran.identity.domain.repositories.AppRepository;
import com.luketran.identity.webapi.config.IdentityProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private AppRepository appRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private IdentityProperties identityProperties;
    private JwtAuthenticationFilter filter;

    private static final String APP_CODE = "ROCKY_IDENTITY";
    // 64-byte key for HMAC-SHA256
    private static final String SIGNING_KEY = "1234567890123456789012345678901234567890123456789012345678901234";
    private static final String BYPASS_USER_ID = "f4c4e181-f7c3-4880-b1ab-dd1c91e7f2df";
    private static final String NORMAL_USER_ID = "a1b2c3d4-e5f6-7890-1234-56789abcdef0";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        identityProperties = new IdentityProperties();
        identityProperties.setBypassUserIds(List.of(BYPASS_USER_ID));

        filter = new JwtAuthenticationFilter(appRepository, identityProperties);

        App app = new App();
        app.setId(UUID.randomUUID());
        app.setCode(APP_CODE);
        app.setSigningKey(SIGNING_KEY);

        lenient().when(appRepository.findByCode(APP_CODE)).thenReturn(Optional.of(app));
    }

    private String generateToken(String accountId, Instant issuedAt, Instant expiration, String signingKey) {
        SecretKey key = Keys.hmacShaKeyFor(signingKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .issuer(APP_CODE)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .claims(Map.of(
                        "id", accountId,
                        "name", "Test User",
                        "scope", "admin user.read"
                ))
                .signWith(key)
                .compact();
    }

    @Test
    void doFilterInternal_ValidToken_SetsAuthentication() throws Exception {
        Instant now = Instant.now();
        String token = generateToken(NORMAL_USER_ID, now, now.plus(1, ChronoUnit.HOURS), SIGNING_KEY);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(NORMAL_USER_ID, auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("admin")));
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("user.read")));
    }

    @Test
    void doFilterInternal_ExpiredToken_NormalUser_ClearsAuthentication() throws Exception {
        Instant past = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant expiredAt = Instant.now().minus(1, ChronoUnit.HOURS);
        String expiredToken = generateToken(NORMAL_USER_ID, past, expiredAt, SIGNING_KEY);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + expiredToken);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
    }

    @Test
    void doFilterInternal_ExpiredToken_BypassUser_SetsAuthentication() throws Exception {
        Instant past = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant expiredAt = Instant.now().minus(1, ChronoUnit.HOURS);
        String expiredToken = generateToken(BYPASS_USER_ID, past, expiredAt, SIGNING_KEY);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + expiredToken);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Bypass user should be authenticated even with expired token");
        assertEquals(BYPASS_USER_ID, auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("admin")));
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("user.read")));
    }

    @Test
    void doFilterInternal_InvalidSignature_BypassUser_ClearsAuthentication() throws Exception {
        Instant past = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant expiredAt = Instant.now().minus(1, ChronoUnit.HOURS);
        String wrongKey = "0000000000000000000000000000000000000000000000000000000000000000";
        String tamperedToken = generateToken(BYPASS_USER_ID, past, expiredAt, wrongKey);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + tamperedToken);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "Tampered signature must not be authenticated even for bypass user");
    }
}
