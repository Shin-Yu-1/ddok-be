package goorm.ddok.chat.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomMemberId implements Serializable {
    private Long roomId;
    private Long userId;
}
