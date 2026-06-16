package com.luketran.identity.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Base response chứa các trường chung cho mọi entity")
public abstract class BaseDataResponse {

    @Schema(description = "ID định danh", example = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6")
    private UUID id;

    @Schema(description = "Thời gian khởi tạo")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian xóa (null nếu chưa xóa)")
    private LocalDateTime deletedAt;
}
