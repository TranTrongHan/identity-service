package com.luketran.identity.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu tạo reset password request (gửi qua email)")
public class CreateResetPasswordRequest {

    @NotBlank(message = "AppCode required")
    @Schema(description = "Mã ứng dụng", example = "MY_APP")
    private String appCode;

    @NotBlank(message = "Email required")
    @Schema(description = "Email của tài khoản cần reset password", example = "user@example.com")
    private String email;
}
