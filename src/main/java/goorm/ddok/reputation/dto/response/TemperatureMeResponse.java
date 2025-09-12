package goorm.ddok.reputation.dto.response;

import goorm.ddok.global.dto.AbandonBadgeDto;
import goorm.ddok.global.dto.BadgeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "내 온도 조회 응답 DTO")
public class TemperatureMeResponse {

    private Long userId;
    private String nickname;
    private BigDecimal temperature;
    private String mainPosition;
    private String profileImageUrl;
    private BadgeDto mainBadge;
    private AbandonBadgeDto abandonBadgeDto;
}
