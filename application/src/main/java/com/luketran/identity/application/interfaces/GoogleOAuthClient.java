package com.luketran.identity.application.interfaces;

/**
 * Client gọi Google OAuth API.
 * Interface ở application layer, implementation ở infrastructure (dùng RestTemplate).
 */
public interface GoogleOAuthClient {

    /**
     * Exchange authorization code lấy từ Google callback thành thông tin người dùng.
     *
     * @param clientId     Google Client ID (từ App config)
     * @param clientSecret Google Client Secret (từ App config)
     * @param code         Authorization code từ Google callback
     * @param redirectUri  Redirect URI đã dùng khi tạo auth URL
     * @return Thông tin user từ Google
     */
    GoogleUserInfo exchangeCodeForUserInfo(String clientId, String clientSecret, String code, String redirectUri);

    /**
     * Thông tin user trả về từ Google.
     */
    record GoogleUserInfo(String email, String name, String picture) {}
}
