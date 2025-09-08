package goorm.ddok.team.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "TeamMemberResponse",
        description = "팀 참여 확정자 단일 응답 DTO"
)
public class TeamMemberResponse {

    @Schema(description = "팀 멤버 ID", example = "201")
    private Long memberId;

    @Schema(description = "확정된 포지션명 (Project일 때만 존재, Study는 null)", example = "프론트엔드")
    private String decidedPosition;

    @Schema(description = "팀 내 역할 (LEADER / MEMBER)", example = "LEADER")
    private String role;

    @Schema(description = "팀에 가입된 시각 (승인 시각 또는 팀 생성 시각)", example = "2025-09-07T19:40:00")
    private Instant joinedAt;

    @Schema(description = "해당 멤버가 현재 로그인한 나 자신인지 여부", example = "false")
    private boolean IsMine;

    @Schema(description = "팀 멤버 사용자 정보 (닉네임, 프로필, 뱃지 등)", implementation = TeamApplicantUserResponse.class)
    private TeamApplicantUserResponse user;
}
