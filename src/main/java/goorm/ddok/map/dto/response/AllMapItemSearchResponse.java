package goorm.ddok.map.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import goorm.ddok.global.dto.LocationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "AllMapItemSearchResponse",
        description = "지도 검색 결과 통합 아이템 스키마"
)
public class AllMapItemSearchResponse {

    @Schema(description = "카테고리", example = "project", allowableValues = {"project", "study", "player"})
    private final String category;

    // Project fields
    @Schema(description = "프로젝트 ID (category='project'일 때만 존재)", example = "1", nullable = true)
    private final Long projectId;

    // Study fields
    @Schema(description = "스터디 ID (category='study'일 때만 존재)", example = "1", nullable = true)
    private final Long studyId;

    // Common fields for project and study
    @Schema(description = "제목 (project/study)", example = "구지라지 프로젝트", nullable = true)
    private final String title;

    @Schema(description = "팀 상태 (project/study)", example = "RECRUITING", nullable = true)
    private final String teamStatus;

    @Schema(description = "배너 이미지 URL (project/study)", example = "/banner.jpg", nullable = true)
    private final String bannerImageUrl;

    // Player fields
    @Schema(description = "플레이어 ID (category='player'일 때만 존재)", example = "1", nullable = true)
    private final Long userId;

    @Schema(description = "플레이어 닉네임 (category='player'일 때만 존재)", example = "똑똑한 백엔드", nullable = true)
    private final String nickname;

    @Schema(description = "내 정보인지 여부 (category='player'일 때만 존재)", example = "true", nullable = true)
    private final Boolean IsMine;

    @Schema(description = "프로필 이미지 URL (category='player'일 때만 존재)", example = "/profile.jpg", nullable = true)
    private final String profileImageUrl;

    @Schema(description = "온도 (category='player'일 때만 존재)", example = "36.5", nullable = true)
    private final Double temperature;

    @Schema(description = "메인 뱃지 (category='player'일 때만 존재)", nullable = true)
    private final MainBadge mainBadge;

    @Schema(description = "포기 뱃지 (category='player'일 때만 존재)", nullable = true)
    private final AbandonBadge abandonBadge;

    // Common location field
    @Schema(description = "위치 정보")
    private final LocationDto location;

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
}
