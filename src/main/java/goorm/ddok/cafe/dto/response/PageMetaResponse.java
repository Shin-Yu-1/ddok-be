package goorm.ddok.cafe.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "페이지네이션 메타 정보")
public record PageMetaResponse(
        @Schema(description = "현재 페이지(0부터 시작)", example = "0")
        int currentPage,
        @Schema(description = "페이지 크기", example = "4")
        int pageSize,
        @Schema(description = "전체 페이지 수", example = "1")
        int totalPages,
        @Schema(description = "전체 아이템 수", example = "2")
        long totalItems
) {}