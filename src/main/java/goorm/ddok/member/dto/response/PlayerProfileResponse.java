package goorm.ddok.member.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import goorm.ddok.global.dto.AbandonBadgeDto;
import goorm.ddok.global.dto.BadgeDto;
import goorm.ddok.global.dto.LocationDto;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Builder @AllArgsConstructor @NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayerProfileResponse {

    private Long userId;

    @JsonProperty("isMine")
    private boolean IsMine;

    private Long chatRoomId;
    private boolean dmRequestPending;

    @JsonProperty("IsPublic")
    private Boolean isPublic; // 현재 컬럼 없음 → null

    private String profileImageUrl;
    private String nickname;

    private BigDecimal temperature; // 평판 없으면 null
    private String ageGroup;        // "20대" 등, 생년 없으면 null

    private String mainPosition;
    private List<String> subPositions;

    private BadgeDto mainBadge;         // 현재 null
    private AbandonBadgeDto abandonBadge; // 현재 null

    @Getter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class ActiveHours {
        private String start;
        private String end;
    }
    private ActiveHours activeHours;

    private List<String> traits;

    private String content; // 현재 null

    @Getter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class PortfolioItem {
        private String linkTitle;
        private String link;
    }
    private List<PortfolioItem> portfolio; // 현재 null

    private LocationDto location; // address는 roadName으로 대응
}