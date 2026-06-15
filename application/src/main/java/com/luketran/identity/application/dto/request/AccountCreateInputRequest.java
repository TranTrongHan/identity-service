package com.luketran.identity.application.dto.request;


import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Null;
import lombok.Data;
import org.springframework.format.annotation.NumberFormat;

@Data
public class AccountCreateInputRequest {

    @NotBlank
    private String name;
    @NotBlank
    private String userName;
    @Email
    @Null
    private String email;
    @Null
    @NumberFormat
    private String phoneNumber;
    @NotBlank
    @NotEmpty
    private String password;
}
