package goorm.ddok.player.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileSearchResponse {

    private final Long userId;
    private final String category;
    private final String nickname;
    private final String profileImageUrl;

    private final MainBadge mainBadge;
    private final AbandonBadge abandonBadge;

    private final String mainPosition;
    private final String address;
    private final Double temperature;

    private final boolean IsMine;

    private final Long chatRoomId;
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
        private final boolean isGranted;

        @Schema(description = "포기 횟수", example = "0")
        private final Integer count;
    }
}
