package com.luketran.identity.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu lấy Google OAuth URL để redirect người dùng")
public class GoogleAuthRequest {

    @NotBlank(message = "AppCode required")
    @Schema(description = "Mã ứng dụng cần đăng nhập", example = "MY_APP")
    private String appCode;

    @NotBlank(message = "RedirectUri required")
    @Schema(description = "URL redirect sau khi Google xác thực xong", example = "https://myapp.com/auth/google/callback")
    private String redirectUri;
}
