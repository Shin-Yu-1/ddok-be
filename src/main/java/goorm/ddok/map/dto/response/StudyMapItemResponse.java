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
@Schema(description = "지도용 스터디 항목")
public class StudyMapItemResponse {

    @Schema(description = "카테고리", example = "study")
    private String category;

    @Schema(description = "스터디 ID", example = "1")
    private Long studyId;

    @Schema(description = "스터디명", example = "구지라지 스터디")
    private String title;

    @Schema(description = "스터디 팀 상태", example = "RECRUITING")
    private String teamStatus;

    @Schema(name = "LocationDto", description = "카카오 road_address 매핑 DTO")
    private LocationDto location;
}
