package com.luketran.identity.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginPasswordInputRequest {
    @NotBlank(message = "Username required")
    private String username;
    @NotBlank(message = "Password required")
    private String password;
}
