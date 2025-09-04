package goorm.ddok.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(
        name = "ProjectDeleteRequest",
        description = "프로젝트 삭제 확인 요청",
        example = """
        {
          "confirmText": "삭제합니다."
        }
        """
)
public class ProjectDeleteRequest {

    @NotBlank(message = "확인 문구는 필수입니다.")
    @Pattern(regexp = "^삭제합니다\\.$", message = "확인 문구가 올바르지 않습니다.")
    @Schema(description = "삭제 확인 문구 (정확히 '삭제합니다.' 여야 함)", example = "삭제합니다.")
    private String confirmText;
}