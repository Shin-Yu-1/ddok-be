package goorm.ddok.chat.service;

import goorm.ddok.chat.domain.*;
import goorm.ddok.chat.dto.request.ChatMessageRequest;
import goorm.ddok.chat.dto.request.LastReadMessageRequest;
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

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
    }

    private ChatRoom getRoomById(Long roomId) {
        return chatRepository.findById(roomId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    // 1:1 채팅 목록 조회
    public ChatListResponse getPrivateChats(String email, Pageable pageable) {

        User me = getUserByEmail(email);

        Page<ChatRoom> page = chatRepository.pageRoomsByMemberAndTypeOrderByRecent(
                me, ChatRoomType.PRIVATE, pageable);

        List<ChatRoomResponse> chats = chatMapper.toChatRoomDtoList(page.getContent(), me.getId());

        return ChatListResponse.builder()
                .chats(chats)
                .pagination(PaginationResponse.of(page))
                .build();
    }

    // 팀 채팅 목록 조회
    public ChatListResponse getTeamChats(String email, Pageable pageable) {

        User me = getUserByEmail(email);

        Page<ChatRoom> page = chatRepository.pageRoomsByMemberAndTypeOrderByRecent(
                me, ChatRoomType.GROUP, pageable);

        List<ChatRoomResponse> chats = chatMapper.toChatRoomDtoList(page.getContent(), me.getId());

        return ChatListResponse.builder()
                .chats(chats)
                .pagination(PaginationResponse.of(page))
                .build();
    }

    // 1:1 채팅 목록 검색
    public ChatListResponse searchPrivateChats(String email, String search, Pageable pageable) {

        User me = getUserByEmail(email);

        Page<ChatRoom> page = chatRepository.pagePrivateRoomsByMemberAndPeerNickname(
                me, search, pageable);

        List<ChatRoomResponse> chats = chatMapper.toChatRoomDtoList(page.getContent(), me.getId());
        return ChatListResponse.builder()
                .chats(chats)
                .pagination(PaginationResponse.of(page))
                .build();
    }

    // 팀 채팅 목록 검색
    public ChatListResponse searchTeamChats(String email, String search, Pageable pageable) {

        Long userId = userRepository.findByEmail(email).orElseThrow(() ->
                new GlobalException(ErrorCode.USER_NOT_FOUND)).getId();

        // 사용자가 속한 채팅방 Id 및 검색어
        List<Long> myRoomIds = chatRoomMemberRepository.findAllByUser_IdAndDeletedAtIsNull(userId).stream()
                .map(ChatRoomMember::getRoomId)
                .distinct()
                .toList();

        // 사용자가 속한 채팅방 없을 경우
        if (myRoomIds.isEmpty()) {
            return ChatListResponse.builder()
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
        List<Long> matchedRoomIds = chatRoomMemberRepository.findAllByUser_IdInAndDeletedAtIsNull(matchedUserIds).stream()
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
            return ChatListResponse.builder()
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
        List<ChatMessage> recentMessages = chatMessageRepository.findAllByRoom_IdInAndDeletedAtIsNullOrderByCreatedAtDesc(finalRoomIds);

        // 각 채팅방마다 가장 최근 메시지를 Map에 저장
        Map<Long, ChatMessage> lastMessageMap = recentMessages.stream()
                .collect(Collectors.toMap(
                        ChatMessage::getRoomId,
                        Function.identity(),
                        (m1, m2) -> m1.getCreatedAt().isAfter(m2.getCreatedAt()) ? m1 : m2 // 최신 메시지 유지
                ));

        // 정렬
        uniqueRooms.sort(Comparator
                .comparing((ChatRoom room) -> {
                    ChatMessage lastMessage = lastMessageMap.get(room.getId());
                    return lastMessage != null ? lastMessage.getCreatedAt() : room.getCreatedAt();
                }, Comparator.reverseOrder()) // 최신순
                .thenComparing(ChatRoom::getCreatedAt, Comparator.reverseOrder()) // 생성일 순
        );

        // 페이징 처리
        Page<ChatRoom> chatRoomPage = PaginationUtil.paginate(uniqueRooms, pageable);

        // DTO 변환
        List<ChatRoomResponse> chatRoomDtos = chatMapper.toChatRoomDtoList(chatRoomPage.getContent(), userId);
        PaginationResponse pagination = PaginationUtil.from(chatRoomPage);

        return ChatListResponse.builder()
                .chats(chatRoomDtos)
                .pagination(pagination)
                .build();
    }

    // 채팅 멤버 조회
    public ChatMembersResponse getRoomMembers(Long roomID, String email) {
        Long userId = userRepository.findByEmail(email).orElseThrow(() ->
                new GlobalException(ErrorCode.USER_NOT_FOUND)).getId();

        boolean isMember = chatRoomMemberRepository.existsByRoom_IdAndUser_IdAndDeletedAtIsNull(roomID, userId);

        if (!isMember) {
            throw new GlobalException(ErrorCode.NOT_CHAT_MEMBER);
        }

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

    // 메세지 전송
    public ChatMessageResponse sendMessage(String email, Long roomId, ChatMessageRequest request) {
        User sender = userRepository.findByEmail(email).orElseThrow(() ->
                new GlobalException(ErrorCode.USER_NOT_FOUND));

        ChatRoom chatRoom = chatRepository.findById(roomId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 사용자가 해당 채팅방의 멤버인지 확인
        boolean isMember = chatRoomMemberRepository.existsByRoom_IdAndUser_Id(roomId, sender.getId());
        if (!isMember) {
            throw new GlobalException(ErrorCode.NOT_CHAT_MEMBER);
        }

        // 메시지 저장 및 채팅방 시간 갱신
        ChatMessage savedMessage = saveMessageAndUpdateRoom(
                chatRoom,
                sender.getId(),
                request.getContentType(),
                request.getContentText(),
                request.getFileUrl(),
                request.getReplyToId()
        );


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

    @Transactional
    public ChatMessage saveMessageAndUpdateRoom(
            ChatRoom chatRoom,
            Long senderId,
            ChatContentType contentType,
            String contentText,
            String fileUrl,
            Long replyToId
    ) {
        User sender = userRepository.findById(senderId).orElseThrow(() ->
                new GlobalException(ErrorCode.USER_NOT_FOUND));

        ChatMessage replyTo = null;
        if (replyToId != null) {
            replyTo = chatMessageRepository.findById(replyToId)
                    .orElse(null);
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .room(chatRoom)
                .sender(sender)
                .contentType(contentType)
                .contentText(contentText)
                .fileUrl(fileUrl)
                .replyTo(replyTo)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        chatRoom.setLastMessageAt(savedMessage.getCreatedAt());
        chatRepository.save(chatRoom);

        return savedMessage;
    }

    // 채팅 메세지 키워드 검색 조회
    @Transactional
    public ChatMessageListResponse getChatMessages(String email, Long roomId, Pageable pageable, String search) {
        User sender = userRepository.findByEmail(email).orElseThrow(() ->
                new GlobalException(ErrorCode.USER_NOT_FOUND));

        ChatRoom chatRoom = chatRepository.findById(roomId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        Integer size = pageable.getPageSize();
        List<ChatMessage> messages = (search == null || search.trim().isEmpty())
                ? chatMessageRepository.findAllByRoom_IdAndDeletedAtIsNullOrderByCreatedAtDesc(roomId)
                : chatMessageRepository.findAllByRoom_IdAndContentTextContainingAndDeletedAtIsNullOrderByCreatedAtDesc(roomId, search);

        Page<ChatMessage> chatMessagePage = PaginationUtil.paginate(messages, pageable);
        PaginationResponse pagination = PaginationUtil.from(chatMessagePage);

        List<ChatMessageResponse> messageResponses = chatMessagePage.getContent().stream()
                .map(message -> ChatMessageResponse.builder()
                        .messageId(message.getId())
                        .roomId(message.getRoomId())
                        .senderId(message.getSenderId())
                        .senderNickname(
                                userRepository.findById(message.getSenderId())
                                        .map(User::getNickname)
                                        .orElse("알 수 없음")
                        )
                        .contentType(message.getContentType())
                        .contentText(message.getContentText())
                        .fileUrl(message.getFileUrl())
                        .createdAt(message.getCreatedAt())
                        .build())
                .toList();

        return ChatMessageListResponse.builder()
                .messages(messageResponses)
                .pagination(pagination)
                .build();
    }

    // 마지막 읽은 메세지 처리
    public void lastReadMessage(String email, Long roomId, LastReadMessageRequest request) {

        Long userId = userRepository.findByEmail(email).orElseThrow(() ->
                new GlobalException(ErrorCode.USER_NOT_FOUND)).getId();

        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByRoom_IdAndUser_Id(roomId, userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_CHAT_MEMBER));

        chatRoomMember.setLastReadMessageId(request.getMessageId());
        chatRoomMemberRepository.save(chatRoomMember);
    }

    @Transactional
    public void createPrivateChatRoom(User sender, User receiver) {
        // 기존 1:1 채팅방 존재 확인
        Optional<ChatRoom> originChatRoom = chatRepository.findPrivateRoomByUserIds(sender.getId(), receiver.getId());

        if (originChatRoom.isPresent()) {
            throw new GlobalException(ErrorCode.CHAT_ROOM_ALREADY_EXISTS);
        }

        // ChatRoom 저장
        ChatRoom chatRoom = ChatRoom.builder()
                .roomType(ChatRoomType.PRIVATE)
                .owner(sender)
                .build();

        chatRepository.save(chatRoom);

        // ChatRoomMember 저장
        ChatRoomMember member1 = ChatRoomMember.builder()
                .room(chatRoom)
                .user(sender)
                .role(ChatMemberRole.ADMIN) // 요청자가 owner라 생각함.
                .build();

        ChatRoomMember member2 = ChatRoomMember.builder()
                .room(chatRoom)
                .user(receiver)
                .role(ChatMemberRole.MEMBER)
                .build();

        chatRoomMemberRepository.saveAll(List.of(member1, member2));
    }
}
