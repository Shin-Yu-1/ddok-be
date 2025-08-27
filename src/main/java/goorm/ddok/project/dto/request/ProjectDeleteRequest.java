package goorm.ddok.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProjectDeleteRequest {

    @Schema(description = "확인 문구(정확히 '삭제합니다.' 여야 함)", example = "삭제합니다.")
    @NotBlank(message = "확인 문구는 필수입니다.")
    private String confirmText;
}