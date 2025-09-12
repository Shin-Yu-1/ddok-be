package goorm.ddok.reputation.dto.response;

import goorm.ddok.global.dto.AbandonBadgeDto;
import goorm.ddok.global.dto.BadgeDto;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder(toBuilder = true)
public class TemperatureRankResponse {
    private int rank;
    private Long userId;
    private String nickname;
    private String mainPosition;
    private String profileImageUrl;
    private BigDecimal temperature;
    private Long chatRoomId;
    private boolean dmRequestPending;
    private boolean IsMine;
    private BadgeDto mainBadge;
    private AbandonBadgeDto abandonBadge;
    private Instant updatedAt;
}
