package goorm.ddok.member.dto.request;

import goorm.ddok.member.domain.AuthType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "PhoneVerificationRequest",
        description = "휴대폰 번호 인증 발송 DTO",
        requiredProperties = {"phoneNumber", "username", "authType"},
        example = """
    {
      "phoneNumber": "01012345678",
      "username": "홍길동",
      "authType": "SIGN_UP"
    }
    """
)
public class PhoneVerificationRequest {

    @NotBlank(message = "전화번호 입력이 누락되었습니다.")
    @Schema(
            description = "전화번호(010 시작, 숫자 11자리)",
            example = "01012345678",
            pattern = "^010\\d{8}$",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String phoneNumber;

    @NotBlank(message = "이름 입력이 누락되었습니다.")
    @Schema(
            description = "사용자 이름",
            example = "홍길동",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

    @NotNull(message = "인증 타입이 누락되었습니다.")
    @Schema(
            description = "인증 타입",
            implementation = AuthType.class,
            allowableValues = {"SIGN_UP", "FIND_ID", "FIND_PASSWORD"},
            example = "SIGN_UP",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private AuthType authType;
}
