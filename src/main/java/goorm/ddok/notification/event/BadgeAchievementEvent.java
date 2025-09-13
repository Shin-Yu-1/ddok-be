package goorm.ddok.notification.event;

import goorm.ddok.badge.domain.BadgeTier;
import goorm.ddok.badge.domain.BadgeType;
import org.springframework.lang.Nullable;

public record BadgeAchievementEvent(
        Long userId,
        BadgeType badgeType,
        @Nullable BadgeTier previousTier,
        @Nullable BadgeTier newTier,
        Integer totalCnt,
        Reason reason
) {
    public enum Reason { NEW_BADGE, TIER_UP }
}
