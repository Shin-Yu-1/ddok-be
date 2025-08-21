package goorm.ddok.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(
        name = "SignUpResponse",
        description = "회원가입 응답 DTO",
        example = """
    {
      "id": 1,
      "username": "홍길동"
    }
    """
)
public class SignUpResponse {

    @Schema(
            description = "사용자 id",
            example = "1",
            type = "integer",
            format = "int64",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Schema(
            description = "사용자 실명",
            example = "홍길동",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String username;
}
