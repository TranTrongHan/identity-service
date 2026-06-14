package com.luketran.identity.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginAppInputRequest {
    @NotBlank(message = "AppCode required")
    public String appCode;
}
