package com.luketran.identity.webapi.controller.common;

import com.luketran.identity.application.dto.response.ApiResponse;
import com.luketran.identity.application.interfaces.AppPermissionService;
import com.luketran.identity.domain.entities.AppPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/AppPermission")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('admin')")
@Tag(name = "AppPermission", description = "API lấy danh sách Permission (yêu cầu quyền admin)")
public class PublicAppPermissionController {

    private final AppPermissionService appPermissionService;

    @GetMapping("/All")
    @Operation(summary = "Lấy tất cả Permissions",
            description = "Trả về danh sách permissions active của một App.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy App",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<List<AppPermission>> getAll(@RequestParam String appCode) {
        return ApiResponse.ok(appPermissionService.findAllActiveByAppCode(appCode));
    }
}
