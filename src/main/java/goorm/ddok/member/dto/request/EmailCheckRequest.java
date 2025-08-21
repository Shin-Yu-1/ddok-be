package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "EmailCheckRequest",
        description = "이메일 중복 확인 요청 DTO",
        requiredProperties = {"email"},
        example = """
    {
      "email": "test@test.com"
    }
    """
)
public class EmailCheckRequest {

    @NotBlank
    @Email
    @Schema(
            description = "가입할 이메일",
            example = "test@test.com",
            type = "string",
            format = "email",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;
}
