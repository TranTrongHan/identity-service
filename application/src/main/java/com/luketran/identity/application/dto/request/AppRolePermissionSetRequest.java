package com.luketran.identity.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "Yêu cầu gán permissions cho role (thay thế toàn bộ)")
public class AppRolePermissionSetRequest {

    @NotNull(message = "permissionIds không được null")
    @Schema(description = "Danh sách permission IDs gán cho role")
    private List<UUID> permissionIds;
}
