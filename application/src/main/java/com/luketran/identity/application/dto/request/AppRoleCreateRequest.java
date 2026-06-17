package com.luketran.identity.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Yêu cầu tạo AppRole mới")
public class AppRoleCreateRequest {

    @NotNull(message = "appId không được null")
    @Schema(description = "ID của App")
    private UUID appId;

    @NotBlank(message = "Code không được để trống")
    @Schema(description = "Mã role (unique trong app)", example = "admin")
    private String code;

    @NotBlank(message = "Name không được để trống")
    @Schema(description = "Tên role", example = "Quản trị viên")
    private String name;
}
