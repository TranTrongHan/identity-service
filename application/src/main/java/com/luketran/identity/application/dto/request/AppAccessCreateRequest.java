package com.luketran.identity.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Yêu cầu tạo AppAccess (gán account vào app)")
public class AppAccessCreateRequest {

    @NotNull(message = "accountId không được null")
    @Schema(description = "ID của Account cần gán quyền")
    private UUID accountId;

    @NotBlank(message = "appCode không được để trống")
    @Schema(description = "Mã App", example = "pos")
    private String appCode;

    @Schema(description = "Mã role (optional, không bắt buộc)", example = "manager")
    private String roleCode;

    @Schema(description = "Scope override (optional, VD: '+export.data -order.delete')")
    private String scope;
}
