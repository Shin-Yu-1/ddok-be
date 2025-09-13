package goorm.ddok.notification.listener;

import goorm.ddok.badge.domain.BadgeTier;
import goorm.ddok.badge.domain.BadgeType;
import goorm.ddok.member.domain.User;
import goorm.ddok.notification.domain.Notification;
import goorm.ddok.notification.domain.NotificationType;
import goorm.ddok.notification.dto.NotificationPayload;
import goorm.ddok.notification.event.BadgeAchievementEvent;
import goorm.ddok.notification.repository.NotificationRepository;
import goorm.ddok.notification.service.NotificationPushService;
import goorm.ddok.notification.support.NotificationMessageHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class BadgeAchievementListener {

    private final NotificationRepository notificationRepository;
    private final NotificationPushService pushService;
    private final NotificationMessageHelper messageHelper;

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(BadgeAchievementEvent e) {
        User receiver = em.getReference(User.class, e.userId());

        String badgeKo = toKoName(e.badgeType());
        String tierKo = e.newTier() != null ? toKoTier(e.newTier()) : null;

        String base = switch (e.reason()) {
            case NEW_BADGE -> (tierKo == null)
                    ? String.format("새로운 업적 달성: \"%s\" 뱃지를 획득했습니다.", badgeKo)
                    : String.format("새로운 업적 달성: \"%s\" 뱃지를 획득했습니다. (%s)", badgeKo, tierKo);
            case TIER_UP -> String.format("\"%s\" 뱃지 티어가 %s로 승급되었습니다.", badgeKo, tierKo);
        };

        String achievementName = (tierKo == null) ? badgeKo : (badgeKo + " - " + tierKo);

        Notification noti = Notification.builder()
                .receiver(receiver)
                .type(NotificationType.ACHIEVEMENT)
                .message(base)
                .read(false)
                .processed(false)
                .createdAt(Instant.now())
                .achievementName(achievementName)
                .applicantUserId(e.userId())
                .build();

        noti = notificationRepository.save(noti);

        var payload = NotificationPayload.builder()
                .id(String.valueOf(noti.getId()))
                .type(NotificationType.ACHIEVEMENT.name())
                .message(base)
                .IsRead(false)
                .createdAt(noti.getCreatedAt())
                .achievementName(achievementName)
                .actorUserId(String.valueOf(e.userId()))
                .actorNickname(receiver.getNickname())
                .actorTemperature(null)
                .userId(String.valueOf(e.userId()))
                .userNickname(receiver.getNickname())
                .build();

        pushService.pushToUser(e.userId(), payload);
    }

    private String toKoName(BadgeType type) {
        return switch (type) {
            case complete -> "완주";
            case leader_complete -> "리더 완주";
            case login -> "출석";
            case abandon -> "탈주";
        };
    }

    private String toKoTier(BadgeTier tier) {
        return switch (tier) {
            case bronze -> "브론즈";
            case silver -> "실버";
            case gold -> "골드";
        };
    }
}
