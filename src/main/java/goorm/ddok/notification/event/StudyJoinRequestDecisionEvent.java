package goorm.ddok.notification.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudyJoinRequestDecisionEvent {
    private final Long approverUserId;
    private final Long applicantUserId;
    private final Long studyId;
    private final String decision;
    private final Long notificationId;
}