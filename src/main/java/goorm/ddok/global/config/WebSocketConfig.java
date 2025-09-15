package goorm.ddok.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final goorm.ddok.global.websocket.StompAuthChannelInterceptor stompAuthChannelInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chats")
                .setAllowedOriginPatterns("http://localhost:5173", "http://127.0.0.1:5173", "https://www.deepdirect.site")
                .withSockJS();

        registry.addEndpoint("/ws/chats-ws")
                .setAllowedOriginPatterns("http://localhost:5173", "http://127.0.0.1:5173", "https://www.deepdirect.site");

        registry.addEndpoint("/ws/notifications")
                .setAllowedOriginPatterns("http://localhost:5173", "http://127.0.0.1:5173", "https://www.deepdirect.site");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub", "/queue");
        registry.setApplicationDestinationPrefixes("/pub");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }
}
