package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "ActiveHoursRequest",
        description = "활동 시간 요청 DTO (24시간 기반, 문자열 HH 형식)",
        requiredProperties = {"start", "end"},
        example = """
    {
      "start": "09",
      "end": "18"
    }
    """
)
public class ActiveHoursRequest {

    @Schema(
            description = "시작 시간 (00~24, 두 자리 문자열)",
            example = "09",
            pattern = "^(?:[01]\\d|2[0-4])$",
            minLength = 2,
            maxLength = 2,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "시작 시간은 필수 입력 값입니다.")
    @Pattern(regexp = "^(?:[01]\\d|2[0-4])$", message = "시간은 00~24 사이의 값이어야 합니다.")
    private String start;

    @Schema(
            description = "종료 시간 (00~24, 두 자리 문자열)",
            example = "18",
            pattern = "^(?:[01]\\d|2[0-4])$",
            minLength = 2,
            maxLength = 2,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "종료 시간은 필수 입력 값입니다.")
    @Pattern(regexp = "^(?:[01]\\d|2[0-4])$", message = "시간은 00~24 사이의 값이어야 합니다.")
    private String end;
}
