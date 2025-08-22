package goorm.ddok.cafe.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CafeReviewItemResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        BigDecimal rating,
        List<String> cafeReviewTag,
        Instant createdAt,
        Instant updatedAt
) {}