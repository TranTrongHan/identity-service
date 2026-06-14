package com.luketran.identity.application.dto.response;

import lombok.Data;

@Data
public class TokenDataResponse {

    public String accessToken; 
    public String refreshToken;

}