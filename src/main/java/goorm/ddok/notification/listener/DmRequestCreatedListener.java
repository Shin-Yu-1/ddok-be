package goorm.ddok.notification.listener;

import goorm.ddok.member.domain.User;
import goorm.ddok.notification.domain.Notification;
import goorm.ddok.notification.domain.NotificationType;
import goorm.ddok.notification.dto.NotificationPayload;
import goorm.ddok.notification.event.DmRequestCreatedEvent;
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
public class DmRequestCreatedListener {

    private final NotificationRepository notificationRepository;
    private final NotificationPushService pushService;
    private final NotificationMessageHelper messageHelper;

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(DmRequestCreatedEvent e) {
        // 알림 수신자 = DM 요청의 받는이
        User receiverRef = em.getReference(User.class, e.getToUserId());

        String base = e.getFromNickname() + "님이 메시지를 보내고 싶어합니다.";
        String msg  = messageHelper.withTemperatureSuffix(e.getFromUserId(), base); // 온도 suffix 포함

        Notification noti = Notification.builder()
                .receiver(receiverRef)
                .type(NotificationType.DM_REQUEST)
                .message(msg)
                .read(false)
                .processed(false)
                .applicantUserId(e.getFromUserId())
                .requesterUserId(e.getFromUserId())
                .createdAt(Instant.now())
                .build();

        noti = notificationRepository.save(noti);

        // 프론트 WebSocket 페이로드 (userId/userNickname = 행위자)
        NotificationPayload payload = NotificationPayload.builder()
                .id(String.valueOf(noti.getId()))
                .type("DM_REQUEST")
                .message(msg)
                .IsRead(false)
                .createdAt(noti.getCreatedAt())
                .userId(String.valueOf(e.getFromUserId()))
                .userNickname(e.getFromNickname())
                .build();

        pushService.pushToUser(e.getToUserId(), payload);
    }
}
