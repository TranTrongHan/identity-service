package com.luketran.identity.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Filter cho danh sách tài khoản")
public class AccountFilterRequest extends BaseFilterRequest {

    @Schema(description = "Tìm theo tên (like)", example = "Nguyễn")
    private String name;

    @Schema(description = "Lọc theo mã app", example = "IDENTITY")
    private String appCode;

    @Schema(description = "Lọc theo mã chức vụ", example = "admin")
    private String roleCode;
}
