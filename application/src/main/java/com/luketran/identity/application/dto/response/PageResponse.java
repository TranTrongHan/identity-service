package com.luketran.identity.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Response phân trang")
public class PageResponse<T> {

    @Schema(description = "Danh sách items trong trang hiện tại")
    private List<T> items;

    @Schema(description = "Tổng số records", example = "150")
    private long total;

    @Schema(description = "Trang hiện tại (bắt đầu từ 1)", example = "1")
    private int page;

    @Schema(description = "Kích thước trang", example = "10")
    private int size;

    @Schema(description = "Tổng số trang", example = "15")
    private int totalPages;

    public static <T> PageResponse<T> of(List<T> items, long total, int page, int size) {
        PageResponse<T> response = new PageResponse<>();
        response.setItems(items);
        response.setTotal(total);
        response.setPage(page);
        response.setSize(size);
        response.setTotalPages(size > 0 ? (int) Math.ceil((double) total / size) : 0);
        return response;
    }
}
