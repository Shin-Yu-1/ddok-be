package goorm.ddok.player.dto.reaponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "ProjectPeriodResponse",
        description = "프로젝트 기간 정보",
        example = """
        {
          "start": "2025-06-04",
          "end": "2025-08-12"
        }
        """
)
public class ProjectPeriodResponse {
}
