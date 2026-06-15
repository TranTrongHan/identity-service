package com.luketran.identity.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Thông tin yêu cầu refresh token")
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh token không được để trống")
    @Schema(description = "Refresh Token (UUID session ID)", example = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6")
    private String refreshToken;
}
