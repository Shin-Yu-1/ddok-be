package goorm.ddok.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "PreferredAgesDto", description = "선호 연령대")
public class PreferredAgesDto {
    @Schema(description = "최소 연령", example = "20")
    private Integer ageMin;

    @Schema(description = "최대 연령", example = "30")
    private Integer ageMax;
}