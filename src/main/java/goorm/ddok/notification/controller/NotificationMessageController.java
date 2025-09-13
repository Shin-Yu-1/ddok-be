package goorm.ddok.notification.controller;

import goorm.ddok.notification.service.NotificationApplicationService;
import goorm.ddok.notification.ws.NotificationActionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class NotificationMessageController {

    private final NotificationApplicationService notificationApplicationService;

    @MessageMapping("/notifications/{id}/action")
    public void onAction(@DestinationVariable String id,
                         NotificationActionMessage body,
                         Principal principal) {
        notificationApplicationService.handleAction(principal.getName(), id, body.getType());
    }
}
