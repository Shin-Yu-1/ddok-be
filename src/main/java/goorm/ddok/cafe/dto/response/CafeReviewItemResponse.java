// goorm/ddok/cafe/dto/response/CafeReviewItemResponse.java
package goorm.ddok.cafe.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(description = "카페 개별 후기")
public record CafeReviewItemResponse(
        @Schema(description = "작성자 ID", example = "1") Long userId,
        @Schema(description = "작성자 닉네임", example = "고라니") String nickname,
        @Schema(description = "작성자 프로필 이미지 URL", example = "https://img.cdn/profiles/1.png") String profileImageUrl,
        @Schema(description = "평점(1.0~5.0)", example = "4.5") BigDecimal rating,
        @ArraySchema(arraySchema = @Schema(description = "후기 태그 목록"), schema = @Schema(example = "분위기가 좋아요"))
        List<String> cafeReviewTag,
        @Schema(description = "작성 시각(ISO-8601)", example = "2025-08-14T10:12:30") Instant createdAt,
        @Schema(description = "수정 시각(ISO-8601)", example = "2025-08-22T09:05:11") Instant updatedAt
) {}