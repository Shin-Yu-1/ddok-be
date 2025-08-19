package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "SignUpRequest",
        description = "회원가입 요청 DTO",
        requiredProperties = {"email", "username", "password", "passwordCheck", "phoneNumber", "phoneCode"},
        example = """
    {
      "email": "test@test.com",
      "username": "홍길동",
      "password": "1Q2w3e4r!@#",
      "passwordCheck": "1Q2w3e4r!@#",
      "phoneNumber": "01012345678",
      "phoneCode": "828282"
    }
    """
)
public class SignUpRequest {

    @NotBlank(message = "이메일 입력이 누락되었습니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    @Schema(
            description = "사용자 이메일",
            example = "test@test.com",
            type = "string",
            format = "email",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotBlank(message = "이름 입력이 누락되었습니다.")
    @Schema(
            description = "사용자 이름",
            example = "홍길동",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

    @NotBlank(message = "비밀번호 입력이 누락되었습니다.")
    @Schema(
            description = "비밀번호",
            example = "1Q2w3e4r!@#",
            type = "string",
            format = "password",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;

    @NotBlank(message = "비밀번호 확인 입력이 누락되었습니다.")
    @Schema(
            description = "비밀번호 확인",
            example = "1Q2w3e4r!@#",
            type = "string",
            format = "password",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String passwordCheck;

    @NotBlank(message = "전화번호 입력이 누락되었습니다.")
    @Schema(
            description = "전화번호(숫자만, 010으로 시작 · 총 11자리)",
            example = "01012345678",
            pattern = "^010\\d{8}$",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String phoneNumber;

    @NotBlank
    @Schema(
            description = "전화번호 인증 코드(6자리 숫자)",
            example = "828282",
            pattern = "^\\d{6}$",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String phoneCode;
}
