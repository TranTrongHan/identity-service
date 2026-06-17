package com.luketran.identity.webapi.controller.admin;

import com.luketran.identity.application.dto.request.AppCreateRequest;
import com.luketran.identity.application.dto.request.AppUpdateRequest;
import com.luketran.identity.application.dto.response.ApiResponse;
import com.luketran.identity.application.interfaces.AppService;
import com.luketran.identity.domain.entities.App;
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
@RequestMapping("/Admin/App")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('admin')")
@Tag(name = "Admin - App", description = "API quản trị App (yêu cầu quyền admin)")
public class AdminAppController {

    private final AppService appService;

    @PostMapping
    @Operation(summary = "Tạo App mới", description = "Tạo một ứng dụng mới với signing key, token/session lifetime riêng.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tạo thành công, trả về UUID"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<UUID> create(@Valid @RequestBody AppCreateRequest request) {
        return ApiResponse.ok(appService.create(request));
    }

    @GetMapping("/All")
    @Operation(summary = "Lấy tất cả Apps", description = "Trả về danh sách tất cả app chưa bị xóa.")
    public ApiResponse<List<App>> getAll() {
        return ApiResponse.ok(appService.findAll());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật App", description = "Cập nhật thông tin app (name, description, token/session lifetime).")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy App",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<Void> update(@PathVariable UUID id, @Valid @RequestBody AppUpdateRequest request) {
        appService.update(id, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa App", description = "Soft-delete app (đánh dấu deletedAt).")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy App",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<Void> softDelete(@PathVariable UUID id) {
        appService.softDelete(id);
        return ApiResponse.ok(null);
    }
}
