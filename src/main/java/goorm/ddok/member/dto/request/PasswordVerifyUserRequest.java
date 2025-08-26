package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "PasswordVerifyUserRequest",
        description = "비밀번호 찾기 본인 인증 및 reauthToken 발급 DTO",
        requiredProperties = {"username", "email", "phoneNumber", "phoneCode"},
        example = """
    {
      "username": "홍길동",
      "email": "test@test.com",
      "phoneNumber": "01012345678",
      "phoneCode": "012345"
    }
    """
)
public class PasswordVerifyUserRequest {

    @NotBlank(message = "이름 입력이 누락되었습니다.")
    @Schema(
            description = "사용자 이름",
            example = "홍길동",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

    @NotBlank(message = "이메일 입력이 누락되었습니다.")
    @Email
    @Schema(
            description = "가입한 이메일",
            example = "test@test.com",
            type = "string",
            format = "email",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotBlank(message = "전화번호 입력이 누락되었습니다.")
    @Pattern(regexp = "^01[0-9]\\d{7,8}$", message = "올바른 형식의 전화번호여야 합니다.")
    @Schema(
            description = "전화번호(010 시작, 숫자 10~11자리)",
            example = "01012345678",
            pattern = "^01[0-9]\\d{7,8}$",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String phoneNumber;

    @NotBlank(message = "인증번호 입력이 누락되었습니다.")
    @Pattern(regexp = "^\\d{6}$", message = "인증번호는 숫자 6자리여야 합니다.")
    @Schema(
            description = "인증번호(숫자 6자리)",
            example = "012345",
            pattern = "^\\d{6}$",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String phoneCode;
}
