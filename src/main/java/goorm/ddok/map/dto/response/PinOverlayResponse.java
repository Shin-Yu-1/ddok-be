package goorm.ddok.map.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.project.domain.ProjectMode;
import goorm.ddok.project.domain.TeamStatus;
import goorm.ddok.study.domain.StudyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "PinOverlayResponse", description = "지도 핀 오버레이 응답")
public class PinOverlayResponse {

    /* =========================
     *  공통 필드
     * ========================= */
    @Schema(description = "카테고리", example = "project", allowableValues = {"project", "study", "cafe", "player"})
    private final String category;

    @Schema(description = "주소", example = "서울 강남구")
    private final String address;

    /* =========================
     *  ID 필드 (카테고리별)
     * ========================= */
    @Schema(description = "프로젝트 ID (category='project'일 때)", nullable = true)
    private final Long projectId;

    @Schema(description = "스터디 ID (category='study'일 때)", nullable = true)
    private final Long studyId;

    @Schema(description = "카페 ID (category='cafe'일 때)", nullable = true)
    private final Long cafeId;

    @Schema(description = "사용자 ID (category='player'일 때)", nullable = true)
    private final Long userId;

    /* =========================
     *  프로젝트/스터디 공통 필드
     * ========================= */
    @Schema(description = "공고 제목", example = "구지라지")
    private String title;

    @Schema(description = "배너 이미지 URL", example = "https://cdn.example.com/images/default.png")
    private String bannerImageUrl;

    @Schema(description = "팀 상태", example = "RECRUITING")
    private String teamStatus;

    @Schema(description = "모집 정원", example = "6")
    private Integer capacity;

    @Schema(description = "진행 방식", example = "offline")
    private ProjectMode mode;

    @Schema(description = "선호 연령대 (무관이면 null)", nullable = true)
    private PreferredAgesDto preferredAges;

    @Schema(description = "예상 진행 개월 수", example = "3")
    private Integer expectedMonth;

    @Schema(description = "시작 예정일", example = "2025-09-16")
    private LocalDate startDate;


    /* =========================
     *  프로젝트 전용 필드
     * ========================= */
    @Schema(description = "모집 포지션 리스트", example = "[\"백엔드\", \"프론트엔드\", \"디자이너\"]")
    private List<String> positions;

    /* =========================
     *  스터디 전용 필드
     * ========================= */
    @Schema(description = "스터디 유형 (category='study'일 때)", example = "JOB_INTERVIEW", nullable = true)
    private StudyType studyType;

    /* =========================
     *  카페 전용 필드
     * ========================= */
    @Schema(description = "평균 평점 (category='cafe'일 때)", example = "3.9", nullable = true)
    private final BigDecimal rating;

    @Schema(description = "후기 개수 (category='cafe'일 때)", example = "193", nullable = true)
    private final Long reviewCount;

    /* =========================
     *  플레이어 전용 필드
     * ========================= */
    @Schema(description = "닉네임 (category='player'일 때)", example = "똑똑한 똑똑이", nullable = true)
    private String nickname;

    @Schema(description = "프로필 이미지 URL (category='player'일 때)", example = "https://cdn.example.com/profile.png", nullable = true)
    private String profileImageUrl;

    @Schema(description = "메인 포지션 (category='player'일 때)", example = "backend", nullable = true)
    private String mainPosition;

    @Schema(description = "온도 (category='player'일 때)", example = "36.6", nullable = true)
    private BigDecimal temperature;

    @Schema(description = "내 프로필 여부 (category='player'일 때)", example = "true", nullable = true)
    private boolean IsMine;

    @Schema(description = "메인 뱃지 (category='player'일 때)", nullable = true)
    private final MainBadge mainBadge;

    @Schema(description = "탈주 뱃지 (category='player'일 때)", nullable = true)
    private final AbandonBadge abandonBadge;

    @Schema(description = "최근 참여 프로젝트 (category='player'일 때)", nullable = true)
    private final MiniItem latestProject;

    @Schema(description = "최근 참여 스터디 (category='player'일 때)", nullable = true)
    private final MiniItem latestStudy;

    /* =========================
     *  내부 클래스
     * ========================= */
    @Getter
    @Builder
    @Schema(description = "메인 뱃지 정보")
    public static class MainBadge {
        @Schema(description = "뱃지 타입", example = "login")
        private final String type;

        @Schema(description = "뱃지 등급", example = "bronze")
        private final String tier;
    }

    @Getter
    @Builder
    @Schema(description = "탈주 뱃지 정보")
    public static class AbandonBadge {
        @Schema(description = "뱃지 부여 여부", example = "false")
        private final boolean isGranted;

        @Schema(description = "포기 횟수", example = "0")
        private final Integer count;
    }

    @Getter
    @Builder
    @Schema(description = "미니 아이템 정보")
    public static class MiniItem {
        @Schema(description = "ID", example = "1")
        private final Long id;

        @Schema(description = "제목", example = "React 스터디")
        private final String title;

        @Schema(description = "팀 상태", example = "RECRUITING")
        private final String teamStatus;
    }
}
