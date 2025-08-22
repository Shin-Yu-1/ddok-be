package goorm.ddok.project.dto.response;

import goorm.ddok.member.domain.User;
import goorm.ddok.project.domain.ProjectMode;
import goorm.ddok.project.domain.ProjectRecruitment;
import goorm.ddok.project.domain.TeamStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@Schema(
        name = "ProjectRecruitmentResponse",
        description = "프로젝트 모집글 생성 응답 DTO",
        example = """
        {
          "projectId": 1,
          "userId": 1,
          "nickname": "고라니",
          "leaderPosition": "백엔드",
          "title": "구지라지",
          "teamStatus": "RECRUTING",
          "expectedStart": "2025-08-16",
          "expectedMonth": 3,
          "mode": "offline",
          "location": {
            "latitude": 37.5665,
            "longitude": 126.9780,
            "address": "서울 강남구"
          },
          "preferredAges": {
            "ageMin": 20,
            "ageMax": 30
          },
          "capacity": 6,
          "bannerImageUrl": "https://cdn.example.com/images/default.png",
          "traits": ["정리의 신", "실행력 갓", "내향인"],
          "positions": ["백엔드", "프론트엔드", "디자이너"],
          "detail": "저희 정말 멋진 웹을 만들거에요~ 하고 싶죠?"
        }
        """
)
public class ProjectRecruitmentResponse {

    @Schema(description = "프로젝트 ID", example = "1")
    private Long projectId;

    @Schema(description = "리더 사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "리더 닉네임", example = "고라니")
    private String nickname;

    @Schema(description = "리더 포지션", example = "백엔드")
    private String leaderPosition;

    @Schema(description = "공고 제목", example = "구지라지")
    private String title;

    @Schema(description = "팀 상태", example = "RECRUTING")
    private TeamStatus teamStatus;

    @Schema(description = "프로젝트 시작 예정일", example = "2025-08-16")
    private LocalDate expectedStart;

    @Schema(description = "예상 진행 개월 수", example = "3")
    private Integer expectedMonth;

    @Schema(description = "진행 방식 (ONLINE / OFFLINE)", example = "offline")
    private ProjectMode mode;

    @Schema(description = "프로젝트 진행 장소 (offline일 경우 객체, online일 경우 문자열 'online')")
    private Object location;

    @Schema(description = "선호 연령대")
    private PreferredAges preferredAges;

    @Schema(description = "모집 정원", example = "6")
    private Integer capacity;

    @Schema(description = "배너 이미지 URL", example = "https://cdn.example.com/images/default.png")
    private String bannerImageUrl;

    @Schema(description = "모집 성향 리스트", example = "[\"정리의 신\", \"실행력 갓\", \"내향인\"]")
    private List<String> traits;

    @Schema(description = "모집 포지션 리스트", example = "[\"백엔드\", \"프론트엔드\", \"디자이너\"]")
    private List<String> positions;

    @Schema(description = "공고 상세 설명 (Markdown)", example = "저희 정말 멋진 웹을 만들거에요~ 하고 싶죠?")
    private String detail;

    @Getter
    @Builder
    @Schema(
            name = "LocationResponse",
            description = "오프라인 위치 정보 응답",
            example = """
            {
              "latitude": 37.5665,
              "longitude": 126.9780,
              "address": "서울 강남구"
            }
            """
    )
    public static class LocationResponse {
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String address;
    }

    @Getter
    @Builder
    @Schema(
            name = "PreferredAges",
            description = "선호 연령대 응답",
            example = """
            {
              "ageMin": 20,
              "ageMax": 30
            }
            """
    )
    public static class PreferredAges {
        private int ageMin;
        private int ageMax;
    }

    public static ProjectRecruitmentResponse fromEntity(ProjectRecruitment recruitment, User user, String leaderPosition) {
        return ProjectRecruitmentResponse.builder()
                .projectId(recruitment.getId())
                .userId(user.getId())
                .nickname(user.getNickname())
                .leaderPosition(leaderPosition)
                .title(recruitment.getTitle())
                .teamStatus(recruitment.getTeamStatus())
                .expectedStart(recruitment.getStartDate())
                .expectedMonth(recruitment.getExpectedMonths())
                .mode(recruitment.getProjectMode())
                .location(recruitment.getProjectMode() == ProjectMode.ONLINE
                        ? "ONLINE"
                        : LocationResponse.builder()
                        .latitude(recruitment.getLatitude())
                        .longitude(recruitment.getLongitude())
                        .address(recruitment.getRoadName())
                        .build())
                .preferredAges(PreferredAges.builder()
                        .ageMin(recruitment.getAgeMin())
                        .ageMax(recruitment.getAgeMax())
                        .build())
                .capacity(recruitment.getCapacity())
                .bannerImageUrl(recruitment.getBannerImageUrl())
                .traits(recruitment.getTraits().stream()
                        .map(t -> t.getTraitName())
                        .collect(Collectors.toList()))
                .positions(recruitment.getPositions().stream()
                        .map(p -> p.getPositionName())
                        .collect(Collectors.toList()))
                .detail(recruitment.getContentMd())
                .build();
    }
}