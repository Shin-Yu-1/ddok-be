package goorm.ddok.chat.ws;

import goorm.ddok.chat.domain.ChatContentType;
import goorm.ddok.chat.dto.request.ChatMessageRequest;
import goorm.ddok.chat.dto.response.ChatMessageResponse;
import goorm.ddok.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 클라이언트 SEND:  /pub/chats.{roomId}.send
     * 서버 브로드캐스트: /sub/chats.{roomId}
     */
    @MessageMapping("/chats.{roomId}.send")
    public void send(
            @DestinationVariable Long roomId,
            ChatMessageRequest payload,
            @Header("Authorization") String authorization
    ) {
        // 채널 인터셉터에서 검증한 userId를 세션에서 꺼내는 방법도 가능하지만,
        // 여기서는 기존 ChatService를 재사용하기 위해 email 대신 토큰→이메일 변환 로직을 JwtTokenProvider에 두거나,
        // sendMessage 시그니처를 userId 버전으로 하나 더 만드세요.
        // 아래 예시는 간단화를 위해 payload에 contentType null일 때 TEXT 기본값 보정만 함.
        if (payload.getContentType() == null) {
            payload.setContentType(ChatContentType.TEXT);
        }

        // 토큰 → 이메일/유저 식별 (JwtTokenProvider에 맞춰 구현한다고 가정)
        // String email = jwtTokenProvider.getEmailFromToken(authorization.substring(7));
        // ChatMessageResponse saved = chatService.sendMessage(email, roomId, payload);

        // 이미 ChatService가 email 기반이라면, HTTP에서 사용하던 방식과 동일하게 호출하도록 JwtTokenProvider 주입 후 처리
        // 여기선 인터페이스만 보여주고, 실제 email 획득은 위 주석처럼 구현.
        ChatMessageResponse saved = /* 위 주석 로직으로 교체하세요 */ null;

        // 목적지 브로드캐스트
        messagingTemplate.convertAndSend("/sub/chats." + roomId, saved);
    }
}
