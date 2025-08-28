package goorm.ddok.chat.dto.request;

import goorm.ddok.chat.domain.ChatContentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ChatMessageRequest", description = "채팅 메시지 전송 요청")
public class ChatMessageRequest {

    @NotNull
    @Schema(
            description = "메시지 타입",
            example = "TEXT",
            allowableValues = {"TEXT","IMAGE","FILE","SYSTEM"}
    )
    private ChatContentType contentType;

    @Schema(
            description = "텍스트 내용(TEXT일 때 필수)",
            example = "안녕하세요! 반갑습니다.",
            nullable = true
    )
    private String contentText;


    @Schema(
            description = "첨부파일",
            example = "http://...",
            nullable = true
    )
    private String fileUrl;

    @Schema(
            description = "답장 대상 메시지 ID",
            example = "42",
            nullable = true
    )
    private Long replyToId;
}
