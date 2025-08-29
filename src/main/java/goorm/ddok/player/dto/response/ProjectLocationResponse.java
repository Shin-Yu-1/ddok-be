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
          "latitude": 37.5665,
          "longitude": 126.9780,
          "address": "서울특별시 강남구 테헤란로…"
        }
        """
)
public class ProjectLocationResponse {
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;

    public static ProjectLocationResponse from(ProjectRecruitment project) {
        String address = String.join(" ",
                project.getRegion1depthName() != null ? project.getRegion1depthName() : "",
                project.getRegion2depthName() != null ? project.getRegion2depthName() : "",
                project.getRegion3depthName() != null ? project.getRegion3depthName() : "",
                project.getRoadName() != null ? project.getRoadName() : ""
        ).trim();

        return ProjectLocationResponse.builder()
                .latitude(project.getLatitude())
                .longitude(project.getLongitude())
                .address(address.isEmpty() ? null : address)
                .build();
    }

}
