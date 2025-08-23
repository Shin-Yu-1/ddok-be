package goorm.ddok.chat.service;

import goorm.ddok.chat.domain.ChatRoom;
import goorm.ddok.chat.dto.response.*;
import goorm.ddok.chat.repository.ChatRepository;
import goorm.ddok.chat.util.ChatMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatMapper chatMapper;


    // 1:1 채팅 목록 조회
    public ChatListResponseDto getPrivateChats(Long userId, Pageable pageable) {

        Page<ChatRoom> chatRoomPage = chatRepository.findPrivateChatsByUserId(userId, pageable);

        List<ChatRoomDto> chatRoomDtos = chatRoomPage.getContent().stream()
                .map(chatRoom -> chatMapper.toChatRoomDto(chatRoom, userId))
                .collect(Collectors.toList());

        PaginationDto pagination = PaginationDto.builder()
                .currentPage(chatRoomPage.getNumber())
                .pageSize(chatRoomPage.getSize())
                .totalPages(chatRoomPage.getTotalPages())
                .totalItems(chatRoomPage.getTotalElements())
                .build();

        return ChatListResponseDto.builder()
                .chats(chatRoomDtos)
                .pagination(pagination)
                .build();
    }

    // 팀 채팅 목록 조회
    public ChatListResponseDto getTeamChats(Long userId, Pageable pageable) {

        Page<ChatRoom> chatRoomPage = chatRepository.findTeamChatsByUserId(userId, pageable);

        List<ChatRoomDto> chatRoomDtos = chatRoomPage.getContent().stream()
                .map(chatRoom -> chatMapper.toChatRoomDto(chatRoom, userId))
                .collect(Collectors.toList());

        PaginationDto pagination = PaginationDto.builder()
                .currentPage(chatRoomPage.getNumber())
                .pageSize(chatRoomPage.getSize())
                .totalPages(chatRoomPage.getTotalPages())
                .totalItems(chatRoomPage.getTotalElements())
                .build();

        return ChatListResponseDto.builder()
                .chats(chatRoomDtos)
                .pagination(pagination)
                .build();
    }
}
