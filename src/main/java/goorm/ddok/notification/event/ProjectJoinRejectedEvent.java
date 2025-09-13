package goorm.ddok.notification.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectJoinRejectedEvent {
    private final Long applicantUserId;
    private final Long projectId;
    private final String projectTitle;
    private final Long rejectorUserId;
    private final Long applicationId;
}