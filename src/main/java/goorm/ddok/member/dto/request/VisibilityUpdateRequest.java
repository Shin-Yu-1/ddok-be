package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(name = "VisibilityUpdateRequest", description = "프로필 공개/비공개 설정 (참고용)")
public class VisibilityUpdateRequest {
    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublic;
}