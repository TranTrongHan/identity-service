package com.luketran.identity.webapi.controller.common;

import com.luketran.identity.application.dto.request.LoginPasswordRequest;
import com.luketran.identity.application.dto.response.ApiResponse;
import com.luketran.identity.application.dto.response.TokenDataResponse;
import com.luketran.identity.application.interfaces.IdentityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/Identity")
@Tag(name = "Xác thực & Định danh", description = "Các API công khai hỗ trợ đăng nhập và làm mới token")
public class IdentityController {
    private final IdentityService identityService;

    @PostMapping("/Login/Password")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Đăng nhập bằng mật khẩu", description = "Xác thực tài khoản và trả về cặp tokens (Access Token và Refresh Token) của người dùng đối với ứng dụng tương ứng.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Đăng nhập thành công, trả về cặp tokens"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Thông tin đăng nhập gửi lên không hợp lệ",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Tên đăng nhập hoặc mật khẩu không chính xác",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "429",
            description = "Tài khoản tạm thời bị khóa do brute force",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Lỗi hệ thống bất ngờ",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ApiResponse<TokenDataResponse> loginByPassword(@Valid @RequestBody LoginPasswordRequest request) {
        return ApiResponse.ok(identityService.loginByPassword(request));
    }
}
