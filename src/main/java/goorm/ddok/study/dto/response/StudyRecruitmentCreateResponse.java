package goorm.ddok.study.dto.response;

import goorm.ddok.study.domain.StudyMode;
import goorm.ddok.study.domain.StudyType;
import goorm.ddok.study.domain.TeamStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "StudyRecruitmentCreateResponse",
        description = "스터디 모집글 생성 응답 DTO",
        example = """
        {
          "studyId": 1,
          "userId": 1,
          "nickname": "고라니",
          "teamStatus": "RECRUITING",
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
          "bannerImageUrl": "https://cdn.example.com/images/default.png",
          "traits": ["정리의 신", "실행력 갓", "내향인"],
          "studyType": "JOB_INTERVIEW",
          "detail": "저희 정말 멋진 영어공부를 할거예요~ 하고 싶죠?"
        }
        """
)
public class StudyRecruitmentCreateResponse {

    @Schema(description = "스터디 ID", example = "1")
    private Long studyId;

    @Schema(description = "리더 사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "리더 닉네임", example = "고라니")
    private String nickname;

    @Schema(description = "팀 상태 (RECRUITING / ONGOING / CLOSED)", example = "RECRUITING")
    private TeamStatus teamStatus;

    @Schema(description = "스터디 제목", example = "구지라지")
    private String title;

    @Schema(description = "스터디 시작일", example = "2025-08-16")
    private LocalDate expectedStart;

    @Schema(description = "예상 진행 개월 수", example = "3")
    private Integer expectedMonth;

    @Schema(description = "진행 방식 (ONLINE / OFFLINE)", example = "OFFLINE")
    private StudyMode mode;

    @Schema(description = "스터디 진행 장소 (OFFLINE일 경우만 존재)")
    private LocationDto location;

    @Schema(description = "선호 연령대 (없을 경우 null)")
    private PreferredAgesDto preferredAges;

    @Schema(description = "모집 정원", example = "6")
    private Integer capacity;

    @Schema(description = "배너 이미지 URL", example = "https://cdn.example.com/images/default.png")
    private String bannerImageUrl;

    @Schema(description = "모집 성향 리스트", example = "[\"정리의 신\", \"실행력 갓\", \"내향인\"]")
    private List<String> traits;

    @Schema(description = "스터디 유형 (ENUM)", example = "JOB_INTERVIEW")
    private StudyType studyType;

    @Schema(description = "상세 설명 (Markdown)", example = "저희 정말 멋진 영어공부를 할거예요~ 하고 싶죠?")
    private String detail;

    @Getter
    @Builder
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
    @Builder
    @Schema(name = "PreferredAgesDto", description = "선호 연령대")
    public static class PreferredAgesDto {
        @Schema(description = "최소 연령", example = "20")
        private Integer ageMin;

        @Schema(description = "최대 연령", example = "30")
        private Integer ageMax;
    }
}
