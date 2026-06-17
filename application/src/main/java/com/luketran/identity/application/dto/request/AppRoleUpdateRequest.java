package com.luketran.identity.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu cập nhật AppRole")
public class AppRoleUpdateRequest {

    @Schema(description = "Tên role mới")
    private String name;
}
