package com.luketran.identity.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "URL để redirect người dùng sang Google OAuth")
public class GoogleAuthUrlResponse {

    @Schema(description = "Google OAuth URL đầy đủ (frontend redirect tới URL này)")
    private String url;
}
