package goorm.ddok.study.dto.request;

import goorm.ddok.global.dto.LocationDto;
import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.study.domain.StudyMode;
import goorm.ddok.study.domain.StudyType;
import goorm.ddok.study.domain.TeamStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Schema(
        name = "StudyRecruitmentUpdateRequest",
        description = """
            스터디 모집글 수정 요청 DTO.
            부분 수정이 가능하며, 전달된 필드만 반영됩니다.
            
            • mode: online/offline (소문자)
            • studyType: 한글 라벨로 입출력(예: "취업/면접")
            • capacity: 리더 제외 1~7명
            • preferredAges: null → 무관(0/0)로 저장
            """,
        example = """
        {
          "title": "프리토킹 스터디(수정)",
          "studyType": "취업/면접",
          "teamStatus": "RECRUITING",
          "expectedStart": "2025-09-15",
          "expectedMonth": 2,
          "mode": "online",
          "location": null,
          "preferredAges": { "ageMin": 20, "ageMax": 30 },
          "capacity": 4,
          "traits": ["정리의 신", "실행력 갓"],
          "detail": "매주 화/목 줌으로 프리토킹 진행합니다.",
          "bannerImageUrl": "https://cdn.example.com/images/study-banner.png"
        }
        """
)
public class StudyRecruitmentUpdateRequest {

    @Schema(description = "공고 제목 (미전달 시 기존 유지)", example = "프리토킹 스터디(수정)")
    private String title;

    @Schema(description = "스터디 유형(한글 라벨). 예: 취업/면접, 자격증 취득, 자기 개발, 어학, 생활, 취미/교양, 기타",
            example = "취업/면접")
    private StudyType studyType;

    @Schema(description = "팀 상태", example = "RECRUITING", allowableValues = {"RECRUITING","ONGOING","CLOSED"})
    private TeamStatus teamStatus;

    @Schema(description = "시작 예정일(오늘 또는 미래). 미전달 시 기존 유지", example = "2025-09-15")
    private LocalDate expectedStart;

    @Schema(description = "예상 진행 개월 수(1~64). 미전달 시 기존 유지", example = "2")
    private Integer expectedMonth;

    @Schema(description = "진행 방식 (online / offline). 미전달 시 기존 유지", example = "online")
    private StudyMode mode;          // online | offline

    @Schema(description = "OFFLINE일 때만 필요. FRONT에서 카카오 road_address 매핑값 사용", example = """
        {
          "latitude": 37.5665,
          "longitude": 126.9780,
          "address": "서울특별시 강남구 테헤란로…",
          "region1depthName": "서울특별시",
          "region2depthName": "강남구",
          "region3depthName": "역삼동",
          "roadName": "테헤란로",
          "mainBuildingNo": "123",
          "subBuildingNo": "45",
          "zoneNo": "06236"
        }
        """)
    private LocationDto location;    // OFFLINE일 때 필요

    @Schema(description = "선호 연령대 (무관: null 또는 {ageMin:0, ageMax:0})")
    private PreferredAgesDto preferredAges; // null → 무관(0/0)

    @Schema(description = "모집 정원(리더 제외 1~7). 미전달 시 기존 유지", example = "4", minimum = "1", maximum = "7")
    @Min(value = 1, message = "모집 정원은 최소 1명 이상이어야 합니다.")
    @Max(value = 7, message = "모집 정원은 최대 7명 이하이어야 합니다.")
    private Integer capacity;

    @Schema(description = "모집 성향 리스트(전체 치환). 미전달 시 기존 유지", example = "[\"정리의 신\", \"실행력 갓\"]")
    private List<String> traits;

    @Schema(description = "상세 설명 (Markdown). 미전달 시 기존 유지",
            example = "매주 화/목 줌으로 프리토킹 진행합니다.")
    private String detail;

    @Schema(description = "배너 이미지 URL. 파일 미첨부 시 변경/유지 위해 사용 가능",
            example = "https://cdn.example.com/images/study-banner.png")
    private String bannerImageUrl;
}