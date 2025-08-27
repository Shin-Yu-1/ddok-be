package goorm.ddok.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ProjectDeleteRequest", description = "프로젝트 삭제 요청 DTO")
public class ProjectDeleteRequest {

    @NotBlank(message = "확인 문구는 필수입니다.")
    @Schema(description = "정확히 '삭제합니다.' 이어야 삭제 진행", example = "삭제합니다.")
    private String confirmText;
}