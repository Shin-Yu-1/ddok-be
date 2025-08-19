package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "PhoneVerifyCodeRequest",
        description = "휴대폰 인증 코드 요청 DTO",
        requiredProperties = {"phoneNumber", "phoneCode"},
        example = """
    {
      "phoneNumber": "01012345678",
      "phoneCode": "123456"
    }
    """
)
public class PhoneVerifyCodeRequest {

    @NotBlank(message = "전화번호는 필수 입력입니다.")
    @Pattern(regexp = "^01[0-9]\\d{7,8}$", message = "올바른 형식의 전화번호여야 합니다.")
    @Schema(
            description = "전화번호(010 시작, 숫자 10~11자리)",
            example = "01012345678",
            pattern = "^01[0-9]\\d{7,8}$",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String phoneNumber;

    @NotBlank(message = "인증번호는 필수 입력입니다.")
    @Pattern(regexp = "^\\d{6}$", message = "인증번호는 숫자 6자리여야 합니다.")
    @Schema(
            description = "인증번호(숫자 6자리)",
            example = "123456",
            pattern = "^\\d{6}$",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String phoneCode;
}
