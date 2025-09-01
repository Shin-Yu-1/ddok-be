package goorm.ddok.project.dto.response;

import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.project.domain.ProjectMode;
import goorm.ddok.project.domain.TeamStatus;
import goorm.ddok.project.dto.ProjectPositionDto;
import goorm.ddok.project.dto.ProjectUserSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "ProjectDetailResponse",
        description = "프로젝트 모집글 상세 조회 응답 DTO"
)
public class ProjectDetailResponse {

    @Schema(description = "프로젝트 ID", example = "2")
    private Long projectId;

    @Schema(description = "내가 작성한 글인지 여부", example = "true")
    private boolean IsMine;

    @Schema(description = "프로젝트 제목", example = "구라라지 프로젝트")
    private String title;

    @Schema(description = "팀 상태 (RECRUITING / ONGOING / CLOSED)", example = "RECRUITING")
    private TeamStatus teamStatus;

    @Schema(description = "배너 이미지 URL", example = "https://cdn.example.com/images/default.png")
    private String bannerImageUrl;

    @Schema(description = "모집 성향 리스트", example = "[\"정리의 신\", \"실행력 갓\", \"내향인\"]")
    private List<String> traits;

    @Schema(description = "모집 정원", example = "4")
    private Integer capacity;

    @Schema(description = "지원자 수", example = "6")
    private Integer applicantCount;

    @Schema(description = "진행 방식 (online / offline)", example = "online")
    private ProjectMode mode;

    @Schema(description = "진행 주소 (online 일 경우 null, offline 시/구 주소)", example = "서울 강남구")
    private String address;

    @Schema(description = "선호 연령대")
    private PreferredAgesDto preferredAges;

    @Schema(description = "예상 진행 개월 수", example = "3")
    private Integer expectedMonth;

    @Schema(description = "프로젝트 시작 예정일", example = "2025-09-10")
    private LocalDate startDate;

    @Schema(description = "상세 설명", example = "저희 정말 멋진 웹을 만들거에요~ 하고 싶죠?")
    private String detail;

    @Schema(description = "포지션별 모집 현황")
    private List<ProjectPositionDto> positions;

    @Schema(description = "리더 정보")
    private ProjectUserSummaryDto leader;

    @Schema(description = "참여자 목록 (리더 제외)")
    private List<ProjectUserSummaryDto> participants;

}