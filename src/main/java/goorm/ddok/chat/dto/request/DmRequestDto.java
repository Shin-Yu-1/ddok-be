package goorm.ddok.chat.dto.request;


import goorm.ddok.chat.domain.DmRequest;
import goorm.ddok.chat.domain.DmRequestStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class DmRequestDto {
    Long dmRequestId;
    Long fromUserId;
    Long toUserId;
    String status;
    Long chatRoomId;
    Boolean dmRequestPending;
    Instant createdAt;

    public static DmRequestDto from(DmRequest e) {
        return DmRequestDto.builder()
                .dmRequestId(e.getId())
                .fromUserId(e.getFromUser().getId())
                .toUserId(e.getToUser().getId())
                .status(e.getStatus().name())
                .chatRoomId(e.getChatRoomId())
                .dmRequestPending(e.getStatus() == DmRequestStatus.PENDING)
                .createdAt(e.getCreatedAt())
                .build();
    }
}
