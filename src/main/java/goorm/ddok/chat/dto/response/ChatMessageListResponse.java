package goorm.ddok.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "ChatMessageListResponse",
        description = "채팅 메세지 리스트"
)
public class ChatMessageListResponse {

    @Schema(description = "채팅 메세지 목록")
    private List<ChatMessageResponse> messages;

    @Schema(description = "페이징 정보")
    private PaginationResponse pagination;
}
