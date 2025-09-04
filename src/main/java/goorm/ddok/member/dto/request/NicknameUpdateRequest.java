package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(name = "NicknameUpdateRequest", example = """
{ "nickname": "박성진 보고싶다" }
""")
public class NicknameUpdateRequest {
    @NotBlank(message = "닉네임은 비어 있을 수 없습니다.")
    @Size(min = 1, max = 12, message = "닉네임은 1~12자여야 합니다.")
    private String nickname;
}