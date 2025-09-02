package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@Schema(name = "PositionsUpdateRequest", description = "포지션 수정 요청", example = """
    {
      "mainPosition": "백엔드",
      "subPositions": ["프론트엔드","디자이너"]
    }
    """)
public class PositionsUpdateRequest {
    @NotBlank @Schema(description = "메인 포지션 (필수)", example = "백엔드")
    private String mainPosition;

    @Schema(description = "서브 포지션(최대 2개)", example = "[\"프론트엔드\",\"디자이너\"]")
    private List<String> subPositions; // 최대 2개 (서비스에서 제한)
}