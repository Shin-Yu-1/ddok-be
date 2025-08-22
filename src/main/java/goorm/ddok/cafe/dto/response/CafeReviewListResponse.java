package goorm.ddok.cafe.dto.response;

import java.util.List;

public record CafeReviewListResponse(
        Long cafeId,
        String title,
        PageMetaResponse pagination,
        List<CafeReviewItemResponse> cafeReviews
) {}