package goorm.ddok.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "LastReadMessageRequest", description = "마지막으로 읽은 메세지 ID")
public class LastReadMessageRequest {

    @NotNull(message = "messageId는 필수입니다.")
    @Schema(
            description = "메세지 ID",
            example = "1"
    )
    private Long messageId;
}
