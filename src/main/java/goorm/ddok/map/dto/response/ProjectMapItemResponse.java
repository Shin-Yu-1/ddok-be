package goorm.ddok.map.dto.response;

import goorm.ddok.global.dto.LocationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "지도용 프로젝트 항목")
public class ProjectMapItemResponse {

    @Schema(description = "카테고리", example = "project")
    private String category;

    @Schema(description = "프로젝트 ID", example = "1")
    private Long projectId;

    @Schema(description = "프로젝트명", example = "구지라지 프로젝트")
    private String title;

    @Schema(description = "프로젝트 팀 상태", example = "RECRUITING")
    private String teamStatus;

    @Schema(name = "LocationDto", description = "카카오 road_address 매핑 DTO")
    private LocationDto location;

}
