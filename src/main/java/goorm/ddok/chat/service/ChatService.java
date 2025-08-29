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

        User me = getUserByEmail(email);

        Page<ChatRoom> page = chatRepository.pageGroupRoomsByMemberAndRoomOrMemberName(
                me, search, pageable);

        List<ChatRoomResponse> chats = chatMapper.toChatRoomDtoList(page.getContent(), me.getId());
        return ChatListResponse.builder()
                .chats(chats)
                .pagination(PaginationResponse.of(page))
                .build();
    }

    // 채팅 멤버 조회
    public ChatMembersResponse getRoomMembers(Long roomId, String email) {
        User me = getUserByEmail(email);
        ChatRoom roomRef = chatRepository.getReferenceById(roomId);

        boolean isMember = chatRoomMemberRepository.existsByRoomAndUserAndDeletedAtIsNull(roomRef, me);
        if (!isMember) throw new GlobalException(ErrorCode.NOT_CHAT_MEMBER);

        // User를 한 번에 가져오도록 EntityGraph/Fetch Join 사용
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

    // 메세지 전송
    @Transactional
    public ChatMessageResponse sendMessage(String email, Long roomId, ChatMessageRequest request) {
        User sender = getUserByEmail(email);
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
