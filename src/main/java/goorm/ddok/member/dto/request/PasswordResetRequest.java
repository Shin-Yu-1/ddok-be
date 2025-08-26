package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "PasswordResetRequest",
        description = "비밀번호 변경 요청 DTO",
        requiredProperties = {"newPassword", "passwordCheck"},
        example = """
    {
      "newPassword": "!@#123qweR",
      "passwordCheck": "!@#123qweR"
    }
    """
)
public class PasswordResetRequest {

    @NotBlank(message = "비밀번호 입력이 누락되었습니다.")
    @Schema(
            description = "새로운 비밀번호",
            example = "!@#123qweR",
            type = "string",
            format = "password",
            // 문서화용 패턴(서버 검증은 아님). 필요 시 실제 검증은 @Pattern 추가
            pattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^\\w\\s]).{8,64}$",
            minLength = 8,
            maxLength = 64,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String newPassword;

    @NotBlank(message = "비밀번호 확인 입력이 누락되었습니다.")
    @Schema(
            description = "새로운 비밀번호 확인",
            example = "!@#123qweR",
            type = "string",
            format = "password",
            minLength = 8,
            maxLength = 64,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String passwordCheck;
}
