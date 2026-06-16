package com.luketran.identity.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Base filter cho các endpoint list có phân trang")
public abstract class BaseFilterRequest {

    @Schema(description = "Số trang (bắt đầu từ 1)", example = "1", defaultValue = "1")
    private int page = 1;

    @Schema(description = "Kích thước trang", example = "10", defaultValue = "10")
    private int size = 10;

    @Schema(description = "Lọc theo trạng thái: ALL (tất cả), ACTIVE (chưa xóa), INACTIVE (đã xóa)", example = "ACTIVE", defaultValue = "ACTIVE")
    private StatusFilter status = StatusFilter.ACTIVE;

    public enum StatusFilter {
        ALL,
        ACTIVE,
        INACTIVE
    }

    /**
     * Tính offset cho query DB (0-based).
     */
    public int getOffset() {
        return (Math.max(page, 1) - 1) * size;
    }
}
