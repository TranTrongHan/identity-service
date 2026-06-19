package com.luketran.identity.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "Thông tin App")
public class AppDataResponse {

    @Schema(description = "ID")
    private UUID id;

    @Schema(description = "Mã app", example = "IDENTITY")
    private String code;

    @Schema(description = "Tên app", example = "Identity Service")
    private String name;

    @Schema(description = "Mô tả")
    private String description;

    @Schema(description = "Token lifetime (phút)")
    private int tokenLifetimeMinutes;

    @Schema(description = "Session lifetime (phút)")
    private int sessionLifetimeMinutes;

    @Schema(description = "Thời điểm tạo")
    private LocalDateTime createdAt;
}
