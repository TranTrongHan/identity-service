package com.luketran.identity.application.interfaces;

import com.luketran.identity.application.dto.request.LoginPasswordRequest;
import com.luketran.identity.application.dto.request.RefreshTokenRequest;
import com.luketran.identity.application.dto.response.TokenDataResponse;

import java.util.UUID;

public interface IdentityService {
    TokenDataResponse loginByPassword(LoginPasswordRequest request);

    TokenDataResponse refreshToken(RefreshTokenRequest request);
}
