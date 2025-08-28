package goorm.ddok.chat.dto.response;

import goorm.ddok.chat.domain.ChatContentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "ChatMessageResponse",
        description = "채팅 메세지 저장 응답"
)
public class ChatMessageResponse {

    @Schema(description = "메세지 ID", example = "1")
    private Long messageId;

    @Schema(description = "채팅방 ID", example = "1")
    private Long roomId;

    @Schema(description = "보낸 사람 ID", example = "1")
    private Long senderId;

    @Schema(description = "보낸 사람 닉네임", example = "당신은사랑받기위해")
    private String senderNickname;

    @Schema(description = "채팅 메세지 타입", example = "TEXT")
    private ChatContentType contentType;

    @Schema(description = "채팅 메세지 내용", example = "안녕하세요! 반갑습니다.")
    private String contentText;

    @Schema(description = "파일 url", example = "http://...")
    private String fileUrl;

    @Schema(description = "메세지 생성 시간", example = "2025-08-20T15:45:00Z")
    private Instant createdAt;
}
