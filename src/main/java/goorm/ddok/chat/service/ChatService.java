package goorm.ddok.chat.service;

import goorm.ddok.chat.domain.ChatMessage;
import goorm.ddok.chat.domain.ChatRoom;
import goorm.ddok.chat.domain.ChatRoomMember;
import goorm.ddok.chat.domain.ChatRoomType;
import goorm.ddok.chat.dto.request.ChatMessageRequest;
import goorm.ddok.chat.dto.response.*;
import goorm.ddok.chat.repository.ChatMessageRepository;
import goorm.ddok.chat.repository.ChatRepository;
import goorm.ddok.chat.repository.ChatRoomMemberRepository;
import goorm.ddok.chat.util.ChatMapper;
import goorm.ddok.chat.util.PaginationUtil;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatMapper chatMapper;

    // 1:1 채팅 목록 조회
    public ChatListResponseResponse getPrivateChats(String email, Pageable pageable) {

        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND))
                .getId();

        // 사용자가 속한 채팅방 조회 조회
        List<ChatRoomMember> userRooms = chatRoomMemberRepository.findAllByUserIdAndDeletedAtIsNull(userId);

        // roomId만 추출
        List<Long> roomIds = userRooms.stream()
                .map(ChatRoomMember::getRoomId)
                .distinct() // 중복 제거 (필요시)
                .toList();

        // 1:1 채팅방만 추출
        List<ChatRoom> chatRooms = chatRepository.findByIdInAndRoomType(roomIds, ChatRoomType.PRIVATE);

        // 최근 대화 순서
        List<ChatMessage> recentMessages = chatMessageRepository.findAllByRoomIdInAndDeletedAtIsNullOrderByCreatedAtDesc(roomIds);

        // 각 채팅방마다 가장 최근 메시지를 Map에 저장
        Map<Long, ChatMessage> lastMessageMap = recentMessages.stream()
                .collect(Collectors.toMap(
                        ChatMessage::getRoomId,
                        Function.identity(),
                        (m1, m2) -> m1.getCreatedAt().isAfter(m2.getCreatedAt()) ? m1 : m2 // 최신 메시지 유지
                ));

        // 정렬
        chatRooms.sort(Comparator
                .comparing((ChatRoom room) -> {
                    ChatMessage lastMessage = lastMessageMap.get(room.getId());
                    return lastMessage != null ? lastMessage.getCreatedAt() : room.getCreatedAt();
                }, Comparator.reverseOrder()) // 최신순
                .thenComparing(ChatRoom::getCreatedAt, Comparator.reverseOrder()) // 생성일 순
        );

        // 페이징 처리
        Page<ChatRoom> chatRoomPage = PaginationUtil.paginate(chatRooms, pageable);

        // DTO 변환
        List<ChatRoomResponse> chatRoomDtos = chatMapper.toChatRoomDtoList(chatRoomPage.getContent(), userId);
        PaginationResponse pagination = PaginationUtil.from(chatRoomPage);

        return ChatListResponseResponse.builder()
                .chats(chatRoomDtos)
                .pagination(pagination)
                .build();
    }

    // 팀 채팅 목록 조회
    public ChatListResponseResponse getTeamChats(String email, Pageable pageable) {

        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND))
                .getId();

        // 사용자가 속한 채팅방 조회 조회
        List<ChatRoomMember> userRooms = chatRoomMemberRepository.findAllByUserIdAndDeletedAtIsNull(userId);

        // roomId만 추출
        List<Long> roomIds = userRooms.stream()
                .map(ChatRoomMember::getRoomId)
                .distinct() // 중복 제거 (필요시)
                .toList();

        // 팀 채팅방 추출
        List<ChatRoom> chatRooms = chatRepository.findByIdInAndRoomType(roomIds, ChatRoomType.GROUP);

        // 최근 대화 순서
        List<ChatMessage> recentMessages = chatMessageRepository.findAllByRoomIdInAndDeletedAtIsNullOrderByCreatedAtDesc(roomIds);

        // 각 채팅방마다 가장 최근 메시지를 Map에 저장
        Map<Long, ChatMessage> lastMessageMap = recentMessages.stream()
                .collect(Collectors.toMap(
                        ChatMessage::getRoomId,
                        Function.identity(),
                        (m1, m2) -> m1.getCreatedAt().isAfter(m2.getCreatedAt()) ? m1 : m2 // 최신 메시지 유지
                ));

        // 페이징 처리
        Page<ChatRoom> chatRoomPage = PaginationUtil.paginate(chatRooms, pageable);

        // 정렬
        chatRooms.sort(Comparator
                .comparing((ChatRoom room) -> {
                    ChatMessage lastMessage = lastMessageMap.get(room.getId());
                    return lastMessage != null ? lastMessage.getCreatedAt() : room.getCreatedAt();
                }, Comparator.reverseOrder()) // 최신순
                .thenComparing(ChatRoom::getCreatedAt, Comparator.reverseOrder()) // 생성일 순
        );

        // DTO 변환
        List<ChatRoomResponse> chatRoomDtos = chatMapper.toChatRoomDtoList(chatRoomPage.getContent(), userId);
        PaginationResponse pagination = PaginationUtil.from(chatRoomPage);

        return ChatListResponseResponse.builder()
                .chats(chatRoomDtos)
                .pagination(pagination)
                .build();
    }

    // 채팅 검색 - 채팅방 이름으로 검색 (닉네임 검색은 별도 구현 필요)
    public ChatListResponseResponse searchPrivateChats(String email, String search, Pageable pageable) {

        Long userId = userRepository.findByEmail(email).orElseThrow(() ->
                new GlobalException(ErrorCode.USER_NOT_FOUND)).getId();

        // 사용자가 속한 채팅방 Id 조회
        List<Long> myRoomIds = chatRoomMemberRepository.findAllByUserIdAndDeletedAtIsNull(userId).stream()
                .map(ChatRoomMember::getRoomId)
                .distinct()
                .toList();

        // 닉네임에 검색어가 포함된 사용자 목록 조회
        List<Long> matchedUserIds = userRepository.findAllByNicknameContaining(search).stream()
                .map(User::getId)
                .filter(id -> !id.equals(userId)) // 자기 자신 제외
                .toList();

        // 겹치는 채팅방만 추출
        List<Long> matchedRoomIds = chatRoomMemberRepository.findAllByUserIdInAndDeletedAtIsNull(matchedUserIds).stream()
                .map(ChatRoomMember::getRoomId)
                .filter(myRoomIds::contains) // 내가 속한 채팅방과 교집합
                .distinct()
                .toList();

        // 1:1 채팅방 목록 조회
        List<ChatRoom> filteredRooms = chatRepository.findByIdInAndRoomType(matchedRoomIds, ChatRoomType.PRIVATE);

        // 최근 대화 순서
        List<ChatMessage> recentMessages = chatMessageRepository.findAllByRoomIdInAndDeletedAtIsNullOrderByCreatedAtDesc(matchedRoomIds);

        // 각 채팅방마다 가장 최근 메시지를 Map에 저장
        Map<Long, ChatMessage> lastMessageMap = recentMessages.stream()
                .collect(Collectors.toMap(
                        ChatMessage::getRoomId,
                        Function.identity(),
                        (m1, m2) -> m1.getCreatedAt().isAfter(m2.getCreatedAt()) ? m1 : m2 // 최신 메시지 유지
                ));

        // 페이징 처리
        Page<ChatRoom> chatRoomPage = PaginationUtil.paginate(filteredRooms, pageable);

        // 정렬
        filteredRooms.sort(Comparator
                .comparing((ChatRoom room) -> {
                    ChatMessage lastMessage = lastMessageMap.get(room.getId());
                    return lastMessage != null ? lastMessage.getCreatedAt() : room.getCreatedAt();
                }, Comparator.reverseOrder()) // 최신순
                .thenComparing(ChatRoom::getCreatedAt, Comparator.reverseOrder()) // 생성일 순
        );

        // DTO 변환
        List<ChatRoomResponse> chatRoomDtos = chatMapper.toChatRoomDtoList(chatRoomPage.getContent(), userId);
        PaginationResponse pagination = PaginationUtil.from(chatRoomPage);

        return ChatListResponseResponse.builder()
                .chats(chatRoomDtos)
                .pagination(pagination)
                .build();
    }

    public ChatListResponseResponse searchTeamChats(String email, String search, Pageable pageable) {

        Long userId = userRepository.findByEmail(email).orElseThrow(() ->
                new GlobalException(ErrorCode.USER_NOT_FOUND)).getId();

        // 사용자가 속한 채팅방 Id 및 검색어
        List<Long> myRoomIds = chatRoomMemberRepository.findAllByUserIdAndDeletedAtIsNull(userId).stream()
                .map(ChatRoomMember::getRoomId)
                .distinct()
                .toList();

        // 사용자가 속한 채팅방 없을 경우
        if (myRoomIds.isEmpty()) {
            return ChatListResponseResponse.builder()
                    .chats(Collections.emptyList())
                    .pagination(PaginationResponse.builder()
                            .currentPage(pageable.getPageNumber())
                            .pageSize(pageable.getPageSize())
                            .totalPages(0)
                            .totalItems(0L)
                            .build())
                    .build();
        }

        // 닉네임에 검색어가 포함된 사용자 목록 조회
        List<Long> matchedUserIds = userRepository.findAllByNicknameContaining(search).stream()
                .map(User::getId)
                .filter(id -> !id.equals(userId))
                .toList();

        // 겹치는 채팅방만 추출
        List<Long> matchedRoomIds = chatRoomMemberRepository.findAllByUserIdInAndDeletedAtIsNull(matchedUserIds).stream()
                .map(ChatRoomMember::getRoomId)
                .filter(myRoomIds::contains)
                .distinct()
                .toList();

        // 팀 채팅방 목록 조회
        List<ChatRoom> chatRooms = chatRepository.findByIdInAndRoomTypeAndNameContaining(myRoomIds, ChatRoomType.GROUP, search);
        List<ChatRoom> filteredRooms = chatRepository.findByIdInAndRoomType(matchedRoomIds, ChatRoomType.GROUP);

        // 리스트 중복 제거
        List<ChatRoom> mergedRooms = new ArrayList<>();
        mergedRooms.addAll(chatRooms);
        mergedRooms.addAll(filteredRooms);

        Map<Long, ChatRoom> uniqueRoomsMap = new LinkedHashMap<>();
        for (ChatRoom room : mergedRooms) {
            uniqueRoomsMap.putIfAbsent(room.getId(), room);
        }
        List<ChatRoom> uniqueRooms = new ArrayList<>(uniqueRoomsMap.values());

        // uniqueRooms이 없을 때
        if (uniqueRooms.isEmpty()) {
            return ChatListResponseResponse.builder()
                    .chats(Collections.emptyList())
                    .pagination(PaginationResponse.builder()
                            .currentPage(pageable.getPageNumber())
                            .pageSize(pageable.getPageSize())
                            .totalPages(0)
                            .totalItems(0L)
                            .build())
                    .build();
        }

        // 최종 채팅방 ID 기준으로 마지막 메시지 가져오기
        List<Long> finalRoomIds = uniqueRooms.stream()
                .map(ChatRoom::getId)
                .toList();

        // 최근 대화 순서
        List<ChatMessage> recentMessages = chatMessageRepository.findAllByRoomIdInAndDeletedAtIsNullOrderByCreatedAtDesc(finalRoomIds);

        // 각 채팅방마다 가장 최근 메시지를 Map에 저장
        Map<Long, ChatMessage> lastMessageMap = recentMessages.stream()
                .collect(Collectors.toMap(
                        ChatMessage::getRoomId,
                        Function.identity(),
                        (m1, m2) -> m1.getCreatedAt().isAfter(m2.getCreatedAt()) ? m1 : m2 // 최신 메시지 유지
                ));

        // 페이징 처리
        Page<ChatRoom> chatRoomPage = PaginationUtil.paginate(uniqueRooms, pageable);

        // 정렬
        uniqueRooms.sort(Comparator
                .comparing((ChatRoom room) -> {
                    ChatMessage lastMessage = lastMessageMap.get(room.getId());
                    return lastMessage != null ? lastMessage.getCreatedAt() : room.getCreatedAt();
                }, Comparator.reverseOrder()) // 최신순
                .thenComparing(ChatRoom::getCreatedAt, Comparator.reverseOrder()) // 생성일 순
        );

        // DTO 변환
        List<ChatRoomResponse> chatRoomDtos = chatMapper.toChatRoomDtoList(chatRoomPage.getContent(), userId);
        PaginationResponse pagination = PaginationUtil.from(chatRoomPage);

        return ChatListResponseResponse.builder()
                .chats(chatRoomDtos)
                .pagination(pagination)
                .build();
    }

    public ChatMembersResponse getRoomMembers(Long roomID, String email) {
        Long userId = userRepository.findByEmail(email).orElseThrow(() ->
                new GlobalException(ErrorCode.USER_NOT_FOUND)).getId();

        // 채팅방 멤버 조회 (User 정보 포함)
        List<Object[]> roomMemberWithUserList = chatRoomMemberRepository.findAllByRoomIdWithUserInfo(roomID);

        List<ChatMembersResponse.Member> members = roomMemberWithUserList.stream()
                .map(result -> {
                    ChatRoomMember roomMember = (ChatRoomMember) result[0];
                    User user = (User) result[1];

                    return ChatMembersResponse.Member.builder()
                            .userId(roomMember.getUserId()) // 실제 사용자 ID
                            .nickname(user.getNickname())
                            .profileImage(user.getProfileImageUrl())
                            .role(String.valueOf(roomMember.getRole()))
                            .joinedAt(roomMember.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return ChatMembersResponse.builder()
                .members(members)
                .totalCount(members.size())
                .build();
    }

    // 대안: 더 효율적인 방법 (별도 메서드로 분리)
    public ChatMembersResponse getRoomMembersOptimized(Long roomID, String email) {
        Long userId = userRepository.findByEmail(email).orElseThrow(() ->
                new GlobalException(ErrorCode.USER_NOT_FOUND)).getId();

        // 1. 채팅방 멤버의 gId 목록 조회
        List<Long> memberIds = chatRoomMemberRepository.findUserIdsByRoomId(roomID);

        // 2. 해당 사용자들의 정보 조회
        List<User> users = userRepository.findAllById(memberIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 3. 멤버 정보 조회
        List<ChatRoomMember> roomMembers = chatRoomMemberRepository.findAllByRoomIdAndDeletedAtIsNull(roomID);

        List<ChatMembersResponse.Member> members = roomMembers.stream()
                .map(roomMember -> {
                    User user = userMap.get(roomMember.getUserId());
                    return ChatMembersResponse.Member.builder()
                            .userId(roomMember.getUserId())
                            .nickname(user != null ? user.getNickname() : "Unknown User")
                            .profileImage(user != null ? user.getProfileImageUrl() : null)
                            .role(String.valueOf(roomMember.getRole()))
                            .joinedAt(roomMember.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return ChatMembersResponse.builder()
                .members(members)
                .totalCount(members.size())
                .build();
    }

    // 메세지 전송
    @Transactional
    public ChatMessageResponse sendMessage(String email, Long roomId, ChatMessageRequest request) {
        User sender = userRepository.findByEmail(email).orElseThrow(() ->
                new GlobalException(ErrorCode.USER_NOT_FOUND));

        ChatRoom chatRoom = chatRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // 사용자가 해당 채팅방의 멤버인지 확인
        boolean isMember = chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, sender.getId());
        if (!isMember) {
            throw new IllegalArgumentException("채팅방에 참여하지 않은 사용자입니다.");
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .roomId(roomId)
                .senderId(sender.getId())
                .contentType(request.getContentType())
                .contentText(request.getContentText())
                .fileUrl(request.getFileUrl())
                .replyToId(request.getReplyToId())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // 채팅방의 마지막 메시지 시간 업데이트
        chatRoom.setLastMessageAt(savedMessage.getCreatedAt());
        chatRepository.save(chatRoom);

        return ChatMessageResponse.builder()
                .messageId(savedMessage.getId())
                .roomId(savedMessage.getRoomId())
                .senderId(savedMessage.getSenderId())
                .senderNickname(sender.getNickname())
                .contentType(savedMessage.getContentType())
                .contentText(savedMessage.getContentText())
                .fileUrl(savedMessage.getFileUrl())
                .createdAt(savedMessage.getCreatedAt())
                .build();
    }
}