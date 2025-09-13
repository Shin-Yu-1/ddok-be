package goorm.ddok.notification.service;

import goorm.ddok.notification.dto.NotificationPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationPushService {
    private final SimpMessagingTemplate messagingTemplate;

    public void pushToUser(Long userId, NotificationPayload payload) {
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "/queue/notifications",
                payload
        );
    }
}
