package goorm.ddok.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Schema(description = "페이지네이션 응답 DTO")
public class PageResponse<T> {

    @Schema(description = "현재 페이지", example = "0")
    private final int currentPage;

    @Schema(description = "페이지 크기", example = "10")
    private final int pageSize;

    @Schema(description = "전체 페이지 수", example = "1")
    private final int totalPages;

    @Schema(description = "전체 아이템 수", example = "3")
    private final long totalItems;

    @Schema(description = "조회 데이터 목록")
    private final List<T> items;

    private PageResponse(Page<T> page) {
        this.currentPage = page.getNumber();
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.totalItems = page.getTotalElements();
        this.items = page.getContent();
    }

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page);
    }
}
