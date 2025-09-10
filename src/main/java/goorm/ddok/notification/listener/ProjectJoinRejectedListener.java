package goorm.ddok.notification.listener;

import goorm.ddok.member.domain.User;
import goorm.ddok.notification.domain.Notification;
import goorm.ddok.notification.domain.NotificationType;
import goorm.ddok.notification.dto.NotificationPayload;
import goorm.ddok.notification.event.ProjectJoinRejectedEvent;
import goorm.ddok.notification.repository.NotificationRepository;
import goorm.ddok.notification.service.NotificationPushService;
import goorm.ddok.notification.support.NotificationMessageHelper; // ★ 추가
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
public class ProjectJoinRejectedListener {

    private final NotificationRepository notificationRepository;
    private final NotificationPushService pushService;
    private final NotificationMessageHelper messageHelper;

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ProjectJoinRejectedEvent e) {
        User applicantRef = em.getReference(User.class, e.getApplicantUserId());

        String base = "당신의 \"" + e.getProjectTitle() + "\" 프로젝트 참여 희망 요청을 프로젝트 모집자가 거절하였습니다.";
        String msg = messageHelper.withTemperatureSuffix(e.getRejectorUserId(), base); // ★

        Notification noti = Notification.builder()
                .receiver(applicantRef)
                .type(NotificationType.PROJECT_JOIN_REJECTED)
                .message(msg)
                .read(false)
                .processed(false)
                .processedAt(null)
                .projectId(e.getProjectId())
                .projectTitle(e.getProjectTitle())
                .applicantUserId(e.getApplicantUserId())
                .requesterUserId(e.getRejectorUserId())
                .createdAt(Instant.now())
                .build();

        noti = notificationRepository.save(noti);

        NotificationPayload payload = NotificationPayload.builder()
                .id(String.valueOf(noti.getId()))
                .type(noti.getType().name())
                .message(noti.getMessage()) // ★ 온도 포함 푸시
                .isRead(false)
                .createdAt(noti.getCreatedAt())
                .projectId(String.valueOf(e.getProjectId()))
                .projectTitle(e.getProjectTitle())
                .build();

        pushService.pushToUser(e.getApplicantUserId(), payload);
    }
}
