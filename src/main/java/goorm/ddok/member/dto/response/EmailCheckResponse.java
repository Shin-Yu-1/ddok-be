package goorm.ddok.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(
        name = "EmailCheckResponse",
        description = "이메일 중복 확인 응답 DTO",
        example = """
    {
      "IsAvailable": true
    }
    """
)
public class EmailCheckResponse {

    @Schema(
            description = "가입 가능 여부",
            example = "true",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private boolean IsAvailable;
}
