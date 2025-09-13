package goorm.ddok.notification.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectJoinRequestDecisionEvent {
    private final Long approverUserId;
    private final Long applicantUserId;
    private final Long projectId;
    private final String decision;
    private final Long notificationId;
}
