package com.luketran.identity.webapi.controller.admin;

import com.luketran.identity.application.dto.request.AppAccessCreateRequest;
import com.luketran.identity.application.dto.request.AppAccessUpdateRequest;
import com.luketran.identity.application.dto.response.ApiResponse;
import com.luketran.identity.application.interfaces.AppAccessService;
import com.luketran.identity.domain.entities.AppAccess;
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

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/Admin/AppAccess")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('admin')")
@Tag(name = "Admin - AppAccess", description = "API quản trị quyền truy cập App (yêu cầu quyền admin)")
public class AdminAppAccessController {

    private final AppAccessService appAccessService;

    @PostMapping
    @Operation(summary = "Gán account vào App",
            description = "Tạo AppAccess record — gán một account vào một app với role và scope tùy chọn. "
                    + "Scope sẽ được tự động validate, loại bỏ permission codes không tồn tại.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tạo thành công, trả về UUID"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy App hoặc Role",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<UUID> create(@Valid @RequestBody AppAccessCreateRequest request) {
        return ApiResponse.ok(appAccessService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật quyền truy cập",
            description = "Cập nhật role và/hoặc scope của một AppAccess. "
                    + "Gửi roleCode rỗng để bỏ role. Gửi scope mới để thay đổi scope override.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy AppAccess hoặc Role",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<Void> update(@PathVariable UUID id, @Valid @RequestBody AppAccessUpdateRequest request) {
        appAccessService.update(id, request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{appCode}/{accountId}")
    @Operation(summary = "Lấy quyền truy cập theo App + Account",
            description = "Tìm AppAccess record theo mã app và ID account.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy AppAccess",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<AppAccess> getByAppAccount(@PathVariable String appCode, @PathVariable UUID accountId) {
        return ApiResponse.ok(appAccessService.getByAppCodeAndAccountId(appCode, accountId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa quyền truy cập",
            description = "Soft-delete AppAccess (đánh dấu deletedAt, không xóa thực).")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy AppAccess",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<Void> softDelete(@PathVariable UUID id) {
        appAccessService.softDelete(id);
        return ApiResponse.ok(null);
    }
}
