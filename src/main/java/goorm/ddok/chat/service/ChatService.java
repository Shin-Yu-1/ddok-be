package goorm.ddok.chat.service;

import goorm.ddok.chat.domain.ChatRoom;
import goorm.ddok.chat.domain.ChatRoomMember;
import goorm.ddok.chat.domain.ChatRoomType;
import goorm.ddok.chat.dto.response.*;
import goorm.ddok.chat.repository.ChatRepository;
import goorm.ddok.chat.repository.ChatRoomMemberRepository;
import goorm.ddok.chat.util.ChatMapper;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.member.repository.UserRepository;
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
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final ChatMapper chatMapper;


    // 1:1 채팅 목록 조회
    public ChatListResponseResponse getPrivateChats(String email, Pageable pageable) {
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND))
                .getId();

        Page<ChatRoom> chatRoomPage = chatRepository.findPrivateChatsByUserId(userId, pageable);

        List<ChatRoomResponse> chatRoomDtos = chatRoomPage.getContent().stream()
                .map(chatRoom -> chatMapper.toChatRoomDto(chatRoom, userId))
                .collect(Collectors.toList());

        PaginationResponse pagination = PaginationResponse.builder()
                .currentPage(chatRoomPage.getNumber())
                .pageSize(chatRoomPage.getSize())
                .totalPages(chatRoomPage.getTotalPages())
                .totalItems(chatRoomPage.getTotalElements())
                .build();

        return ChatListResponseResponse.builder()
                .chats(chatRoomDtos)
                .pagination(pagination)
                .build();
    }

    // 팀 채팅 목록 조회
    public ChatListResponseResponse getTeamChats(String email, Pageable pageable) {

        Long userId = userRepository.findByEmail(email).orElseThrow(() ->
                new GlobalException(ErrorCode.USER_NOT_FOUND)).getId();

        Page<ChatRoom> chatRoomPage = chatRepository.findTeamChatsByUserId(userId, pageable);

        List<ChatRoomResponse> chatRoomDtos = chatRoomPage.getContent().stream()
                .map(chatRoom -> chatMapper.toChatRoomDto(chatRoom, userId))
                .collect(Collectors.toList());

        PaginationResponse pagination = PaginationResponse.builder()
                .currentPage(chatRoomPage.getNumber())
                .pageSize(chatRoomPage.getSize())
                .totalPages(chatRoomPage.getTotalPages())
                .totalItems(chatRoomPage.getTotalElements())
                .build();

        return ChatListResponseResponse.builder()
                .chats(chatRoomDtos)
                .pagination(pagination)
                .build();
    }

    // 채팅 검색 - 채팅방 이름, 팀원 닉네임으로 검색
    public ChatListResponseResponse searchPrivateChats(String email, String search, Pageable pageable) {

        Long userId = userRepository.findByEmail(email).orElseThrow(() ->
                new GlobalException(ErrorCode.USER_NOT_FOUND)).getId();

        Page<ChatRoom> chatRoomPage = chatRepository.searchChatsByKeyword(userId, search.trim(), ChatRoomType.PRIVATE, pageable);

        List<ChatRoomResponse> chatRoomDtos = chatRoomPage.getContent().stream()
                .map(chatRoom -> chatMapper.toChatRoomDto(chatRoom, userId))
                .collect(Collectors.toList());

        PaginationResponse pagination = PaginationResponse.builder()
                .currentPage(chatRoomPage.getNumber())
                .pageSize(chatRoomPage.getSize())
                .totalPages(chatRoomPage.getTotalPages())
                .totalItems(chatRoomPage.getTotalElements())
                .build();

        return ChatListResponseResponse.builder()
                .chats(chatRoomDtos)
                .pagination(pagination)
                .build();
    }

    public ChatListResponseResponse searchTeamChats(String email, String search, Pageable pageable) {

        Long userId = userRepository.findByEmail(email).orElseThrow(() ->
                new GlobalException(ErrorCode.USER_NOT_FOUND)).getId();

        Page<ChatRoom> chatRoomPage = chatRepository.searchChatsByKeyword(userId, search.trim(), ChatRoomType.GROUP, pageable);

        List<ChatRoomResponse> chatRoomDtos = chatRoomPage.getContent().stream()
                .map(chatRoom -> chatMapper.toChatRoomDto(chatRoom, userId))
                .collect(Collectors.toList());

        PaginationResponse pagination = PaginationResponse.builder()
                .currentPage(chatRoomPage.getNumber())
                .pageSize(chatRoomPage.getSize())
                .totalPages(chatRoomPage.getTotalPages())
                .totalItems(chatRoomPage.getTotalElements())
                .build();

        return ChatListResponseResponse.builder()
                .chats(chatRoomDtos)
                .pagination(pagination)
                .build();
    }

    public ChatMembersResponse getRoomMembers(Long roomID, String email) {

        Long userId = userRepository.findByEmail(email).orElseThrow(() ->
                new GlobalException(ErrorCode.USER_NOT_FOUND)).getId();

        List<ChatRoomMember> roomMemberList = chatRoomMemberRepository.findAllByRoomIdWithUser(roomID);

        List<ChatMembersResponse.Member> members = roomMemberList.stream()
                .map(roomMember -> ChatMembersResponse.Member.builder()
                        .userId(roomMember.getUserId().getId())
                        .nickname(roomMember.getUserId().getNickname())
                        .profileImage(roomMember.getUserId().getProfileImageUrl())
                        .role(String.valueOf(roomMember.getRole()))
                        .joinedAt(roomMember.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ChatMembersResponse.builder()
                .members(members)
                .totalCount(members.size())
                .build();
    }
}
