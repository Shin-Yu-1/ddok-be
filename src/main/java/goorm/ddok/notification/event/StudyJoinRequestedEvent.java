package goorm.ddok.notification.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudyJoinRequestedEvent {
    private final Long applicationId;
    private final Long applicantUserId;
    private final String applicantNickname;
    private final Long studyId;
    private final String studyTitle;
    private final String appliedRole;
    private final Long ownerUserId;
}