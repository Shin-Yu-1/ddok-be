package goorm.ddok.member.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import goorm.ddok.global.dto.LocationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "PlayerProfileMapItemResponse",
        description = """
        플레이어 프로필 아이템 스키마.
        - category=project: projectId, title, teamStatus, location 포함
        - category=study:   studyId,   title, teamStatus, location 포함
        """,
        example = """
        {
          "category": "project",
          "projectId": 1,
          "title": "구지라지 프로젝트",
          "teamStatus": "RECRUITING",
          "location": {
            "address": "부산 해운대구 우동 센텀중앙로 90",
            "region1depthName": "부산",
            "region2depthName": "해운대구",
            "region3depthName": "우동",
            "roadName": "센텀중앙로",
            "mainBuildingNo": "90",
            "subBuildingNo": "",
            "zoneNo": "48058",
            "latitude": 35.1702,
            "longitude": 129.1270
          }
        }
        """
)
public class PlayerProfileMapItemResponse {

    @Schema(
            description = "카테고리",
            example = "project",
            allowableValues = {"project", "study"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String category;

    @Schema(description = "프로젝트 ID (category='project'일 때만 존재)", example = "1", nullable = true)
    private final Long projectId;

    @Schema(description = "스터디 ID (category='study'일 때만 존재)", example = "1", nullable = true)
    private final Long studyId;

    @Schema(description = "제목 (project|study 공통)", example = "구지라지 프로젝트", nullable = true)
    private final String title;

    @Schema(
            description = "팀 상태 (project|study 공통)",
            example = "RECRUITING",
            allowableValues = {"RECRUITING", "ONGOING"},
            nullable = true
    )
    private final String teamStatus;

    @Schema(description = "위치 정보", implementation = LocationDto.class, nullable = true)
    private final LocationDto location;
}
