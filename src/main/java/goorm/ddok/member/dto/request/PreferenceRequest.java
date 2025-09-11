package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
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
@Schema(
        name = "PreferenceRequest",
        description = "개인화 설정 요청 DTO",
        requiredProperties = {"mainPosition", "subPosition", "location", "traits", "birthDate", "activeHours"},
        example = """
        {
          "mainPosition": "백엔드",
          "subPosition": [
            "풀스택",
            "데브옵스"
          ],
          "techStacks": [
            "Java",
            "Spring Boot",
            "MySQL",
            "Docker",
            "AWS"
          ],
          "location": {
            "address": "전북 익산시 망산길 11-17",
            "region1depthName": "전북",
            "region2depthName": "익산시",
            "region3depthName": "부송동",
            "roadName": "망산길",
            "mainBuildingNo": "11",
            "subBuildingNo": "17",
            "zoneNo": "54547",
            "latitude": 35.976749396987046,
            "longitude": 126.99599512792346
          },
          "traits": [
            "협업",
            "문제 해결",
            "학습 능력"
          ],
          "birthDate": "1995-03-15",
          "activeHours": {
            "start": "09",
            "end": "18"
          }
        }
        """
)
public class PreferenceRequest {

    @Schema(
            description = "대표 포지션",
            example = "백엔드",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "대표 포지션은 필수 입력값입니다.")
    private String mainPosition;

    @ArraySchema(
            minItems = 2,
            maxItems = 2,
            uniqueItems = false,
            arraySchema = @Schema(description = "관심 포지션 (정확히 2개)"),
            schema = @Schema(description = "관심 포지션 항목", example = "프론트엔드")
    )
    @NotNull(message = "관심 포지션은 필수 입력값입니다.")
    @Size(min = 2, max = 2, message = "관심 포지션은 반드시 2개여야 합니다.")
    private List<@NotBlank(message = "관심 포지션 항목은 비어 있을 수 없습니다.") String> subPosition;

    @ArraySchema(
            arraySchema = @Schema(description = "기술 스택 (선택 입력)"),
            schema = @Schema(description = "기술 스택 항목", example = "Spring Boot")
    )
    // @NotEmpty(message = "기술 스택은 최소 1개 이상 입력해야 합니다.")
    private List<String> techStacks;

    @Schema(
            description = "위치 정보 요청 DTO",
            implementation = LocationRequest.class,
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = """
        { "latitude": 37.5665, "longitude": 126.9780, "address": "서울특별시 강남구 테헤란로 123" }
        """
    )
    @NotNull(message = "위치 정보는 필수 입력값입니다.")
    @Valid
    private LocationRequest location;

    @ArraySchema(
            minItems = 1,
            maxItems = 5,
            arraySchema = @Schema(description = "특성 리스트 (1~5개)"),
            schema = @Schema(description = "특성 항목", example = "협업")
    )
    @NotNull(message = "특성(traits)은 필수 입력 값입니다.")
    @Size(min = 1, max = 5, message = "특성(traits)은 최소 1개, 최대 5개까지 입력 가능합니다.")
    private List<@NotBlank(message = "특성 항목은 비어 있을 수 없습니다.") String> traits;

    @Schema(
            description = "생년월일",
            example = "1997-10-10",
            type = "string",
            format = "date",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "생년월일은 필수 입력 값입니다.")
    @Past(message = "생년월일은 과거 날짜여야 합니다.")
    private LocalDate birthDate;

    @Schema(
            description = "활동 시간 요청 DTO (24시간 형식, 00~24)",
            implementation = ActiveHoursRequest.class,
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = """
        { "start": "19", "end": "23" }
        """
    )
    @NotNull(message = "활동 시간은 필수 입력 값입니다.")
    @Valid
    private ActiveHoursRequest activeHours;
}
