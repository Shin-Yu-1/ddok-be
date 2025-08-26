package goorm.ddok.cafe.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "카페 후기 통계 응답 데이터")
public record CafeStatsResponse(
        @Schema(description = "카페 ID", example = "1")
        Long cafeId,
        @Schema(description = "카페명", example = "구지라지 카페")
        String title,
        @Schema(description = "후기 개수", example = "193")
        long reviewCount,
        @ArraySchema(arraySchema = @Schema(description = "태그 통계 목록"))
        List<TagCountResponse> cafeReviewTag,
        @Schema(description = "평균 평점(소수점 1자리, 반올림)", example = "3.9")
        BigDecimal totalRating
) {}