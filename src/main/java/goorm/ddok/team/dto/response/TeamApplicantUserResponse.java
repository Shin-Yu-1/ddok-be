package goorm.ddok.team.dto.response;

import goorm.ddok.global.dto.AbandonBadgeDto;
import goorm.ddok.global.dto.BadgeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "TeamApplicantUserResponse",
        description = "팀 신청자 사용자 정보 응답 DTO"
)
public class TeamApplicantUserResponse {

    @Schema(description = "사용자 ID", example = "11")
    private Long userId;

    @Schema(description = "닉네임", example = "배고픈 디자이너")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profiles/user1.png")
    private String profileImageUrl;

    @Schema(description = "사용자 온도 (평판 지수)", example = "36.5")
    private BigDecimal temperature;

    @Schema(description = "메인 포지션명", example = "프론트엔드")
    private String mainPosition;

    @Schema(description = "DM 채팅방 ID (없으면 null)", example = "101")
    private Long chatRoomId;

    @Schema(description = "DM 요청 대기 상태 여부", example = "false")
    private boolean dmRequestPending;

    @Schema(description = "대표 착한 배지", implementation = BadgeDto.class)
    private BadgeDto mainBadge;

    @Schema(description = "탈주 배지", implementation = AbandonBadgeDto.class)
    private AbandonBadgeDto abandonBadge;
}
