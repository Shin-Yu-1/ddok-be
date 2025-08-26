package goorm.ddok.chat.controller;

import goorm.ddok.chat.domain.ChatMessage;
import goorm.ddok.chat.domain.ChatRoom;
import goorm.ddok.chat.dto.ChatMessageDto;
import goorm.ddok.chat.dto.request.ChatMessageRequest;
import goorm.ddok.chat.dto.response.ChatMessageResponse;
import goorm.ddok.chat.repository.ChatRepository;
import goorm.ddok.chat.service.ChatService;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatMessageController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chats/{roomId}/messages")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageRequest request, Principal principal) {
        try {
            String email = principal.getName();

            // Call the service to handle business logic and get the response
            ChatMessageResponse response = chatService.sendMessage(email, roomId, request);

            // Broadcast the message to subscribers
            messagingTemplate.convertAndSend("/sub/chats/" + roomId, response);

        } catch (Exception e) {
            log.error("WebSocket 메시지 처리 중 오류 발생: {}", e.getMessage());
        }
    }

    @MessageMapping("/chats/{roomId}/enter")
    public void enter(@DestinationVariable Long roomId, ChatMessageDto message) {
        message.setContentText(message.getSenderNickname() + "님이 입장했습니다.");
        message.setType(ChatMessageDto.MessageType.ENTER);
        messagingTemplate.convertAndSend("/sub/chats/" + roomId, message);
    }
}
