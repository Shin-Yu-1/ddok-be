package goorm.ddok.cafe.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CafeStatsResponse(
        Long cafeId,
        String title,
        long reviewCount,
        List<TagCountResponse> cafeReviewTag,
        BigDecimal totalRating
) {}