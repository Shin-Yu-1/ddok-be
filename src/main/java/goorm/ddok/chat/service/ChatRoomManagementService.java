package goorm.ddok.chat.service;

import goorm.ddok.chat.domain.*;
import goorm.ddok.chat.repository.ChatRepository;
import goorm.ddok.chat.repository.ChatRoomMemberRepository;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.member.domain.User;
import goorm.ddok.team.domain.Team;
import goorm.ddok.team.domain.TeamMemberRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ChatRoomManagementService {
    private final ChatRepository chatRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Transactional
    public void createPrivateChatRoom(User sender, User receiver) {
        if (chatRepository.existsPrivateRoomByUserIds(sender.getId(), receiver.getId())) {
            throw new GlobalException(ErrorCode.CHAT_ROOM_ALREADY_EXISTS);
        }

        ChatRoom room = chatRepository.save(ChatRoom.builder()
                .roomType(ChatRoomType.PRIVATE)
                .owner(sender)
                .build());

        ChatRoomMember admin = ChatRoomMember.builder()
                .room(room).user(sender).role(ChatMemberRole.ADMIN).build();
        ChatRoomMember member = ChatRoomMember.builder()
                .room(room).user(receiver).role(ChatMemberRole.MEMBER).build();

        chatRoomMemberRepository.saveAll(List.of(admin, member));
    }

    @Transactional
    public void createTeamChatRoom(Team team, User leader) {
        Optional<ChatRoom> existing = chatRepository.findByTeam(team);
        if (existing.isPresent()) {
            throw new GlobalException(ErrorCode.CHAT_ROOM_ALREADY_EXISTS);
        }

        chatRepository.save(ChatRoom.builder()
                .roomType(ChatRoomType.GROUP)
                .owner(leader)
                .team(team)
                .name(team.getTitle())
                .build());

        addMemberToTeamChat(team, leader, TeamMemberRole.LEADER);
    }

    @Transactional
    public void addMemberToTeamChat(Team team, User user, TeamMemberRole teamRole) {
        ChatRoom room = chatRepository.findByTeam_Id(team.getId())
                .orElseThrow(() -> new GlobalException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        Long roomId = room.getId();
        Long userId = user.getId();

        if (chatRoomMemberRepository.existsByRoom_IdAndUser_IdAndDeletedAtIsNull(roomId, userId)) {
            return;
        }

        Optional<ChatRoomMember> any = chatRoomMemberRepository.findLatestIncludingDeleted(roomId, userId);
        if (any.isPresent()) {
            ChatRoomMember m = any.get();
            m.setRoom(room);
            m.setUser(user);
            m.setRole(ChatMemberRole.MEMBER);
            m.restore();
            chatRoomMemberRepository.save(m);
            return;
        }

        ChatRoomMember created = ChatRoomMember.builder()
                .room(room)
                .user(user)
                .role(ChatMemberRole.MEMBER)
                .build();
        chatRoomMemberRepository.save(created);
    }

    @Transactional
    public void removeMemberFromTeamChat(Long teamId, Long memberID) {
        ChatRoom chatRoom = chatRepository.findByTeam_Id(teamId)
                .orElseThrow(() -> new GlobalException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        ChatRoomMember chatMember = chatRoomMemberRepository
                .findFirstByRoom_IdAndDeletedAtIsNullAndUser_IdNotOrderByCreatedAtAsc(chatRoom.getId(), memberID)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_CHAT_MEMBER));

        chatMember.expel();
    }
}
