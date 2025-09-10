package goorm.ddok.notification.listener;

import goorm.ddok.notification.domain.Notification;
import goorm.ddok.notification.domain.NotificationType;
import goorm.ddok.notification.dto.NotificationPayload;
import goorm.ddok.notification.event.ProjectJoinRequestedEvent;
import goorm.ddok.notification.repository.NotificationRepository;
import goorm.ddok.notification.service.NotificationPushService;
import goorm.ddok.notification.support.NotificationMessageHelper;
import goorm.ddok.member.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ProjectJoinRequestedListener {

    private final NotificationRepository notificationRepository;
    private final NotificationPushService pushService;
    private final NotificationMessageHelper messageHelper;

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ProjectJoinRequestedEvent e) {
        User ownerRef = em.getReference(User.class, e.getOwnerUserId());

        User applicantRef = em.getReference(User.class, e.getApplicantUserId());
        String actorNick = (e.getApplicantNickname() != null) ? e.getApplicantNickname() : applicantRef.getNickname();
        var actorTemp = (applicantRef.getReputation() != null)
                ? applicantRef.getReputation().getTemperature()
                : null;

        String base = "당신의 \"" + e.getProjectTitle() + "\" 프로젝트에 "
                + actorNick + "님이 참여 승인 요청을 보냈습니다.";
        String msg = messageHelper.withTemperatureSuffix(e.getApplicantUserId(), base);

        Notification noti = Notification.builder()
                .receiver(ownerRef)
                .type(NotificationType.PROJECT_JOIN_REQUEST)
                .message(msg)
                .read(false)
                .processed(false)
                .projectId(e.getProjectId())
                .projectTitle(e.getProjectTitle())
                .applicantUserId(e.getApplicantUserId()) // 행위자
                .createdAt(Instant.now())
                .build();

        noti = notificationRepository.save(noti);

        NotificationPayload payload = NotificationPayload.builder()
                .id(String.valueOf(noti.getId()))
                .type("PROJECT_JOIN_REQUEST")
                .message(msg)
                .isRead(false)
                .createdAt(noti.getCreatedAt())
                .projectId(String.valueOf(e.getProjectId()))
                .projectTitle(e.getProjectTitle())
                .actorUserId(String.valueOf(e.getApplicantUserId()))
                .actorNickname(actorNick)
                .actorTemperature(actorTemp)
                .userId(String.valueOf(e.getApplicantUserId()))
                .userNickname(actorNick)
                .build();

        pushService.pushToUser(e.getOwnerUserId(), payload);
    }
}

