package com.luketran.identity.webapi.controller.admin;

import com.luketran.identity.application.dto.request.AccountCreateInputRequest;
import com.luketran.identity.application.dto.request.AccountFilterRequest;
import com.luketran.identity.application.dto.request.AdminResetPasswordRequest;
import com.luketran.identity.application.dto.response.ApiResponse;
import com.luketran.identity.application.dto.response.PageResponse;
import com.luketran.identity.application.dto.response.account.AccountDetailDataResponse;
import com.luketran.identity.application.interfaces.AccountLogoutService;
import com.luketran.identity.application.interfaces.AccountService;
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
@RequestMapping("/Admin/Account")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('admin')")
@Tag(name = "Admin - Tài khoản", description = "API quản trị tài khoản (yêu cầu quyền admin)")
public class AdminAccountController {

    private final AccountLogoutService accountLogoutService;
    private final AccountService accountService;

    @PutMapping("/{id}/ForceLogout")
    @Operation(summary = "Ép đăng xuất tài khoản", description = "Xóa toàn bộ phiên đăng nhập (sessions) của tài khoản và tạo flag force-logout.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Ép đăng xuất thành công"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Chưa xác thực",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Không có quyền admin",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Lỗi hệ thống bất ngờ",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ApiResponse<Void> createForceLogout(@PathVariable UUID id) {
        accountLogoutService.createForceLogout(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/ForceLogout")
    @Operation(summary = "Kiểm tra trạng thái force-logout", description = "Kiểm tra tài khoản có đang bị ép đăng xuất không (không consume flag, chỉ check).")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Trả về true nếu account đang bị force-logout, false nếu không"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Chưa xác thực",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Không có quyền admin",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Lỗi hệ thống bất ngờ",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ApiResponse<Boolean> getForceLogoutStatus(@PathVariable UUID id) {
        return ApiResponse.ok(accountLogoutService.isForceLoggedOut(id));
    }

    @PostMapping
    @Operation(summary = "Tạo tài khoản mới", description = "Tạo account mới với thông tin đăng nhập (username bắt buộc, email/phone tùy chọn). Password sẽ được hash với secret key ngẫu nhiên.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Tạo tài khoản thành công, trả về UUID của account"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Dữ liệu đầu vào không hợp lệ hoặc username/email/phone đã tồn tại",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Chưa xác thực",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Không có quyền admin",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Lỗi hệ thống bất ngờ",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ApiResponse<UUID> create(@Valid @RequestBody AccountCreateInputRequest inputRequest) {
        return ApiResponse.ok(accountService.create(inputRequest));
    }

    @PutMapping("/{id}/Password")
    @Operation(summary = "Thiết lập mật khẩu tài khoản", description = "Admin set/reset mật khẩu cho tài khoản chỉ định. Mật khẩu sẽ được hash với secret key riêng của account.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Thiết lập mật khẩu thành công"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Dữ liệu không hợp lệ (mật khẩu trống hoặc không đúng format)",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Chưa xác thực",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Không có quyền admin",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy tài khoản",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Lỗi hệ thống bất ngờ",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ApiResponse<Void> resetPassword(@PathVariable UUID id, @Valid @RequestBody AdminResetPasswordRequest request) {
        accountService.resetPassword(id, request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết tài khoản", description = "Lấy thông tin chi tiết account kèm auth methods, app accesses và sessions.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Trả về chi tiết tài khoản"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy tài khoản",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<AccountDetailDataResponse> getDetail(@PathVariable UUID id) {
        return ApiResponse.ok(accountService.retreive(id));
    }

    @GetMapping
    @Operation(summary = "Danh sách tài khoản", description = "Lấy danh sách tài khoản có phân trang, hỗ trợ filter theo tên và trạng thái.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Trả về danh sách tài khoản phân trang")
    })
    public ApiResponse<PageResponse<AccountDetailDataResponse.AccountListItem>> getPage(AccountFilterRequest filter) {
        return ApiResponse.ok(accountService.getPage(filter));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa tài khoản", description = "Soft-delete tài khoản (đánh dấu deletedAt, không xóa thực sự khỏi DB).")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy tài khoản",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<Void> softDelete(@PathVariable UUID id) {
        accountService.softDelete(id);
        return ApiResponse.ok(null);
    }
}
