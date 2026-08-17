package com.luketran.identity.application.interfaces;

import com.luketran.identity.application.dto.request.*;
import com.luketran.identity.application.dto.response.GoogleAuthUrlResponse;
import com.luketran.identity.application.dto.response.ResetPasswordDataResponse;
import com.luketran.identity.application.dto.response.TokenDataResponse;

public interface IdentityService {
    TokenDataResponse loginByPassword(LoginPasswordRequest request);

    TokenDataResponse refreshToken(RefreshTokenRequest request);

    GoogleAuthUrlResponse getGoogleAuthUrl(GoogleAuthRequest request);

    TokenDataResponse loginByGoogleWithAccess(GoogleLoginWithAccessRequest request);

    ResetPasswordDataResponse createResetPasswordRequest(CreateResetPasswordRequest request);

    void executeResetPassword(ExecuteResetPasswordRequest request);
}
