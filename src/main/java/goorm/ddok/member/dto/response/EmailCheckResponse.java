package goorm.ddok.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "이메일 중복 확인 응답 DTO")
public class EmailCheckResponse {

    @Schema(description = "가입 가능 여부", example = "true")
    private boolean IsAvailable;
}
