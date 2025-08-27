package goorm.ddok.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import goorm.ddok.global.dto.AbandonBadgeDto;
import goorm.ddok.global.dto.BadgeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "프로젝트 사용자 요약 정보 DTO")
public class ProjectUserSummaryDto {
    @Schema(description = "사용자 ID", example = "101")
    private Long userId;

    @Schema(description = "닉네임", example = "개구라")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/images/user101.png")
    private String profileImageUrl;

    @Schema(description = "대표 포지션", example = "풀스택")
    private String mainPosition;

    @Schema(description = "메인 배지")
    private BadgeDto mainBadge;

    @Schema(description = "탈주 배지")
    private AbandonBadgeDto abandonBadge;

    @Schema(description = "온도", example = "36.5")
    private Double temperature;

    @Schema(description = "담당 포지션", example = "백엔드")
    private String decidedPosition;

    @JsonProperty("isMine")
    @Schema(description = "현재 사용자 본인 여부", example = "true")
    private boolean isMine;

    @Schema(description = "채팅방 ID", example = "null")
    private Long chatRoomId;

    @Schema(description = "DM 요청 대기 여부", example = "false")
    private boolean dmRequestPending;
}
