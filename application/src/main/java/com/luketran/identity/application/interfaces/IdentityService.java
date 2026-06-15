package com.luketran.identity.application.interfaces;

import com.luketran.identity.application.dto.request.LoginPasswordRequest;
import com.luketran.identity.application.dto.response.TokenDataResponse;

public interface IdentityService {
    TokenDataResponse loginByPassword(LoginPasswordRequest request);
}
