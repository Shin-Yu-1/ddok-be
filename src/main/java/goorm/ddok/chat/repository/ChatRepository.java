package goorm.ddok.chat.repository;

import goorm.ddok.chat.domain.ChatMessage;
import goorm.ddok.chat.domain.ChatRoom;
import goorm.ddok.chat.domain.ChatRoomMember;
import goorm.ddok.chat.domain.ChatRoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<ChatRoom, Long> {

    // Room type + Room ID list 조회
    List<ChatRoom> findByIdInAndRoomType(List<Long> roomIds, ChatRoomType type);

    // Room type + Room Id + Room name 키워드 조회
    List<ChatRoom> findByIdInAndRoomTypeAndNameContaining(List<Long> roomIds, ChatRoomType type, String name);

    // 특정 채팅방에 대한 사용자의 멤버십 조회
    @Query("""
        SELECT crm FROM ChatRoomMember crm
        WHERE crm.roomId = :roomId
        AND crm.userId = :userId
        AND crm.deletedAt IS NULL
        """)
    Optional<ChatRoomMember> findMembershipByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    // 특정 채팅방의 멤버 수 조회
    @Query("""
        SELECT COUNT(crm) FROM ChatRoomMember crm
        WHERE crm.roomId = :roomId
        AND crm.deletedAt IS NULL
        """)
    Long countMembersByRoomId(@Param("roomId") Long roomId);

    // 특정 채팅방의 마지막 메시지 조회
    @Query("""
        SELECT cm FROM ChatMessage cm
        WHERE cm.roomId = :roomId
        AND cm.deletedAt IS NULL
        ORDER BY cm.createdAt DESC
        LIMIT 1
        """)
    Optional<ChatMessage> findLastMessageByRoomId(@Param("roomId") Long roomId);

    // 기존 1:1 채팅방 존재 여부 확인
    @Query("""
        SELECT cr FROM ChatRoom cr
        JOIN ChatRoomMember crm1 ON cr.id = crm1.roomId
        JOIN ChatRoomMember crm2 ON cr.id = crm2.roomId
        WHERE cr.roomType = 'PRIVATE'
          AND crm1.userId = :userId1
          AND crm2.userId = :userId2
          AND crm1.deletedAt IS NULL
          AND crm2.deletedAt IS NULL
    """)
    Optional<ChatRoom> findPrivateRoomByUserIds(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
