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
        description = "진행 중 팀 카운트 응답 DTO"
)
public class TeamCountResponse {
    @Schema(description = "진행 중인 팀 수", example = "5")
    private Long count;
}
