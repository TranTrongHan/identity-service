package com.luketran.identity.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Yêu cầu đặt lại password bằng reset token")
public class ExecuteResetPasswordRequest {

    @NotNull(message = "RequestId required")
    @Schema(description = "ID của reset password request")
    private UUID requestId;

    @NotBlank(message = "NewPassword required")
    @Schema(description = "Mật khẩu mới")
    private String newPassword;
}
