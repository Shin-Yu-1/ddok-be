package goorm.ddok.player.dto.response;

import goorm.ddok.study.domain.StudyRecruitment;
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
        name = "StudyLocationResponse",
        description = "스터디 위치 DTO (LocationDto 통일)",
        example = """
        {
          "address": "전북 익산시 부송동 망산길 11-17",
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
public class StudyLocationResponse {

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

    public static StudyLocationResponse from(StudyRecruitment study) {
        // 전체 주소 문자열 합성
        StringBuilder sb = new StringBuilder();
        if (study.getRegion1depthName() != null) sb.append(study.getRegion1depthName()).append(" ");
        if (study.getRegion2depthName() != null) sb.append(study.getRegion2depthName()).append(" ");
        if (study.getRegion3depthName() != null) sb.append(study.getRegion3depthName()).append(" ");
        if (study.getRoadName() != null) sb.append(study.getRoadName()).append(" ");
        if (study.getMainBuildingNo() != null) {
            sb.append(study.getMainBuildingNo());
            if (study.getSubBuildingNo() != null && !study.getSubBuildingNo().isBlank()) {
                sb.append("-").append(study.getSubBuildingNo());
            }
        }
        String fullAddress = sb.toString().trim().replaceAll("\\s+", " ");

        return StudyLocationResponse.builder()
                .address(fullAddress.isEmpty() ? null : fullAddress)
                .region1depthName(study.getRegion1depthName())
                .region2depthName(study.getRegion2depthName())
                .region3depthName(study.getRegion3depthName())
                .roadName(study.getRoadName())
                .mainBuildingNo(study.getMainBuildingNo())
                .subBuildingNo(study.getSubBuildingNo())
                .zoneNo(study.getZoneNo())
                .latitude(study.getLatitude())
                .longitude(study.getLongitude())
                .build();
    }
}