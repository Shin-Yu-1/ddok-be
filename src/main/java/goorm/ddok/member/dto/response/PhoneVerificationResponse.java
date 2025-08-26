package goorm.ddok.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(
        name = "PhoneVerificationResponse",
        description = "휴대폰 인증 발송 응답 DTO",
        example = """
    { "expiresIn": 180 }
    """
)
public class PhoneVerificationResponse {

    @Schema(
            description = "인증 유효 시간(초)",
            example = "180",
            type = "integer",
            format = "int32",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private int expiresIn;
}
