package com.luketran.identity.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "Thông tin Permission")
public class AppPermissionDataResponse {

    @Schema(description = "ID của permission")
    private UUID id;

    @Schema(description = "ID của app sở hữu permission")
    private UUID appId;

    @Schema(description = "Mã permission", example = "order.create")
    private String code;

    @Schema(description = "Tên permission", example = "Tạo đơn hàng")
    private String name;

    @Schema(description = "Nhóm permission", example = "Đơn hàng")
    private String groupName;

    @Schema(description = "Mô tả")
    private String description;

    @Schema(description = "Danh sách mã permission con (include)")
    private List<String> includePermissionCodes;

    @Schema(description = "Thời điểm tạo")
    private LocalDateTime createdAt;
}
