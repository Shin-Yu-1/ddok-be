package goorm.ddok.notification.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudyJoinApprovedEvent {
    private final Long applicantUserId;
    private final Long studyId;
    private final String studyTitle;
    private final Long approverUserId;
}
