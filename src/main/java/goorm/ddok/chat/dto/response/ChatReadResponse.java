package goorm.ddok.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "ChatReadResponse",
        description = "채팅 메세지 읽음 처리 응답"
)
public class ChatReadResponse {

    @Schema(description = "메세지 ID", example = "1")
    private Long messageId;
}
