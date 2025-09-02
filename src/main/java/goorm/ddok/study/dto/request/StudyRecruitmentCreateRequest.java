package goorm.ddok.study.dto.request;

import goorm.ddok.global.dto.LocationDto;
import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.study.domain.StudyMode;
import goorm.ddok.study.domain.StudyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "StudyRecruitmentCreateRequest",
        description = """
        스터디 모집글 생성 요청 DTO
        
        • mode: online/offline
        • mode=offline → location 필수 (카카오 road_address 매핑)
        • preferredAges: null이면 무관(0/0)로 저장
        """,
        requiredProperties = {
                "title", "expectedStart", "expectedMonth",
                "mode", "capacity", "studyType", "detail"
        },
        example = """
        {
          "title": "구지라지",
          "expectedStart": "2025-08-16",
          "expectedMonth": 3,
          "mode": "offline",
          "location": {
            "address": "서울 강남구 역삼동 테헤란로 123-45",
            "region1depthName": "서울",
            "region2depthName": "강남구",
            "region3depthName": "역삼동",
            "mainBuildingNo": "123",
            "subBuildingNo": "45",
            "roadName": "테헤란로",
            "zoneNo": "06236",
            "latitude": 37.5665,
            "longitude": 126.9780
            },
          "preferredAges": { "ageMin": 20, "ageMax": 30 },
          "capacity": 6,
          "traits": ["정리의 신", "실행력 갓", "내향인"],
          "studyType": "취업/면접",
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
    @Schema(description = "진행 방식 (online / offline)", example = "offline", requiredMode = Schema.RequiredMode.REQUIRED)
    private StudyMode mode;

    @Schema(description = "스터디 진행 장소 (mode=offline일 경우 필수). 카카오 road_address 매핑 DTO")
    private LocationDto location;

    @Schema(description = "선호 연령대 (무관 시 null)")
    private PreferredAgesDto preferredAges;

    @NotNull(message = "모집 정원은 필수 입력값입니다.")
    @Min(value = 1, message = "모집 정원은 최소 1명 이상이어야 합니다.")
    @Max(value = 7, message = "모집 정원은 최대 7명 이하이어야 합니다.")
    @Schema(description = "모집 정원", example = "6", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer capacity;

    @Schema(description = "모집 성향 리스트", example = "[\"정리의 신\", \"실행력 갓\", \"내향인\"]")
    private List<String> traits;

    @NotNull(message = "스터디 유형은 필수 입력값입니다.")
    @Schema(description = "스터디 유형 (한글 라벨)", example = "취업/면접", requiredMode = Schema.RequiredMode.REQUIRED)
    private StudyType studyType;

    @NotBlank(message = "상세 설명은 필수 입력값입니다.")
    @Size(min = 10, max = 2000, message = "상세 설명은 10자 이상 2000자 이하여야 합니다.")
    @Schema(description = "상세 설명 (Markdown)", example = "저희 정말 멋진 영어공부를 할거예요~ 하고 싶죠?", requiredMode = Schema.RequiredMode.REQUIRED)
    private String detail;
}