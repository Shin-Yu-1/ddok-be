package goorm.ddok.team.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(
        name = "TeamCountResponse",
        description = "모집 중 프로젝트/스터디 + 진행 중 팀 카운트 응답 DTO"
)
public class TeamCountResponse {

    @Schema(description = "모집 중인 프로젝트 팀 수", example = "3")
    private Long projectCount;

    @Schema(description = "모집 중인 스터디 팀 수", example = "2")
    private Long studyCount;

    @Schema(description = "진행 중인 팀 수", example = "5")
    private Long teamCount;
}
