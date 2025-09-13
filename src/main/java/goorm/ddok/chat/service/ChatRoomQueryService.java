package goorm.ddok.chat.service;

import goorm.ddok.chat.domain.*;
import goorm.ddok.chat.dto.request.ChatMessageRequest;
import goorm.ddok.chat.dto.request.LastReadMessageRequest;
import goorm.ddok.chat.dto.response.*;
import goorm.ddok.chat.repository.ChatMessageRepository;
import goorm.ddok.chat.repository.ChatRepository;
import goorm.ddok.chat.repository.ChatRoomMemberRepository;
import goorm.ddok.chat.util.ChatMapper;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.repository.UserRepository;
import goorm.ddok.team.domain.Team;
import goorm.ddok.team.domain.TeamMemberRole;
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
public class ChatRoomQueryService {
    private final ChatRepository chatRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final ChatMapper chatMapper;

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
    }

    // 1:1 채팅 목록 조회
    public ChatListResponse getPrivateChats(Long userId, Pageable pageable) {
        User me = getUserById(userId);
        Page<ChatRoom> page = chatRepository.pageRoomsByMemberAndTypeOrderByRecent(
                me, ChatRoomType.PRIVATE, pageable);
        List<ChatRoomResponse> chats = chatMapper.toChatRoomDtoList(page.getContent(), me.getId());

        return ChatListResponse.builder()
                .chats(chats)
                .pagination(PaginationResponse.of(page))
                .build();
    }

    // 팀 채팅 목록 조회
    public ChatListResponse getTeamChats(Long userId, Pageable pageable) {
        User me = getUserById(userId);
        Page<ChatRoom> page = chatRepository.pageRoomsByMemberAndTypeOrderByRecent(
                me, ChatRoomType.GROUP, pageable);
        List<ChatRoomResponse> chats = chatMapper.toChatRoomDtoList(page.getContent(), me.getId());

        return ChatListResponse.builder()
                .chats(chats)
                .pagination(PaginationResponse.of(page))
                .build();
    }

    // 1:1 채팅 목록 검색
    public ChatListResponse searchPrivateChats(Long userId, String search, Pageable pageable) {
        User me = getUserById(userId);
        Page<ChatRoom> page = chatRepository.pagePrivateRoomsByMemberAndPeerNickname(
                me, search, pageable);
        List<ChatRoomResponse> chats = chatMapper.toChatRoomDtoList(page.getContent(), me.getId());

        return ChatListResponse.builder()
                .chats(chats)
                .pagination(PaginationResponse.of(page))
                .build();
    }

    // 팀 채팅 목록 검색
    public ChatListResponse searchTeamChats(Long userId, String search, Pageable pageable) {
        User me = getUserById(userId);
        Page<ChatRoom> page = chatRepository.pageGroupRoomsByMemberAndRoomOrMemberName(
                me, search, pageable);
        List<ChatRoomResponse> chats = chatMapper.toChatRoomDtoList(page.getContent(), me.getId());

        return ChatListResponse.builder()
                .chats(chats)
                .pagination(PaginationResponse.of(page))
                .build();
    }

    // 채팅 멤버 조회
    public ChatMembersResponse getRoomMembers(Long roomId, Long userId) {
        User me = getUserById(userId);
        ChatRoom roomRef = chatRepository.getReferenceById(roomId);

        boolean isMember = chatRoomMemberRepository.existsByRoomAndUserAndDeletedAtIsNull(roomRef, me);
        if (!isMember) throw new GlobalException(ErrorCode.NOT_CHAT_MEMBER);

        List<ChatRoomMember> members = chatRoomMemberRepository.findByRoomWithUser(roomRef);
        List<ChatMembersResponse.Member> dtos = members.stream()
                .map(m -> ChatMembersResponse.Member.builder()
                        .userId(m.getUser().getId())
                        .nickname(m.getUser().getNickname())
                        .profileImage(m.getUser().getProfileImageUrl())
                        .role(String.valueOf(m.getRole()))
                        .joinedAt(m.getCreatedAt())
                        .build())
                .toList();

        return ChatMembersResponse.builder()
                .members(dtos)
                .totalCount(dtos.size())
                .build();
    }
}
