package com.luketran.identity.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "Thông tin Role")
public class AppRoleDataResponse {

    @Schema(description = "ID của role")
    private UUID id;

    @Schema(description = "ID của app sở hữu role")
    private UUID appId;

    @Schema(description = "Mã role", example = "ADMIN")
    private String code;

    @Schema(description = "Tên role", example = "Quản trị viên")
    private String name;

    @Schema(description = "Thời điểm tạo")
    private LocalDateTime createdAt;
}
