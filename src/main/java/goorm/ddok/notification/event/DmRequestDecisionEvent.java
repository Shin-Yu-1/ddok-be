package goorm.ddok.notification.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DmRequestDecisionEvent {
    private final Long approverUserId;
    private final Long requesterUserId;
    private final String decision;
    private final Long notificationId;
}
