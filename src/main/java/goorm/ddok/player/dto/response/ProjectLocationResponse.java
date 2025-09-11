package goorm.ddok.player.dto.response;

import goorm.ddok.project.domain.ProjectRecruitment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "ProjectLocationResponse",
        description = "프로젝트 위치 DTO",
        example = """
        {
          "address": "전북 익산시",
          "region1depthName": "전북",
          "region2depthName": "익산시",
          "region3depthName": "부송동",
          "roadName": "망산길",
          "mainBuildingNo": "11",
          "subBuildingNo": "17",
          "zoneNo": "54547",
          "latitude": 35.976749396987046,
          "longitude": 126.99599512792346
        }
        """
)
public class ProjectLocationResponse {
    private String address;
    private String region1depthName;
    private String region2depthName;
    private String region3depthName;
    private String roadName;
    private String mainBuildingNo;
    private String subBuildingNo;
    private String zoneNo;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public static ProjectLocationResponse from(ProjectRecruitment project) {
        String region1 = project.getRegion1depthName() != null ? project.getRegion1depthName() : "";
        String region2 = project.getRegion2depthName() != null ? project.getRegion2depthName() : "";
        String address = normalizeRegion1(region1) + " " + region2;

        return ProjectLocationResponse.builder()
                .address(address)
                .region1depthName(project.getRegion1depthName())
                .region2depthName(project.getRegion2depthName())
                .region3depthName(project.getRegion3depthName())
                .roadName(project.getRoadName())
                .mainBuildingNo(project.getMainBuildingNo())
                .subBuildingNo(project.getSubBuildingNo())
                .zoneNo(project.getZoneNo())
                .latitude(project.getLatitude())
                .longitude(project.getLongitude())
                .build();
    }

    private static String normalizeRegion1(String region1) {
        return region1.replace("특별시", "")
                .replace("광역시", "")
                .replace("자치시", "")
                .replace("도", "")
                .trim();
    }

}
