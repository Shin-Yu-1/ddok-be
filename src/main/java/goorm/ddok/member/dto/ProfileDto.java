package goorm.ddok.member.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class ProfileDto {
    private Long userId;
    private boolean isMine;
    private Long chatRoomId;
    private boolean dmRequestPending;
    private Boolean isPublic;

    private String profileImageUrl;
    private String nickname;

    private BigDecimal temperature; // null 허용
    private String ageGroup;        // 예: "20대" (필요 시 계산)

    private String mainPosition;
    private List<String> subPositions;

    private Badge mainBadge;        // 요구: 값 없으면 null
    private AbandonBadge abandonBadge; // 요구: 값 없으면 null

    private ActiveHours activeHours;
    private List<String> traits;

    private String content;         // TODO: 저장 전까지 null
    private List<PortfolioLink> portfolio; // TODO: 저장 전까지 null

    private LocationBlock location;

    private List<String> techStacks;

    @Getter @AllArgsConstructor
    public static class Badge {
        private String type;
        private String tier;
    }

    @Getter @AllArgsConstructor
    public static class AbandonBadge {
        private Boolean isGranted;
        private Integer count;
    }

    @Getter @Builder
    public static class LocationBlock {
        private Double latitude;
        private Double longitude;
        private String address;
    }

    @Getter @AllArgsConstructor
    public static class PortfolioLink {
        private String linkTitle;
        private String link;
    }

    @Getter @AllArgsConstructor @NoArgsConstructor
    public static class ActiveHours {
        private Integer start;
        private Integer end;
    }
}