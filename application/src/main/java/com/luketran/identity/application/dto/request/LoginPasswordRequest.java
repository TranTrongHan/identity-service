package com.luketran.identity.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Thông tin yêu cầu đăng nhập bằng tài khoản và mật khẩu")
public class LoginPasswordRequest {
    @NotBlank(message = "AppCode required")
    @Schema(description = "Mã định danh của ứng dụng cần đăng nhập", example = "IDENTITY", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appCode;

    @NotBlank(message = "Username required")
    @Schema(description = "Tên đăng nhập của tài khoản", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Password required")
    @Schema(description = "Mật khẩu của tài khoản", example = "123456aA@", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
