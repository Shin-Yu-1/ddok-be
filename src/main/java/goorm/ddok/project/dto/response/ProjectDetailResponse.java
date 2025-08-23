package goorm.ddok.project.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import goorm.ddok.project.domain.ProjectMode;
import goorm.ddok.project.domain.TeamStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
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

    @JsonProperty("isMine")
    @Schema(description = "내가 작성한 글인지 여부", example = "true")
    private boolean isMine;

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

    @Schema(description = "신청자 수", example = "6")
    private Integer applicantCount;

    @Schema(description = "진행 방식 (ONLINE / OFFLINE)", example = "ONLINE")
    private ProjectMode mode;

    @Schema(description = "진행 주소 (ONLINE 시 'ONLINE')", example = "서울 강남구 테헤란로")
    private String address;

    @Schema(description = "선호 연령대 (없을 경우 null)")
    private PreferredAgesDto preferredAges;

    @Schema(description = "예상 진행 개월 수", example = "3")
    private Integer expectedMonth;

    @Schema(description = "프로젝트 시작 예정일", example = "2025-09-10")
    private LocalDate startDate;

    @Schema(description = "상세 설명", example = "저희 정말 멋진 웹을 만들거에요~ 하고 싶죠?")
    private String detail;

    @Schema(description = "포지션별 모집 현황")
    private List<PositionDto> positions;

    @Schema(description = "리더 정보")
    private ParticipantDto leader;

    @Schema(description = "참여자 목록")
    private List<ParticipantDto> participants;

    // ------------------- Sub DTO -------------------

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "PreferredAgesDto", description = "선호 연령대")
    public static class PreferredAgesDto {
        @Schema(description = "최소 연령", example = "20")
        private Integer ageMin;

        @Schema(description = "최대 연령", example = "30")
        private Integer ageMax;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "PositionDto", description = "포지션별 모집 현황")
    public static class PositionDto {
        @Schema(description = "포지션명", example = "백엔드")
        private String position;

        @Schema(description = "지원자 수", example = "3")
        private int applied;

        @Schema(description = "확정 인원 수", example = "2")
        private int confirmed;

        @JsonProperty("isApplied")
        @Schema(description = "내가 지원한 포지션 여부", example = "false")
        private boolean isApplied;

        @JsonProperty("isApproved")
        @Schema(description = "내가 승인된 포지션 여부", example = "false")
        private boolean isApproved;

        @JsonProperty("isAvailable")
        @Schema(description = "지원 가능 여부", example = "true")
        private boolean isAvailable;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ParticipantDto", description = "참여자/리더 정보")
    public static class ParticipantDto {
        @Schema(description = "사용자 ID", example = "101")
        private Long userId;

        @Schema(description = "닉네임", example = "개구라")
        private String nickname;

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/images/user101.png")
        private String profileImageUrl;

        @Schema(description = "주 포지션", example = "풀스택")
        private String mainPosition;

        @Schema(description = "온도", example = "36.5")
        private Double temperature;

        @Schema(description = "확정된 포지션", example = "백엔드")
        private String decidedPosition;

        @Schema(description = "채팅방 ID (없으면 null)", example = "5")
        private Long chatRoomId;

        @Schema(description = "DM 요청 대기 여부", example = "false")
        private boolean dmRequestPending;
    }
}