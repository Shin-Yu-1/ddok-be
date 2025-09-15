package goorm.ddok.notification.controller;

import goorm.ddok.notification.service.NotificationApplicationService;
import goorm.ddok.notification.ws.NotificationActionMessage;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Tag(name = "Notification", description = "수신함 API")
public class NotificationMessageController {

    private final NotificationApplicationService notificationApplicationService;

    @MessageMapping("/notifications/{id}/action")
    public void onAction(@DestinationVariable String id,
                         NotificationActionMessage body,
                         Principal principal) {
        notificationApplicationService.handleAction(principal.getName(), id, body.getType());
    }
}
