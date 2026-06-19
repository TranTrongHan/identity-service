package com.luketran.identity.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu đăng nhập Google kèm tự tạo AppAccess")
public class GoogleLoginWithAccessRequest {

    @NotBlank(message = "AppCode required")
    @Schema(description = "Mã ứng dụng cần đăng nhập", example = "MY_APP")
    private String appCode;

    @NotBlank(message = "Authorization code required")
    @Schema(description = "Authorization code nhận từ Google OAuth callback")
    private String code;

    @NotBlank(message = "RedirectUri required")
    @Schema(description = "Redirect URI đã dùng khi lấy auth URL (phải khớp)")
    private String redirectUri;

    @Schema(description = "Mã role gán cho account (nullable)")
    private String roleCode;

    @Schema(description = "Scope bổ sung (nullable)")
    private String scope;
}
