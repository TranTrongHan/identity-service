package com.luketran.identity.webapi.controller.admin;

import com.luketran.identity.application.dto.request.AppRoleCreateRequest;
import com.luketran.identity.application.dto.request.AppRolePermissionSetRequest;
import com.luketran.identity.application.dto.request.AppRoleUpdateRequest;
import com.luketran.identity.application.dto.response.ApiResponse;
import com.luketran.identity.application.interfaces.AppRoleService;
import com.luketran.identity.domain.entities.AppRole;
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
@RequestMapping("/Admin/AppRole")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('admin')")
@Tag(name = "Admin - AppRole", description = "API quản trị Role (yêu cầu quyền admin)")
public class AdminAppRoleController {

    private final AppRoleService appRoleService;

    @PostMapping
    @Operation(summary = "Tạo Role mới", description = "Tạo role mới thuộc 1 App.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tạo thành công, trả về UUID"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<UUID> create(@Valid @RequestBody AppRoleCreateRequest request) {
        return ApiResponse.ok(appRoleService.create(request));
    }

    @GetMapping("/All")
    @Operation(summary = "Lấy tất cả Roles", description = "Trả về danh sách tất cả roles chưa bị xóa.")
    public ApiResponse<List<AppRole>> getAll() {
        return ApiResponse.ok(appRoleService.findAll());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật Role", description = "Cập nhật thông tin role (tên).")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy Role",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<Void> update(@PathVariable UUID id, @Valid @RequestBody AppRoleUpdateRequest request) {
        appRoleService.update(id, request);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/Permission")
    @Operation(summary = "Gán permissions cho Role", description = "Thay thế toàn bộ permissions của role bằng danh sách mới. Xóa hết permission cũ, insert lại.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Gán permissions thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy Role",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<Void> setPermissions(@PathVariable UUID id, @Valid @RequestBody AppRolePermissionSetRequest request) {
        appRoleService.setPermissions(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa Role", description = "Soft-delete role (đánh dấu deletedAt).")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy Role",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<Void> softDelete(@PathVariable UUID id) {
        appRoleService.softDelete(id);
        return ApiResponse.ok(null);
    }
}
