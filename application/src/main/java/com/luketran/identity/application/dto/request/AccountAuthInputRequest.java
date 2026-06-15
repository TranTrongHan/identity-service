package com.luketran.identity.application.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountAuthInputRequest {
    @NotEmpty
    private int fieldType;
    @NotNull
    private String fieldValue;


}
