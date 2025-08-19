package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "활동 시간 요청 DTO")
public class ActiveHoursRequest {

    @Schema(description = "시작 시간", example = "09")
    @NotBlank(message = "시작 시간은 필수 입력 값입니다.")
    @Pattern(regexp = "^(?:[01]\\d|2[0-4])$", message = "시간은 00~24 사이의 값이어야 합니다.")
    private String start;

    @Schema(description = "종료 시간", example = "18")
    @NotBlank(message = "종료 시간은 필수 입력 값입니다.")
    @Pattern(regexp = "^(?:[01]\\d|2[0-3])$", message = "시간은 00~24 사이의 값이어야 합니다.")
    private String end;
}
