package goorm.ddok.chat.dto.response;

import goorm.ddok.chat.dto.request.DmRequestDto;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DmRequestCreateResponse {
    Long dmRequestId;
    Long fromUserId;
    Long toUserId;
    String status;
    Long chatRoomId;
    Boolean dmRequestPending;
    String createdAt;

    public static DmRequestCreateResponse from(DmRequestDto d) {
        return DmRequestCreateResponse.builder()
                .dmRequestId(d.getDmRequestId())
                .fromUserId(d.getFromUserId())
                .toUserId(d.getToUserId())
                .status(d.getStatus())
                .chatRoomId(d.getChatRoomId())
                .dmRequestPending(d.getDmRequestPending())
                .createdAt(d.getCreatedAt().toString())
                .build();
    }
}
