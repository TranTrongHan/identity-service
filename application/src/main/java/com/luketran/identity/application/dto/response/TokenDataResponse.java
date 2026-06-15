package com.luketran.identity.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Thông tin cặp token trả về sau khi xác thực thành công")
public class TokenDataResponse {

    @Schema(description = "Access Token dạng JWT dùng để gửi lên qua Authorization header", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    public String accessToken; 

    @Schema(description = "Refresh Token (Session ID) dạng UUID dùng để gia hạn Access Token mới", example = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6")
    public String refreshToken;

}