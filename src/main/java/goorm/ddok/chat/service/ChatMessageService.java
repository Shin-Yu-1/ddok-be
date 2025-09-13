package goorm.ddok.chat.service;


import goorm.ddok.chat.domain.*;
import goorm.ddok.chat.dto.request.ChatMessageRequest;
import goorm.ddok.chat.dto.request.LastReadMessageRequest;
import goorm.ddok.chat.dto.response.*;
import goorm.ddok.chat.repository.ChatMessageRepository;
import goorm.ddok.chat.repository.ChatRepository;
import goorm.ddok.chat.repository.ChatRoomMemberRepository;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ChatMessageService {
    private final ChatRepository chatRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
    }

    private ChatRoom getRoomById(Long roomId) {
        return chatRepository.findById(roomId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    // 메시지 전송
    @Transactional
    public ChatMessageResponse sendMessage(Long userId, Long roomId, ChatMessageRequest request) {
        User sender = getUserById(userId);
        ChatRoom room = getRoomById(roomId);

        if (!chatRoomMemberRepository.existsByRoomAndUser(room, sender)) {
            throw new GlobalException(ErrorCode.NOT_CHAT_MEMBER);
        }

        ChatMessage replyTo = (request.getReplyToId() == null) ? null :
                chatMessageRepository.findById(request.getReplyToId()).orElse(null);

        ChatMessage saved = chatMessageRepository.save(ChatMessage.builder()
                .room(room)
                .sender(sender)
                .contentType(request.getContentType())
                .contentText(request.getContentText())
                .fileUrl(request.getFileUrl())
                .replyTo(replyTo)
                .build());

        room.setLastMessageAt(saved.getCreatedAt());

        return ChatMessageResponse.builder()
                .messageId(saved.getId())
                .roomId(room.getId())
                .senderId(sender.getId())
                .senderNickname(sender.getNickname())
                .contentType(saved.getContentType())
                .contentText(saved.getContentText())
                .fileUrl(saved.getFileUrl())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    // 채팅 메시지 목록/검색
    @Transactional
    public ChatMessageListResponse getChatMessages(Long userId, Long roomId, Pageable pageable, String search) {
        User me = getUserById(userId);
        ChatRoom room = getRoomById(roomId);

        if (!chatRoomMemberRepository.existsByRoomAndUserAndDeletedAtIsNull(room, me)) {
            throw new GlobalException(ErrorCode.NOT_CHAT_MEMBER);
        }

        Page<ChatMessageRepository.MessageView> page =
                (search == null || search.isBlank())
                        ? chatMessageRepository.pageViewsByRoom(room, pageable)
                        : chatMessageRepository.pageViewsByRoomAndKeyword(room, search, pageable);

        List<ChatMessageResponse> messages = page.getContent().stream()
                .map(v -> ChatMessageResponse.builder()
                        .messageId(v.getId())
                        .roomId(room.getId())
                        .senderId(v.getSenderId())
                        .senderNickname(v.getSenderNickname())
                        .contentType(v.getContentType())
                        .contentText(v.getContentText())
                        .fileUrl(v.getFileUrl())
                        .createdAt(v.getCreatedAt())
                        .build())
                .toList();

        return ChatMessageListResponse.builder()
                .messages(messages)
                .pagination(PaginationResponse.of(page))
                .build();
    }

    // 마지막 읽은 메시지 처리
    @Transactional
    public ChatReadResponse lastReadMessage(Long userId, Long roomId, LastReadMessageRequest request) {
        User me = getUserById(userId);
        ChatRoom roomRef = chatRepository.getReferenceById(roomId);

        ChatRoomMember member = chatRoomMemberRepository.findByRoomAndUser(roomRef, me)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_CHAT_MEMBER));

        member.setLastReadMessageId(request.getMessageId());

        return ChatReadResponse.builder()
                .messageId(request.getMessageId())
                .build();
    }
}
