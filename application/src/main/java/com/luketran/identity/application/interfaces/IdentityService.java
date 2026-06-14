package com.luketran.identity.application.interfaces;

import com.luketran.identity.application.dto.request.LoginAppInputRequest;
import com.luketran.identity.application.dto.request.LoginPasswordInputRequest;
import com.luketran.identity.application.dto.response.TokenDataResponse;

public interface IdentityService {
    TokenDataResponse loginByPassword(LoginAppInputRequest appInputRequest, LoginPasswordInputRequest passwordInputRequest);
}
