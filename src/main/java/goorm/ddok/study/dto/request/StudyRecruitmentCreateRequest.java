package goorm.ddok.study.dto.request;

import goorm.ddok.study.domain.StudyMode;
import goorm.ddok.study.domain.StudyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "StudyRecruitmentCreateRequest",
        description = "스터디 모집글 생성 요청 DTO",
        requiredProperties = {
                "title", "expectedStart", "expectedMonth",
                "mode", "capacity", "studyType", "detail"
        },
        example = """
        {
          "title": "구지라지",
          "expectedStart": "2025-08-16",
          "expectedMonth": 3,
          "mode": "OFFLINE",
          "location": {
            "latitude": 37.5665,
            "longitude": 126.9780,
            "address": "서울특별시 강남구 테헤란로…"
          },
          "preferredAges": {
            "ageMin": 20,
            "ageMax": 30
          },
          "capacity": 6,
          "traits": ["정리의 신", "실행력 갓", "내향인"],
          "studyType": "JOB_INTERVIEW",
          "detail": "저희 정말 멋진 영어공부를 할거예요~ 하고 싶죠?"
        }
        """
)
public class StudyRecruitmentCreateRequest {

    @NotBlank(message = "제목은 필수 입력값입니다.")
    @Size(min = 2, max = 30, message = "제목은 2자 이상 30자 이하여야 합니다.")
    @Schema(description = "공고 제목", example = "구지라지", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotNull(message = "예상 시작일은 필수 입력값입니다.")
    @Schema(description = "스터디 시작 예정일", example = "2025-08-16", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate expectedStart;

    @NotNull(message = "예상 진행 개월 수는 필수 입력값입니다.")
    @Min(value = 1, message = "예상 진행 개월 수는 최소 1개월 이상이어야 합니다.")
    @Max(value = 64, message = "예상 진행 개월 수는 최대 64개월 이하이어야 합니다.")
    @Schema(description = "예상 진행 개월 수", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer expectedMonth;

    @NotNull(message = "스터디 진행 방식은 필수 입력값입니다.")
    @Schema(description = "진행 방식 (ONLINE / OFFLINE)", example = "OFFLINE", requiredMode = Schema.RequiredMode.REQUIRED)
    private StudyMode mode;

    @Schema(description = "스터디 진행 장소 (OFFLINE일 경우만 입력)")
    private LocationDto location;

    @Schema(description = "선호 연령대 (무관 시 0 입력)")
    private PreferredAgesDto preferredAges;

    @NotNull(message = "모집 정원은 필수 입력값입니다.")
    @Min(value = 2, message = "모집 정원은 최소 2명 이상이어야 합니다.")
    @Max(value = 8, message = "모집 정원은 최대 8명 이하이어야 합니다.")
    @Schema(description = "모집 정원", example = "6", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer capacity;

    @Schema(description = "모집 성향 리스트", example = "[\"정리의 신\", \"실행력 갓\", \"내향인\"]")
    private List<String> traits;

    @NotNull(message = "스터디 유형은 필수 입력값입니다.")
    @Schema(description = "스터디 유형 (ENUM)", example = "JOB_INTERVIEW", requiredMode = Schema.RequiredMode.REQUIRED)
    private StudyType studyType;

    @NotBlank(message = "상세 설명은 필수 입력값입니다.")
    @Size(min = 10, max = 2000, message = "상세 설명은 10자 이상 2000자 이하여야 합니다.")
    @Schema(description = "상세 설명 (Markdown)", example = "저희 정말 멋진 영어공부를 할거예요~ 하고 싶죠?", requiredMode = Schema.RequiredMode.REQUIRED)
    private String detail;

    @Getter
    @Schema(name = "LocationDto", description = "오프라인 위치 정보")
    public static class LocationDto {
        @Schema(description = "위도", example = "37.5665")
        private BigDecimal latitude;

        @Schema(description = "경도", example = "126.9780")
        private BigDecimal longitude;

        @Schema(description = "주소", example = "서울특별시 강남구 테헤란로…")
        private String address;
    }

    @Getter
    @Schema(name = "PreferredAgesDto", description = "선호 연령대")
    public static class PreferredAgesDto {
        @Schema(description = "최소 연령", example = "20")
        private Integer ageMin;

        @Schema(description = "최대 연령", example = "30")
        private Integer ageMax;
    }
}
