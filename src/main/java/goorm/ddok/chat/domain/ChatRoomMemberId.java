package goorm.ddok.chat.domain;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomMemberId {
    private Long roomId;
    private Long userId;
}
