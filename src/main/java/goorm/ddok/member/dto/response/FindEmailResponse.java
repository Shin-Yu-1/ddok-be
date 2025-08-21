package goorm.ddok.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "FindEmailResponse",
        description = "아이디(이메일) 찾기 응답 DTO",
        example = """
    { "email": "test@test.com" }
    """
)
public record FindEmailResponse(
        @Schema(
                description = "이메일",
                example = "test@test.com",
                type = "string",
                format = "email",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String email
) {}
