package goorm.ddok.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "ActiveHoursResponse",
        description = "활동 시간 응답 DTO",
        example = """
    {
      "start": "09",
      "end": "18"
    }
    """
)
public class ActiveHoursResponse {

    @Schema(
            description = "시작 시간 (00~24, 두 자리 문자열)",
            example = "09",
            pattern = "^(?:[01]\\d|2[0-4])$",
            minLength = 2,
            maxLength = 2,
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String start;

    @Schema(
            description = "종료 시간 (00~24, 두 자리 문자열)",
            example = "18",
            pattern = "^(?:[01]\\d|2[0-4])$",
            minLength = 2,
            maxLength = 2,
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String end;
}
