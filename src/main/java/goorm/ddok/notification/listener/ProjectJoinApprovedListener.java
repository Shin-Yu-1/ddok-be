package goorm.ddok.notification.listener;

import goorm.ddok.member.domain.User;
import goorm.ddok.notification.domain.Notification;
import goorm.ddok.notification.domain.NotificationType;
import goorm.ddok.notification.dto.NotificationPayload;
import goorm.ddok.notification.event.ProjectJoinApprovedEvent;
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
public class ProjectJoinApprovedListener {

    private final NotificationRepository notificationRepository;
    private final NotificationPushService pushService;
    private final NotificationMessageHelper messageHelper;

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ProjectJoinApprovedEvent e) {
        User applicantRef = em.getReference(User.class, e.getApplicantUserId());

        User approverRef = em.getReference(User.class, e.getApproverUserId());
        var approverNick = approverRef.getNickname();
        var approverTemp = (approverRef.getReputation() != null)
                ? approverRef.getReputation().getTemperature()
                : null;

        String base = "당신의 \"" + e.getProjectTitle() + "\" 프로젝트 참여 희망 요청이 승인되었습니다.";
        String msg = messageHelper.withTemperatureSuffix(e.getApproverUserId(), base);

        Notification noti = Notification.builder()
                .receiver(applicantRef)
                .type(NotificationType.PROJECT_JOIN_APPROVED)
                .message(msg)
                .read(false)
                .processed(false)
                .projectId(e.getProjectId())
                .projectTitle(e.getProjectTitle())
                .requesterUserId(e.getApproverUserId())
                .createdAt(Instant.now())
                .build();

        noti = notificationRepository.save(noti);

        NotificationPayload payload = NotificationPayload.builder()
                .id(String.valueOf(noti.getId()))
                .type("PROJECT_JOIN_APPROVED")
                .message(msg)
                .isRead(false)
                .createdAt(noti.getCreatedAt())
                .projectId(String.valueOf(e.getProjectId()))
                .projectTitle(e.getProjectTitle())
                .actorUserId(String.valueOf(e.getApproverUserId()))
                .actorNickname(approverNick)
                .actorTemperature(approverTemp)
                .userId(String.valueOf(e.getApproverUserId()))
                .userNickname(approverNick)
                .build();

        pushService.pushToUser(e.getApplicantUserId(), payload);
    }
}
