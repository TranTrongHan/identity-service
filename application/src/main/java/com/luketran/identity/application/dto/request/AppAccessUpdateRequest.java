package com.luketran.identity.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu cập nhật AppAccess")
public class AppAccessUpdateRequest {

    @Schema(description = "Mã role mới (null = không thay đổi, rỗng = bỏ role)", example = "manager")
    private String roleCode;

    @Schema(description = "Scope override mới (null = không thay đổi)")
    private String scope;
}
