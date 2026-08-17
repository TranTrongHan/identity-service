package com.luketran.identity.infrastructure.external;

import com.luketran.identity.application.interfaces.GoogleOAuthClient;
import com.luketran.identity.domain.exceptions.AuthenticationException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Implementation gọi Google OAuth2 API bằng RestTemplate.
 * Flow: exchange code → access_token → gọi userinfo endpoint.
 */
@Service
public class GoogleOAuthClientImpl implements GoogleOAuthClient {

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final RestTemplate restTemplate;

    public GoogleOAuthClientImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public GoogleUserInfo exchangeCodeForUserInfo(String clientId, String clientSecret, String code, String redirectUri) {
        // 1. Exchange code → access_token
        String accessToken = exchangeCode(clientId, clientSecret, code, redirectUri);

        // 2. Gọi userinfo endpoint
        return fetchUserInfo(accessToken);
    }

    private String exchangeCode(String clientId, String clientSecret, String code, String redirectUri) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, request, Map.class);
            Map body = response.getBody();
            if (body == null || !body.containsKey("access_token")) {
                throw new AuthenticationException("Google OAuth: failed to exchange code");
            }
            return (String) body.get("access_token");
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationException("Google OAuth: failed to exchange code - " + e.getMessage());
        }
    }

    private GoogleUserInfo fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(USERINFO_URL, HttpMethod.GET, request, Map.class);
            Map body = response.getBody();
            if (body == null || !body.containsKey("email")) {
                throw new AuthenticationException("Google OAuth: failed to get user info");
            }
            String email = (String) body.get("email");
            String name = (String) body.get("name");
            String picture = (String) body.get("picture");
            return new GoogleUserInfo(email, name, picture);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationException("Google OAuth: failed to get user info - " + e.getMessage());
        }
    }
}
