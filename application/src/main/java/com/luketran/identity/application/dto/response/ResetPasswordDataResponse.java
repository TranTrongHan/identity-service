package com.luketran.identity.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Kết quả tạo yêu cầu reset password")
public class ResetPasswordDataResponse {

    @Schema(description = "ID của reset password request (dùng để gọi PUT /Identity/ResetPassword)")
    private UUID id;
}
