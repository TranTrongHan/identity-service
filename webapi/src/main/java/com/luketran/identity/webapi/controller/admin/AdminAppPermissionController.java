package com.luketran.identity.webapi.controller.admin;

import com.luketran.identity.application.dto.request.AppPermissionSetupRequest;
import com.luketran.identity.application.dto.response.ApiResponse;
import com.luketran.identity.application.interfaces.AppPermissionService;
import com.luketran.identity.domain.entities.AppPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/Admin/AppPermission")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('admin')")
@Tag(name = "Admin - AppPermission", description = "API quản trị Permission (yêu cầu quyền admin)")
public class AdminAppPermissionController {

    private final AppPermissionService appPermissionService;

    @PostMapping("/Setup")
    @Operation(summary = "Bulk setup permissions",
            description = "Đồng bộ toàn bộ danh sách permissions cho một App. " +
                    "Permission có trong input sẽ được tạo mới hoặc cập nhật. " +
                    "Permission không có trong input sẽ bị xóa. " +
                    "Scope của AppAccess sẽ được tự động dọn dẹp.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Setup thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy App",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<Void> setup(@Valid @RequestBody AppPermissionSetupRequest request) {
        appPermissionService.setup(request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/All")
    @Operation(summary = "Lấy tất cả Permissions", description = "Trả về danh sách permissions active của một App.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy App",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<List<AppPermission>> getAll(@RequestParam String appCode) {
        return ApiResponse.ok(appPermissionService.findAllActiveByAppCode(appCode));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa Permission", description = "Soft-delete permission (đánh dấu deletedAt, không xóa thực).")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy Permission",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<Void> softDelete(@PathVariable UUID id) {
        appPermissionService.softDelete(id);
        return ApiResponse.ok(null);
    }
}
