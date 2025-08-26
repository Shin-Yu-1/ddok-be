package goorm.ddok.cafe.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지도용 카페 항목")
public record CafeMapItemResponse(
        @Schema(description = "카테고리", example = "cafe")
        String category,
        @Schema(description = "카페 ID", example = "1")
        Long cafeId,
        @Schema(description = "카페명", example = "구지라지 카페")
        String title,
        CafeLocationResponse location
) {}