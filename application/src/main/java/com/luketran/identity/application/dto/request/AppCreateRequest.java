package com.luketran.identity.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu tạo App mới")
public class AppCreateRequest {

    @NotBlank(message = "Code không được để trống")
    @Schema(description = "Mã định danh app (unique)", example = "IDENTITY")
    private String code;

    @NotBlank(message = "Name không được để trống")
    @Schema(description = "Tên app", example = "Identity Service")
    private String name;

    @Schema(description = "Mô tả")
    private String description;

    @NotBlank(message = "Signing key không được để trống")
    @Schema(description = "Signing key cho JWT (128 ký tự)")
    private String signingKey;

    @Positive(message = "Token lifetime phải lớn hơn 0")
    @Schema(description = "Thời gian sống của access token (phút)", example = "15", defaultValue = "15")
    private int tokenLifetimeMinutes = 15;

    @Positive(message = "Session lifetime phải lớn hơn 0")
    @Schema(description = "Thời gian sống của session (phút)", example = "1440", defaultValue = "1440")
    private int sessionLifetimeMinutes = 1440;
}
