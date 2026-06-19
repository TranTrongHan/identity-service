package com.luketran.identity.webapi.controller.common;

import com.luketran.identity.application.dto.response.ApiResponse;
import com.luketran.identity.application.dto.response.AppRoleDataResponse;
import com.luketran.identity.application.interfaces.AppRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/AppRole")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('admin')")
@Tag(name = "AppRole", description = "API lấy danh sách Role (yêu cầu quyền admin)")
public class PublicAppRoleController {

    private final AppRoleService appRoleService;

    @GetMapping("/All")
    @Operation(summary = "Lấy tất cả Roles", description = "Trả về danh sách tất cả roles active.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công")
    })
    public ApiResponse<List<AppRoleDataResponse>> getAll() {
        return ApiResponse.ok(appRoleService.findAll());
    }
}
