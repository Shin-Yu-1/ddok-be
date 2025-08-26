package goorm.ddok.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chats")
                .setAllowedOriginPatterns("http://localhost:5173/*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
        registry.setUserDestinationPrefix("/user");  // 개인 메시지용
    }

//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/ws/chats")
//                .setAllowedOriginPatterns(
//                        "http://localhost:5173",     // 개발환경 (모든 포트)
//                        "https://localhost:5173"    // HTTPS 로컬)
//                )
//                .withSockJS();
//    }
//
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        // 구독할 채널의 prefix
//        registry.enableSimpleBroker("/sub");
//
//        // 서버로 메시지를 보낼 때 사용할 prefix
//        registry.setApplicationDestinationPrefixes("/pub");
//
//        // 개인 메시지를 위한 prefix (선택적)
//        registry.setUserDestinationPrefix("/user");
//    }
}
