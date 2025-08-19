package goorm.ddok.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Schema(
        name = "SignInResponse",
        description = "로그인 응답 DTO",
        example = """
    {
      "accessToken": "eyJhbGciOi...",
      "user": {
        "id": 1,
        "username": "홍길동",
        "email": "test@test.com",
        "nickname": "고통스러운 개발자",
        "profileImageUrl": "https://cdn.pixabay.com/photo/2013/07/12/14/15/boy-148071_1280.png",
        "mainPosition": "백엔드",
        "IsPreference": true,
        "location": {
          "latitude": 37.5665,
          "longitude": 126.9780,
          "address": "서울특별시 강남구 테헤란로 123"
        }
      }
    }
    """
)
@AllArgsConstructor
public class SignInResponse {

    @Schema(
            description = "Access Token (JWT)",
            example = "eyJhbGciOi...",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String accessToken;

    @Schema(
            description = "로그인 사용자 정보",
            implementation = SignInUserResponse.class,
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private SignInUserResponse user;
}
