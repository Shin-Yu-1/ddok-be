package goorm.ddok.team.dto.response;

import goorm.ddok.team.domain.ApplicantStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "TeamApplicantResponse",
        description = "팀 참여 희망자 단일 응답 DTO"
)
public class TeamApplicantResponse {

    @Schema(description = "참여 신청 ID", example = "101")
    private Long applicantId;

    @Schema(description = "지원한 포지션 (Study의 경우 null)", example = "백엔드")
    private String appliedPosition;

    @Schema(description = "신청 상태 (PENDING / APPROVED / REJECTED)", example = "PENDING")
    private ApplicantStatus status;

    @Schema(description = "신청 시각", example = "2025-09-07T19:40:00")
    private Instant appliedAt;

    @Schema(description = "현재 로그인한 사용자의 신청인지 여부", example = "true")
    private boolean IsMine;

    @Schema(description = "지원자 사용자 정보", implementation = TeamApplicantUserResponse.class)
    private TeamApplicantUserResponse user;
}
