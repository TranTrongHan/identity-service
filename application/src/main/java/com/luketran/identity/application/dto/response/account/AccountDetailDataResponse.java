package com.luketran.identity.application.dto.response.account;

import com.luketran.identity.application.dto.response.BaseDataResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Chi tiết tài khoản")
public class AccountDetailDataResponse extends BaseDataResponse {

    @Schema(description = "Tên hiển thị", example = "Nguyễn Văn A")
    private String name;

    @Schema(description = "URL ảnh đại diện")
    private String avatarUrl;

    @Schema(description = "Số lần đăng nhập sai")
    private int wrongLoginCount;

    @Schema(description = "Thời gian bị khóa đến")
    private LocalDateTime accessDeniedUntil;

    @Schema(description = "Danh sách phương thức đăng nhập")
    private List<AuthMethodItem> authMethods;

    @Schema(description = "Danh sách quyền truy cập app")
    private List<AppAccessItem> appAccesses;

    @Schema(description = "Danh sách phiên đăng nhập")
    private List<SessionItem> sessions;

    // === Nested DTOs ===

    @Getter
    @Setter
    @Schema(description = "Phương thức đăng nhập")
    public static class AuthMethodItem {
        @Schema(description = "ID")
        private UUID id;

        @Schema(description = "Loại: USERNAME, EMAIL, PHONE", example = "USERNAME")
        private String fieldType;

        @Schema(description = "Giá trị", example = "admin")
        private String fieldValue;
    }

    @Getter
    @Setter
    @Schema(description = "Quyền truy cập app")
    public static class AppAccessItem {
        @Schema(description = "ID")
        private UUID id;

        @Schema(description = "Mã app", example = "IDENTITY")
        private String appCode;

        @Schema(description = "Tên app", example = "Identity Service")
        private String appName;

        @Schema(description = "Mã role", example = "admin")
        private String roleCode;

        @Schema(description = "Tên role", example = "Quản trị viên")
        private String roleName;

        @Schema(description = "Scope quyền", example = "+order.create -report.view")
        private String scope;
    }

    @Getter
    @Setter
    @Schema(description = "Phiên đăng nhập")
    public static class SessionItem {
        @Schema(description = "Session ID (cũng là refresh token)")
        private UUID id;

        @Schema(description = "Mã app", example = "IDENTITY")
        private String appCode;

        @Schema(description = "Thời gian hết hạn")
        private LocalDateTime expiredAt;

        @Schema(description = "Thời gian tạo")
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @Schema(description = "Account item cho danh sách (gọn hơn detail)")
    public static class AccountListItem {
        @Schema(description = "ID")
        private UUID id;

        @Schema(description = "Tên hiển thị")
        private String name;

        @Schema(description = "URL ảnh đại diện")
        private String avatarUrl;

        @Schema(description = "Thời gian tạo")
        private LocalDateTime createdAt;

        @Schema(description = "Thời gian xóa (null nếu active)")
        private LocalDateTime deletedAt;
    }
}
