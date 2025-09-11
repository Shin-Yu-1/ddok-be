package goorm.ddok.cafe.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CafeReviewCreateResponse {
    private Long userId;
    private Long reviewId;

    private String title;              // 카페명
    private String nickname;           // 작성자 닉네임
    private String profileImageUrl;    // 작성자 프로필

    private BigDecimal rating;
    private Boolean isMine;

    private List<String> cafeReviewTag;

    private Instant createdAt;
    private Instant updatedAt;
}