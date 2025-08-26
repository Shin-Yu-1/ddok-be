package goorm.ddok.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(
        name = "PhoneVerifyCodeResponse",
        description = "휴대폰 인증 코드 응답 DTO",
        example = """
    { "verified": true }
    """
)
public class PhoneVerifyCodeResponse {

    @Schema(
            description = "인증 확인 여부",
            example = "true",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private boolean verified;
}
