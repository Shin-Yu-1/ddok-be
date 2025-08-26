package goorm.ddok.global.websocket;

import goorm.ddok.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor { // ✅ implements 추가

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) { // ✅ 시그니처 일치
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);

        StompCommand cmd = acc.getCommand();
        if (cmd == null) return message;

        switch (cmd) {
            case CONNECT, SUBSCRIBE, SEND -> {
                String authHeader = acc.getFirstNativeHeader("Authorization");
                if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
                    throw new IllegalArgumentException("Missing Authorization header");
                }
                String token = authHeader.substring(7);

                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                if (userId == null) throw new IllegalArgumentException("Invalid token");

                // (옵션) SUBSCRIBE 시 roomId 권한 체크 지점
                if (cmd == StompCommand.SUBSCRIBE) {
                    String dest = acc.getDestination();
                    // dest 예: "/sub/chats.123" → roomId 파싱 후 membership 검증 로직 가능
                }

                // 세션에 userId 저장 → 컨트롤러에서 활용 가능
                acc.getSessionAttributes().put("userId", userId);
            }
            default -> { /* no-op */ }
        }

        return message;
    }
}