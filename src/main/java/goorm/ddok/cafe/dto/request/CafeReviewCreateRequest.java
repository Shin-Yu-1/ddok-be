package goorm.ddok.cafe.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CafeReviewCreateRequest {

    @NotNull(message = "평점은 필수입니다.")
    // 소수 1자리 제한은 서비스에서 추가 검증(스케일 체크)도 수행
    private BigDecimal rating;

    @Builder.Default
    private List<@NotBlank String> cafeReviewTag = List.of();
}