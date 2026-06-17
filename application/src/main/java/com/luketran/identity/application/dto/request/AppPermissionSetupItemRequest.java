package com.luketran.identity.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "Một permission item trong danh sách Setup")
public class AppPermissionSetupItemRequest {

    @Schema(description = "ID permission (null = tạo mới, có giá trị = cập nhật)")
    private UUID id;

    @NotBlank(message = "Code không được để trống")
    @Schema(description = "Mã permission (unique trong app)", example = "order.create")
    private String code;

    @NotBlank(message = "Name không được để trống")
    @Schema(description = "Tên hiển thị", example = "Tạo đơn hàng")
    private String name;

    @Schema(description = "Tên nhóm (phân loại permission)", example = "Đơn hàng")
    private String groupName;

    @Schema(description = "Mô tả chi tiết")
    private String description;

    @Schema(description = "Danh sách mã permission được bao gồm khi chọn permission này")
    private List<String> includePermissionCodes;
}
