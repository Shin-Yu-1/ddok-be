package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(name = "PhoneUpdateRequest", example = """
{ "phoneNumber": "01012345678" }
""")
public class PhoneUpdateRequest {
    @NotBlank(message = "전화번호는 비어 있을 수 없습니다.")
    @Pattern(regexp = "01[016789]\\d{7,8}", message = "유효한 휴대폰 번호 형식이 아닙니다.")
    private String phoneNumber;
}