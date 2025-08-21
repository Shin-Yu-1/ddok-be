package goorm.ddok.member.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "PreferenceDetailResponse",
        description = "개인화 상세 응답 DTO",
        example = """
    {
      "mainPosition": "백엔드",
      "subPosition": ["프론트엔드", "데브옵스"],
      "techStacks": ["Java", "Spring Boot", "React"],
      "location": { "latitude": 37.5665, "longitude": 126.9780, "address": "서울특별시 강남구 테헤란로 123" },
      "traits": ["협업", "문제 해결"],
      "birthDate": "1997-10-10",
      "activeHours": { "start": "19", "end": "23" }
    }
    """
)
public class PreferenceDetailResponse {

    @Schema(description = "대표 포지션", example = "백엔드", accessMode = Schema.AccessMode.READ_ONLY)
    private String mainPosition;

    @ArraySchema(
            minItems = 2,
            maxItems = 2,
            arraySchema = @Schema(description = "관심 포지션 (2개)"),
            schema = @Schema(description = "관심 포지션 항목", example = "프론트엔드")
    )
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private List<String> subPosition;

    @ArraySchema(
            arraySchema = @Schema(description = "기술 스택"),
            schema = @Schema(description = "기술 스택 항목", example = "Spring Boot")
    )
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private List<String> techStacks;

    @Schema(description = "위치 정보", implementation = LocationResponse.class, accessMode = Schema.AccessMode.READ_ONLY)
    private LocationResponse location;

    @ArraySchema(
            minItems = 1,
            maxItems = 5,
            arraySchema = @Schema(description = "특성 리스트 (1~5개)"),
            schema = @Schema(description = "특성 항목", example = "협업")
    )
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private List<String> traits;

    @Schema(description = "생년월일", example = "1997-10-10", type = "string", format = "date", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDate birthDate;

    @Schema(description = "활동 시간", implementation = ActiveHoursResponse.class, accessMode = Schema.AccessMode.READ_ONLY)
    private ActiveHoursResponse activeHours;
}
