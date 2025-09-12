package goorm.ddok.reputation.dto.response;

import goorm.ddok.global.dto.AbandonBadgeDto;
import goorm.ddok.global.dto.BadgeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder(toBuilder = true)
@Schema(description = "지역별 온도 조회 응답 DTO")
public class TemperatureRegionResponse {
    private String region;
    private Long userId;
    private String nickname;
    private BigDecimal temperature;
    private String mainPosition;
    private String profileImageUrl;
    private Long chatRoomId;
    private boolean dmRequestPending;
    private boolean IsMine;
    private BadgeDto mainBadge;
    private AbandonBadgeDto abandonBadge;
    private Instant updatedAt;
}
