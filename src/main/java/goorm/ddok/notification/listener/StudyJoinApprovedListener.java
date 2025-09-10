package goorm.ddok.notification.listener;

import goorm.ddok.member.domain.User;
import goorm.ddok.notification.domain.Notification;
import goorm.ddok.notification.domain.NotificationType;
import goorm.ddok.notification.dto.NotificationPayload;
import goorm.ddok.notification.event.StudyJoinApprovedEvent;
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
public class StudyJoinApprovedListener {

    private final NotificationRepository notificationRepository;
    private final NotificationPushService pushService;
    private final NotificationMessageHelper messageHelper;

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(StudyJoinApprovedEvent e) {
        User applicantRef = em.getReference(User.class, e.getApplicantUserId());

        String base = "당신의 \"" + e.getStudyTitle() + "\" 스터디 참여 희망 요청이 승인되었습니다.";
        String msg = messageHelper.withTemperatureSuffix(e.getApproverUserId(), base);

        Notification noti = Notification.builder()
                .receiver(applicantRef)
                .type(NotificationType.STUDY_JOIN_APPROVED)
                .message(msg)
                .read(false)
                .processed(false)
                .studyId(e.getStudyId())
                .studyTitle(e.getStudyTitle())
                .requesterUserId(e.getApproverUserId())
                .createdAt(Instant.now())
                .build();

        noti = notificationRepository.save(noti);

        var payload = NotificationPayload.builder()
                .id(String.valueOf(noti.getId()))
                .type("STUDY_JOIN_APPROVED")
                .message(msg)
                .isRead(false)
                .createdAt(noti.getCreatedAt())
                .studyId(String.valueOf(e.getStudyId()))
                .studyTitle(e.getStudyTitle())
                .build();

        pushService.pushToUser(e.getApplicantUserId(), payload);
    }
}
