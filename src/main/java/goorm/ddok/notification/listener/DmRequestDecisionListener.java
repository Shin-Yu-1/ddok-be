package goorm.ddok.notification.listener;

import goorm.ddok.member.domain.User;
import goorm.ddok.notification.domain.Notification;
import goorm.ddok.notification.domain.NotificationType;
import goorm.ddok.notification.dto.NotificationPayload;
import goorm.ddok.notification.event.DmRequestDecisionEvent;
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
public class DmRequestDecisionListener {

    private final NotificationRepository notificationRepository;
    private final NotificationPushService pushService;
    private final NotificationMessageHelper messageHelper;

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(DmRequestDecisionEvent e) {
        User requester = em.getReference(User.class, e.getRequesterUserId()); // DM 원요청자(수신자)
        User approver  = em.getReference(User.class, e.getApproverUserId());  // 수락/거절한 사람(액터)

        boolean accepted = "accept".equalsIgnoreCase(e.getDecision());
        NotificationType type = accepted ? NotificationType.DM_APPROVED : NotificationType.DM_REJECTED;

        String base = approver.getNickname() + (accepted ? "님이 DM 요청을 수락했습니다." : "님이 DM 요청을 거절했습니다.");
        String msg  = messageHelper.withTemperatureSuffix(e.getApproverUserId(), base);

        Notification noti = Notification.builder()
                .receiver(requester)
                .type(type)
                .message(msg)
                .read(false)
                .processed(false)
                .applicantUserId(e.getRequesterUserId())
                .requesterUserId(e.getApproverUserId())
                .createdAt(Instant.now())
                .build();

        noti = notificationRepository.save(noti);

        NotificationPayload payload = NotificationPayload.builder()
                .id(String.valueOf(noti.getId()))
                .type(noti.getType().name())
                .message(msg)
                .IsRead(false)
                .createdAt(noti.getCreatedAt())
                .actorUserId(String.valueOf(e.getApproverUserId()))
                .actorNickname(approver.getNickname())
                .actorTemperature(approver.getReputation() != null ? approver.getReputation().getTemperature() : null)
                .userId(String.valueOf(e.getApproverUserId()))
                .userNickname(approver.getNickname())
                .build();

        pushService.pushToUser(e.getRequesterUserId(), payload);
    }
}
