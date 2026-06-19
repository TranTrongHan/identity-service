package com.luketran.identity.webapi.controller.common;

import com.luketran.identity.application.dto.request.*;
import com.luketran.identity.application.dto.response.ApiResponse;
import com.luketran.identity.application.dto.response.GoogleAuthUrlResponse;
import com.luketran.identity.application.dto.response.ResetPasswordDataResponse;
import com.luketran.identity.application.dto.response.TokenDataResponse;
import com.luketran.identity.application.interfaces.AccountLogoutService;
import com.luketran.identity.application.interfaces.IdentityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/Identity")
@Tag(name = "Xác thực & Định danh", description = "Các API công khai hỗ trợ đăng nhập và làm mới token")
public class IdentityController {
    private final IdentityService identityService;
    private final AccountLogoutService accountLogoutService;


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

    @PostMapping("/RefreshToken")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Làm mới Access Token", description = "Dùng Refresh Token (Session UUID) để gia hạn phiên đăng nhập và nhận cặp tokens mới.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Refresh thành công, trả về cặp tokens mới"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Refresh token không hợp lệ hoặc rỗng",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Phiên đăng nhập đã hết hạn hoặc không tồn tại",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Lỗi hệ thống bất ngờ",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ApiResponse<TokenDataResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(identityService.refreshToken(request));
    }

    @GetMapping("/ForceLogout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Yêu cầu ForceLogout", description = "Kiểm tra và thực hiện ForceLogout đối với tài khoản hiện tại từ Security Context.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Thực hiện thành công (trả về true nếu tài khoản bị bắt buộc đăng xuất và đã bị xóa trạng thái logout, ngược lại trả về false)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Token không hợp lệ hoặc chưa đăng nhập",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Lỗi hệ thống bất ngờ",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ApiResponse<Boolean> forceLogout(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new com.luketran.identity.domain.exceptions.AuthenticationException("User not authenticated");
        }
        
        // Trích xuất principal là Account ID (dạng String UUID) từ authentication object
        UUID accountId = UUID.fromString((String) authentication.getPrincipal());
        
        return ApiResponse.ok(accountLogoutService.checkForceLogout(accountId));
    }

    @PostMapping("/Auth/Google")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Lấy Google OAuth URL", description = "Tạo link đăng nhập Google OAuth để frontend redirect người dùng.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Trả về URL redirect"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "App không tồn tại hoặc chưa cấu hình Google OAuth",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<GoogleAuthUrlResponse> getGoogleAuthUrl(@Valid @RequestBody GoogleAuthRequest request) {
        return ApiResponse.ok(identityService.getGoogleAuthUrl(request));
    }

    @PostMapping("/Login/Google/WithAccess")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Đăng nhập Google kèm tạo AppAccess", description = "Exchange authorization code từ Google, xác thực user, tự tạo account + AppAccess nếu chưa có, trả về cặp tokens.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng nhập thành công, trả về cặp tokens"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Xác thực Google thất bại hoặc domain không được phép",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<TokenDataResponse> loginByGoogleWithAccess(@Valid @RequestBody GoogleLoginWithAccessRequest request) {
        return ApiResponse.ok(identityService.loginByGoogleWithAccess(request));
    }

    @PostMapping("/ResetPassword")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Tạo yêu cầu reset password", description = "Tạo reset password request cho account theo email. Trả về request ID (frontend dùng ID này để gọi PUT).")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tạo thành công, trả về ID"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "App hoặc email không tồn tại",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<ResetPasswordDataResponse> createResetPasswordRequest(@Valid @RequestBody CreateResetPasswordRequest request) {
        return ApiResponse.ok(identityService.createResetPasswordRequest(request));
    }

    @PutMapping("/ResetPassword")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Đặt lại password", description = "Dùng reset token (requestId) để đặt lại password mới cho tài khoản.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reset thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token đã hết hạn",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Reset request không tồn tại",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<Void> executeResetPassword(@Valid @RequestBody ExecuteResetPasswordRequest request) {
        identityService.executeResetPassword(request);
        return ApiResponse.ok(null);
    }
}
