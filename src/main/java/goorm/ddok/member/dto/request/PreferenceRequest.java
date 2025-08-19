package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "개인화 설정 요청 DTO")
public class PreferenceRequest {

    @Schema(description = "대표 포지션", example = "백엔드")
    @NotBlank(message = "대표 포지션은 필수 입력값입니다.")
    private String mainPosition;

    @Schema(
            description = "관심 포지션 (정확히 2개)",
            example = "[\"프론트엔드\", \"데브옵스\"]"
    )
    @NotNull(message = "관심 포지션은 필수 입력값입니다.")
    @Size(min = 2, max = 2, message = "관심 포지션은 반드시 2개여야 합니다.")
    private List<@NotBlank(message = "관심 포지션 항목은 비어 있을 수 없습니다.") String> subPosition;

    @Schema(
            description = "기술 스택",
            example = "[\"Java\", \"Spring Boot\", \"React\"]"
    )
    // @NotEmpty(message = "기술 스택은 최소 1개 이상 입력해야 합니다.")
    private List<String> techStacks;

    @Schema(
            description = "위치 정보 요청 DTO",
            example = "{ \"latitude\": 37.5665, \"longitude\": 126.9780, \"address\": \"서울특별시 강남구 테헤란로 123\" }"
    )
    @NotNull(message = "위치 정보는 필수 입력값입니다.")
    @Valid
    private LocationRequest location;

    @Schema(
            description = "특성 리스트 (1~5개)",
            example = "[\"협업\", \"문제 해결\"]"
    )
    @NotNull(message = "특성(traits)은 필수 입력 값입니다.")
    @Size(min = 1, max = 5, message = "특성(traits)은 최소 1개, 최대 5개까지 입력 가능합니다.")
    private List<@NotBlank(message = "특성 항목은 비어 있을 수 없습니다.") String> traits;

    @Schema(description = "생년월일", example = "1990-01-01")
    @NotNull(message = "생년월일은 필수 입력 값입니다.")
    @Past(message = "생년월일은 과거 날짜여야 합니다.")
    private LocalDate birthDate;

    @Schema(
            description = "활동 시간 요청 DTO (24시간 형식, 00~23)",
            example = "{ \"start\": \"09\", \"end\": \"18\" }"
    )
    @NotNull(message = "활동 시간은 필수 입력 값입니다.")
    @Valid
    private ActiveHoursRequest activeHours;
}
