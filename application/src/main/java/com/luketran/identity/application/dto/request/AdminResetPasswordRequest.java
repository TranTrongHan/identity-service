package com.luketran.identity.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminResetPasswordRequest {
    @NotBlank
    private String newPassword;
}
