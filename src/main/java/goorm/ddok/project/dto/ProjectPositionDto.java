package goorm.ddok.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectPositionDto {
    @Schema(description = "포지션명", example = "PM")
    private String position;

    @Schema(description = "지원자 수", example = "3")
    private Integer applied;

    @Schema(description = "확정자 수", example = "2")
    private Integer confirmed;

    @Schema(description = "현재 사용자가 이 포지션에 지원했는지 여부", example = "false")
    private boolean IsApplied;

    @Schema(description = "현재 사용자가 이 포지션에 확정되었는지 여부", example = "false")
    private boolean IsApproved;

    @Schema(description = "아직 자리가 남아있는지 여부", example = "false")
    private boolean IsAvailable;
}
