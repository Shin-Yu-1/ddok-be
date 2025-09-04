package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "PasswordVerifyRequest",
        description = "비밀번호 인증 요청 DTO",
        requiredProperties = "password",
        example = """
    {
      "password": "1Q2w3e4r!@#"
    }
    """
)
public class PasswordVerifyRequest {
    @NotBlank(message = "비밀번호 입력이 누락되었습니다.")
    @Schema(
            description = "비밀번호",
            example = "1Q2w3e4r!@#",
            type = "string",
            format = "password",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Size(min = 8, max = 72)
    private String password;
}
