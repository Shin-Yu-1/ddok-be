package goorm.ddok.global.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Slf4j
@Component
public class StompEventsLogger {

    @EventListener
    public void onConnect(SessionConnectEvent e) {
        log.info("STOMP CONNECT headers={}", e.getMessage().getHeaders());
    }

    @EventListener
    public void onConnected(SessionConnectedEvent e) {
        log.info("STOMP CONNECTED");
    }

    @EventListener
    public void onSubscribe(SessionSubscribeEvent e) {
        var acc = StompHeaderAccessor.wrap(e.getMessage());
        log.info("STOMP SUBSCRIBE dest={}", acc.getDestination());
        Object userId = acc.getSessionAttributes() != null ? acc.getSessionAttributes().get("userId") : null;
        log.info("SUBSCRIBER userId={}", userId);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent e) {
        log.info("STOMP DISCONNECT status={}", e.getCloseStatus());
    }
}