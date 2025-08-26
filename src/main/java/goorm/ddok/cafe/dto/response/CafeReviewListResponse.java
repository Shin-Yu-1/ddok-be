package goorm.ddok.cafe.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "카페 후기 리스트 조회 결과")
public record CafeReviewListResponse(
        @Schema(description = "카페 ID", example = "1") Long cafeId,
        @Schema(description = "카페명", example = "구지라지 카페") String title,
        PageMetaResponse pagination,
        List<CafeReviewItemResponse> cafeReviews
) {}