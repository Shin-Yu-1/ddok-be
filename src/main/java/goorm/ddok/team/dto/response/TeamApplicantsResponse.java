package goorm.ddok.team.dto.response;

import goorm.ddok.chat.dto.response.PaginationResponse;
import goorm.ddok.team.domain.TeamType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "TeamApplicantsResponse",
        description = "팀 참여 희망자 목록 조회 응답 DTO"
)
public class TeamApplicantsResponse {

    @Schema(description = "페이지네이션 정보", implementation = PaginationResponse.class)
    private PaginationResponse pagination;

    @Schema(description = "팀 ID", example = "2")
    private Long teamId;

    @Schema(description = "모집글 ID", example = "33")
    private Long recruitmentId;

    @Schema(description = "현재 로그인한 사용자가 리더인지 여부", example = "true")
    private boolean IsLeader;

    @Schema(description = "팀 타입 (PROJECT / STUDY)", example = "PROJECT")
    private TeamType teamType;

    @Schema(description = "참여 희망자 목록", implementation = TeamApplicantResponse.class)
    private List<TeamApplicantResponse> items;
}
