package goorm.ddok.team.dto.response;

import goorm.ddok.global.dto.AbandonBadgeDto;
import goorm.ddok.global.dto.BadgeDto;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.domain.UserPosition;
import goorm.ddok.member.domain.UserPositionType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamApplicantUserResponse {

    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private BigDecimal temperature;
    private String mainPosition;
    private Long chatRoomId;
    private boolean dmRequestPending;
    private BadgeDto mainBadge;
    private AbandonBadgeDto abandonBadge;


    public static TeamApplicantUserResponse from(User user) {
        return TeamApplicantUserResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .temperature(user.getReputation().getTemperature())
                .mainPosition(resolvePrimaryPosition(user))
                .chatRoomId(null) // DM/채팅 기능 붙을 때 매핑
                .dmRequestPending(false) // 기본값, 후속 로직에서 세팅
                .mainBadge(BadgeDto.from(user.getMainBadge()))
                .abandonBadge(AbandonBadgeDto.from(user.getAbandonBadge()))
                .build();
    }

    private static String resolvePrimaryPosition(User user) {
        return user.getPositions().stream()
                .filter(pos -> pos.getType() == UserPositionType.PRIMARY)
                .map(UserPosition::getPositionName)
                .findFirst()
                .orElse(null);
    }
}
