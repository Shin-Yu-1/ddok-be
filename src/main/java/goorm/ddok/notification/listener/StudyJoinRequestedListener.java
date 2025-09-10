package goorm.ddok.notification.listener;

import goorm.ddok.member.domain.User;
import goorm.ddok.notification.domain.Notification;
import goorm.ddok.notification.domain.NotificationType;
import goorm.ddok.notification.dto.NotificationPayload;
import goorm.ddok.notification.event.StudyJoinRequestedEvent;
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
public class StudyJoinRequestedListener {

    private final NotificationRepository notificationRepository;
    private final NotificationPushService pushService;
    private final NotificationMessageHelper messageHelper;

    @PersistenceContext private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(StudyJoinRequestedEvent e) {
        User ownerRef = em.getReference(User.class, e.getOwnerUserId());

        String base = "당신의 \"" + e.getStudyTitle() + "\" 스터디에 "
                + e.getApplicantNickname() + "님이 참여 승인 요청을 보냈습니다.";
        String msg  = messageHelper.withTemperatureSuffix(e.getApplicantUserId(), base); // ★ 신청자 온도 포함

        Notification noti = Notification.builder()
                .receiver(ownerRef)
                .type(NotificationType.STUDY_JOIN_REQUEST)
                .message(msg)
                .read(false)
                .processed(false)
                .studyId(e.getStudyId())
                .studyTitle(e.getStudyTitle())
                .applicantUserId(e.getApplicantUserId())
                .createdAt(Instant.now())
                .build();

        noti = notificationRepository.save(noti);

        var payload = NotificationPayload.builder()
                .id(String.valueOf(noti.getId()))
                .type("STUDY_JOIN_REQUEST")
                .message(msg)
                .isRead(false)
                .createdAt(noti.getCreatedAt())
                .userId(String.valueOf(e.getApplicantUserId()))
                .userNickname(e.getApplicantNickname())
                .studyId(String.valueOf(e.getStudyId()))
                .studyTitle(e.getStudyTitle())
                .build();

        pushService.pushToUser(e.getOwnerUserId(), payload);
    }
}
