package goorm.ddok.global.websocket;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
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
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);

        StompCommand cmd = acc.getCommand();

        if (cmd == null) {
            return message;
        }

        switch (cmd) {
            case CONNECT -> {
                // CONNECT 에서만 Authorization 요구
                String authHeader = acc.getFirstNativeHeader("Authorization");

                if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
                    throw new GlobalException(ErrorCode.UNAUTHORIZED);
                }

                String token = authHeader.substring(7);
                Long userId = jwtTokenProvider.getUserIdFromToken(token);

                if (userId == null) {
                    throw new GlobalException(ErrorCode.UNAUTHORIZED);
                }

                acc.getSessionAttributes().put("userId", userId);
            }

            case SUBSCRIBE -> {
                Long userId = (Long) acc.getSessionAttributes().get("userId");

                if (userId == null) {
                    throw new GlobalException(ErrorCode.UNAUTHORIZED);
                }
            }

            case SEND -> {
                Long userId = (Long) acc.getSessionAttributes().get("userId");

                if (userId == null) {
                    throw new GlobalException(ErrorCode.UNAUTHORIZED);
                }

                String dest = acc.getDestination();
            }

            default -> { /* no-op */ }
        }

        return message;
    }
}