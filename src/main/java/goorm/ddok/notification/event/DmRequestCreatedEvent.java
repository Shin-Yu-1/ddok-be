package goorm.ddok.notification.event;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DmRequestCreatedEvent {
    private final Long dmRequestId;
    private final Long fromUserId;
    private final String fromNickname;
    private final Long toUserId;
}
