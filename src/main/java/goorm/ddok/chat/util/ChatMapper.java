package goorm.ddok.chat.util;

import goorm.ddok.chat.domain.ChatMessage;
import goorm.ddok.chat.domain.ChatRoom;
import goorm.ddok.chat.domain.ChatRoomMember;
import goorm.ddok.chat.domain.ChatRoomType;
import goorm.ddok.chat.dto.response.ChatRoomResponse;
import goorm.ddok.chat.dto.response.LastMessageResponse;
import goorm.ddok.chat.dto.response.OtherUserResponse;
import goorm.ddok.chat.dto.response.UserSimpleResponse;
import goorm.ddok.chat.repository.ChatRepository;
import goorm.ddok.chat.repository.ChatRoomMemberRepository;
import goorm.ddok.member.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMapper {
    private final ChatRepository chatRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final RestClient.Builder builder;

    // ChatRoom을 ChatRoomResponse로 변환
    public ChatRoomResponse toChatRoomDto(ChatRoom chatRoom, Long currentUserId) {
        ChatRoomResponse.ChatRoomResponseBuilder builder = ChatRoomResponse.builder()
                .roomId(chatRoom.getId())
                .roomType(chatRoom.getRoomType())
                .isPinned(false) // TODO: 고정 기능 구현 시 실제 값으로 변경
                .updatedAt(Optional.ofNullable(chatRoom.getLastMessageAt())
                        .orElse(chatRoom.getCreatedAt()));

        // 채팅방 타입에 따라 다른 정보 설정
        if (chatRoom.getRoomType() == ChatRoomType.PRIVATE) {
            setPrivateChatInfo(builder, chatRoom, currentUserId);
        } else {
            setGroupChatInfo(builder, chatRoom, currentUserId);
        }

        // 마지막 메시지 설정
        setLastMessage(builder, chatRoom);

        return builder.build();
    }

    // 개인 채팅 정보 설정
    private void setPrivateChatInfo(ChatRoomResponse.ChatRoomResponseBuilder builder,
                                    ChatRoom chatRoom, Long currentUserId) {
        // ChatRoomMember를 통해 상대방 찾기 (User 정보 포함)
        List<ChatRoomMember> members = chatRoomMemberRepository.findAllByRoomIdWithUser(chatRoom.getId());

        // 현재 사용자가 아닌 상대방 찾기
        User otherUser = members.stream()
                .filter(member -> !member.getUserId().getId().equals(currentUserId))
                .map(ChatRoomMember::getUserId)
                .findFirst()
                .orElse(null);

        if (otherUser == null) {
            log.warn("개인 채팅방에서 상대방을 찾을 수 없습니다 - roomId: {}, currentUserId: {}",
                    chatRoom.getId(), currentUserId);
            builder.name("알 수 없는 사용자");
            return;
        }

        // 상대방 정보로 채팅방 이름 설정 (이름이 없는 경우)
        String roomName = chatRoom.getName() != null ? chatRoom.getName() : getNickname(otherUser);

        builder.name(roomName)
                .otherUser(OtherUserResponse.builder()
                        .id(otherUser.getId())
                        .nickname(getNickname(otherUser))
                        .profileImage(getProfileImageUrl(otherUser))
                        .build());
    }


    // User 객체에서 프로필 이미지 URL 추출 (User 클래스 구조에 따라 수정 필요)
    private String getProfileImageUrl(User user) {
        // User 클래스에 getProfileImage() 메서드가 있다고 가정
        // 실제 User 클래스 구조에 맞게 수정 필요
        try {
            return user.getProfileImageUrl();
        } catch (Exception e) {
            log.warn("프로필 이미지 URL 추출 실패 - userId: {}", user.getId());
            return null;
        }
    }

    // User 객체에서 닉네임 추출 (User 클래스 구조에 따라 수정 필요)
    private String getNickname(User user) {
        // User 클래스에 getNickname() 메서드가 있다고 가정
        // 실제 User 클래스 구조에 맞게 수정 필요
        try {
            return user.getNickname();
        } catch (Exception e) {
            log.warn("닉네임 추출 실패 - userId: {}", user.getId());
            return "사용자" + user.getId();
        }
    }

    // 그룹 채팅 정보 설정
    private void setGroupChatInfo(ChatRoomResponse.ChatRoomResponseBuilder builder,
                                  ChatRoom chatRoom, Long currentUserId) {
        builder.name(chatRoom.getName());

        // 멤버 수 조회
        Long memberCount = chatRepository.countMembersByRoomId(chatRoom.getId());
        builder.memberCount(memberCount.intValue());

        // 방장 정보 설정
        if (chatRoom.getOwnerUserId() != null) {
            builder.owner(UserSimpleResponse.builder()
                    .id(chatRoom.getOwnerUserId().getId())
                    .nickname(getNickname(chatRoom.getOwnerUserId()))
                    .profileImage(getProfileImageUrl(chatRoom.getOwnerUserId()))
                    .build());
        }

        // 현재 사용자의 역할 조회
        Optional<ChatRoomMember> currentMember = chatRepository.findMembershipByRoomIdAndUserId(
                chatRoom.getId(), currentUserId);
        currentMember.ifPresent(member -> builder.myRole(member.getRole()));
    }

    // 마지막 메시지 정보 설정
    private void setLastMessage(ChatRoomResponse.ChatRoomResponseBuilder builder, ChatRoom chatRoom) {
        Optional<ChatMessage> lastMessage = chatRepository.findLastMessageByRoomId(chatRoom.getId());

        lastMessage.ifPresent(message -> {
            String content = getMessageContent(message);

            builder.lastMessage(LastMessageResponse.builder()
                    .messageId(message.getId())
                    .type(message.getContentType())
                    .content(content)
                    .createdAt(message.getCreatedAt())
                    .senderId(message.getSenderId() != null ?
                            message.getSenderId().getId() : null)
                    .build());
        });
    }

    // 메시지 내용 추출 (타입에 따라 다르게 처리)
    private String getMessageContent(ChatMessage message) {
        return switch (message.getContentType()) {
            case TEXT -> message.getContentText();
            case IMAGE -> "이미지";
            case FILE -> "파일";
            case SYSTEM -> message.getContentText();
        };
    }
}
