package goorm.ddok.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "회원가입 응답 DTO")
public class SignUpResponse {

    @Schema(description = "사용자 id", example = "1")
    private Long id;

    @Schema(description = "사용자 실명", example = "홍길동")
    private String username;

}
