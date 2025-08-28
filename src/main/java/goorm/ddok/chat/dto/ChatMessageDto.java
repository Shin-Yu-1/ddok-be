package goorm.ddok.chat.dto;

import goorm.ddok.chat.domain.ChatContentType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {

    private MessageType type;
    private Long roomId;
    private Long senderId;
    private String senderNickname;
    private String contentText;
    private String fileUrl;
    private Long replyToId;
    private ChatContentType contentType;

    public enum MessageType {
        ENTER, TALK
    }
}
