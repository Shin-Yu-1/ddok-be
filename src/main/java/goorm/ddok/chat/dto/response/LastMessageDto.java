package goorm.ddok.chat.dto.response;

import goorm.ddok.chat.domain.ChatContentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "마지막 메세지 정보")
public class LastMessageDto {

    @Schema(description = "메시지 ID")
    private Long messageId;

    @Schema(description = "메시지 타입")
    private ChatContentType type;

    @Schema(description = "메시지 내용")
    private String content;

    @Schema(description = "생성 시간")
    private Instant createdAt;

    @Schema(description = "발신자 ID")
    private Long senderId;
}
