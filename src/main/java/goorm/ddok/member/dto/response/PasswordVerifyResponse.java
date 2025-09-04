package goorm.ddok.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(
        name = "PasswordVerifResponse",
        description = "비밀번호 인증 및 reauthToken 발급 응답 DTO",
        example = """
    { "reauthToken": "REAUTH_TOKEN_VALUE" }
    """
)
public class PasswordVerifyResponse {
    @Schema(
            description = "재인증 토큰",
            example = "REAUTH_TOKEN_VALUE",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String reauthToken;
}
