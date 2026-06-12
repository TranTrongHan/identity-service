package com.luketran.identity.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ErrorDetail error;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static ApiResponse<Void> ok() {
        return ApiResponse.<Void>builder()
                .success(true)
                .build();
    }

    public static ApiResponse<Void> error(String code, String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .error(new ErrorDetail(code, message, null))
                .build();
    }

    public static ApiResponse<Void> error(String code, String message, Object details) {
        return ApiResponse.<Void>builder()
                .success(false)
                .error(new ErrorDetail(code, message, details))
                .build();
    }
}
