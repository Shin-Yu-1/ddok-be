package goorm.ddok.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Schema(description = "로그인 응답 DTO")
@AllArgsConstructor
public class SignInResponse {

    @Schema(description = "Access Token (JWT)", example =  "eyJhbGciOi...")
    private String accessToken;

    @Schema(description = "로그인 사용자 정보")
    private SignInUserResponse user;
}
