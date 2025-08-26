package goorm.ddok.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "PreferredAgesDto", description = "선호 연령대")
public class PreferredAgesDto {
    @NotNull(message = "최소 연령은 필수 입력값입니다.")
    @Schema(description = "최소 연령", example = "20")
    private Integer ageMin;

    @NotNull(message = "최대 연령은 필수 입력값입니다.")
    @Schema(description = "최대 연령", example = "30")
    private Integer ageMax;
}