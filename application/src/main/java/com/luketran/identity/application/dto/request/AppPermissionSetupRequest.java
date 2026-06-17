package com.luketran.identity.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Yêu cầu bulk setup permissions cho một App")
public class AppPermissionSetupRequest {

    @NotBlank(message = "appCode không được để trống")
    @Schema(description = "Mã App cần setup permissions", example = "pos")
    private String appCode;

    @NotNull(message = "Danh sách permissions không được null")
    @Valid
    @Schema(description = "Toàn bộ danh sách permissions mới nhất của app")
    private List<AppPermissionSetupItemRequest> permissions;
}
