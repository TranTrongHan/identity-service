package com.luketran.identity.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu cập nhật App")
public class AppUpdateRequest {

    @Schema(description = "Tên app")
    private String name;

    @Schema(description = "Mô tả")
    private String description;

    @Positive(message = "Token lifetime phải lớn hơn 0")
    @Schema(description = "Thời gian sống của access token (phút)")
    private Integer tokenLifetimeMinutes;

    @Positive(message = "Session lifetime phải lớn hơn 0")
    @Schema(description = "Thời gian sống của session (phút)")
    private Integer sessionLifetimeMinutes;
}
