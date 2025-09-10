package goorm.ddok.notification.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudyJoinRejectedEvent {
    private final Long applicantUserId;
    private final Long studyId;
    private final String studyTitle;
    private final Long rejectorUserId;
    private final Long applicationId;
}
