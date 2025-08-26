package goorm.ddok.chat.controller;

import goorm.ddok.chat.domain.ChatMessage;
import goorm.ddok.chat.domain.ChatRoom;
import goorm.ddok.chat.dto.ChatMessageDto;
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
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;

    @MessageMapping("/chats/{roomId}/messages")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto message, Principal principal) {
        try {
            String email = principal.getName();

            // 유저 조회
            User sender = userRepository.findByEmail(email)
                    .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

            // 채팅방 조회
            ChatRoom chatRoom = chatRepository.findById(roomId)
                    .orElseThrow(() -> new GlobalException(ErrorCode.CHAT_ROOM_NOT_FOUND));

            // DB 저장
            ChatMessage saved = chatService.saveMessageAndUpdateRoom(
                    chatRoom,
                    sender.getId(),
                    message.getContentType(),
                    message.getContentText(),
                    message.getFileUrl(),
                    message.getReplyToId()
            );

            // 클라이언트로 전달할 DTO 구성
            ChatMessageDto response = ChatMessageDto.builder()
                    .roomId(saved.getRoomId())
                    .senderId(sender.getId())
                    .senderNickname(sender.getNickname())
                    .type(ChatMessageDto.MessageType.TALK)
                    .contentType(saved.getContentType())
                    .contentText(saved.getContentText())
                    .fileUrl(saved.getFileUrl())
                    .replyToId(saved.getReplyToId())
                    .build();

            // 5. 해당 채팅방 구독자에게 메시지 전송
            messagingTemplate.convertAndSend("/sub/chats/" + roomId, response);

        } catch (Exception e) {
            log.error("WebSocket 메시지 처리 중 오류 발생: {}", e.getMessage());
            // 필요하다면 에러 메시지를 클라이언트로 보낼 수 있음
        }
    }

    @MessageMapping("/chats/{roomID}/enter")
    public void enter(@DestinationVariable Long roomId, ChatMessageDto message) {
        message.setContentText(message.getSenderNickname() + "님이 입장했습니다.");
        message.setType(ChatMessageDto.MessageType.ENTER);
        // 수정: 올바른 브로커 경로 사용
        messagingTemplate.convertAndSend("/sub/chats/" + roomId, message);
    }
}
