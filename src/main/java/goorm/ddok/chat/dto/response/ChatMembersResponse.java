package goorm.ddok.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "ChatMembersResponse",
        description = "채팅방 멤버"
)
public class ChatMembersResponse {
    private List<Member> members;
    private int totalCount;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Member {
        private Long userId;
        private String nickname;
        private String profileImage;
        private String role;
        private Instant joinedAt;
    }
}
