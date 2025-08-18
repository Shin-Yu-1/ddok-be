package goorm.ddok.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "비밀번호 찾기 본인 인증 및 reauthToken 발급 응답 DTO")
public class PasswordVerifyUserResponse {
    @Schema(description = "재인증 토큰", example = "REAUTH_TOKEN_VALUE")
    private String reauthToken;
}
