package goorm.ddok.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(
        name = "TokenResponse",
        description = "액세스 토큰 재발급 응답 DTO",
        example = """
    { "accessToken": "eyJhbGciOi..." }
    """
)
public class TokenResponse {

    @Schema(
            description = "재발급된 액세스 토큰 (JWT)",
            example = "eyJhbGciOi...",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String accessToken;
}
