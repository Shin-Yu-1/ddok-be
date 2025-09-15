package goorm.ddok.notification.service;

import goorm.ddok.notification.dto.NotificationPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationPushService {
    private final SimpMessagingTemplate template;

    // 개인 알림
    public void pushToUser(Long userId, NotificationPayload payload) {
        template.convertAndSendToUser(String.valueOf(userId), "/queue/notifications", payload);
    }

    // 브로드캐스트(옵션)
    public void broadcast(NotificationPayload payload) {
        template.convertAndSend("/topic/notifications", payload);
    }
}