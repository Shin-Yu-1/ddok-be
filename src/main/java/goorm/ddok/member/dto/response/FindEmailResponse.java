package goorm.ddok.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "아이디(이메일) 찾기 응답 DTO")
public record FindEmailResponse(@Schema(description = "이메일", example = "test@test.com") String email) {
}
