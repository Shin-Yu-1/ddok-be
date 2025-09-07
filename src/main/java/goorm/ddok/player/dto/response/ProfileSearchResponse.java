package goorm.ddok.player.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "ProfileSearchResponse", description = "플레이어 검색 응답")
public class ProfileSearchResponse {

    @Schema(description = "플레이어 사용자 ID", example = "1")
    private final Long userId;

    @Schema(description = "카테고리", example = "player")
    private final String category;

    @Schema(description = "플레이어 닉네임 (category='player')", example = "멍한 백엔드")
    private final String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profile.png")
    private final String profileImageUrl;

    @Schema(description = "메인 뱃지")
    private final MainBadge mainBadge;

    @Schema(description = "탈주 뱃지")
    private final AbandonBadge abandonBadge;

    @Schema(description = "메인 포지션)", example = "백엔드")
    private final String mainPosition;

    @Schema(description = "주소", example = "서울 강남구")
    private final String address;

    @Schema(description = "온도", example = "36.6")
    private final Double temperature;

    @Schema(description = "내 프로필 여부", example = "true")
    private final boolean IsMine;

    @Schema(description = "채팅방 ID", example = "1", nullable = true)
    private final Long chatRoomId;

    @Schema(description = "1:1 대화 요청 상태", example = "false")
    private final boolean dmRequestPending;

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
        private final boolean IsGranted;

        @Schema(description = "포기 횟수", example = "0")
        private final Integer count;
    }
}
