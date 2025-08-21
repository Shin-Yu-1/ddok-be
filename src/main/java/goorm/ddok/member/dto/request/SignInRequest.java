package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(
        name = "SignInRequest",
        description = "로그인 요청 DTO",
        requiredProperties = {"email", "password"},
        example = """
    {
      "email": "test@test.com",
      "password": "1Q2w3e4r!@#"
    }
    """
)
public class SignInRequest {

    @NotBlank
    @Email
    @Schema(
            description = "가입한 이메일",
            example = "test@test.com",
            type = "string",
            format = "email",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotBlank
    @Schema(
            description = "비밀번호",
            example = "1Q2w3e4r!@#",
            type = "string",
            format = "password", // Swagger UI에서 비밀번호 입력 필드로 렌더링
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;
}
