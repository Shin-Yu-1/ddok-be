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

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<ChatRoom, Long> {

    // 사용자의 개인 채팅(1:1) 목록 조회
    @Query("""
        SELECT DISTINCT cr FROM ChatRoom cr
        LEFT JOIN ChatRoomMember crm ON cr.id = crm.roomId.id
        WHERE cr.roomType = 'PRIVATE'
        AND (cr.privateAUserId.id = :userId OR cr.privateBUserId.id = :userId)
        ORDER BY COALESCE(cr.lastMessageAt, cr.createdAt) DESC
        """)
    Page<ChatRoom> findPrivateChatsByUserId(@Param("userId") Long userId, Pageable pageable);

    // 사용자의 그룹 채팅 목록 조회
    @Query("""
        SELECT DISTINCT cr FROM ChatRoom cr
        INNER JOIN ChatRoomMember crm ON cr.id = crm.roomId.id
        WHERE cr.roomType = 'GROUP'
        AND crm.userId.id = :userId
        ORDER BY COALESCE(cr.lastMessageAt, cr.createdAt) DESC
        """)
    Page<ChatRoom> findTeamChatsByUserId(@Param("userId") Long userId, Pageable pageable);

    // 사용자의 모든 채팅 목록에서 검색 (채팅방 이름, 닉네임 기준)
    @Query("""
    SELECT DISTINCT cr FROM ChatRoom cr
    LEFT JOIN ChatRoomMember crm ON cr.id = crm.roomId.id
    LEFT JOIN cr.privateAUserId ua
    LEFT JOIN cr.privateBUserId ub
    WHERE (
        (cr.roomType = 'PRIVATE' AND (cr.privateAUserId.id = :userId OR cr.privateBUserId.id = :userId)) OR
        (cr.roomType = 'GROUP' AND crm.userId.id = :userId)
    )
    AND cr.roomType = :roomType
    AND (
        LOWER(cr.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        (cr.roomType = 'PRIVATE' AND (
            LOWER(ua.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(ub.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ))
    )
    ORDER BY COALESCE(cr.lastMessageAt, cr.createdAt) DESC
    """)
    Page<ChatRoom> searchChatsByKeyword(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("roomType") ChatRoomType roomType,
            Pageable pageable
    );

    // 특정 채팅방에 대한 사용자의 멤버십 조회
    @Query("""
        SELECT crm FROM ChatRoomMember crm
        WHERE crm.roomId.id = :roomId AND crm.userId.id = :userId
        """)
    Optional<ChatRoomMember> findMembershipByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    // 특정 채팅방의 멤버 수 조회
    @Query("""
        SELECT COUNT(crm) FROM ChatRoomMember crm
        WHERE crm.roomId.id = :roomId
        """)
    Long countMembersByRoomId(@Param("roomId") Long roomId);

    // 특정 채팅방의 마지막 메시지 조회
    @Query(value = """
        SELECT * FROM chat_message cm
        WHERE cm.room_id = :roomId
        AND cm.deleted_at IS NULL
        ORDER BY cm.created_at DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<ChatMessage> findLastMessageByRoomId(@Param("roomId") Long roomId);

    // 사용자의 특정 채팅방 읽지 않은 메시지 수 조회
    @Query("""
        SELECT COUNT(cm) FROM ChatMessage cm
        LEFT JOIN ChatRoomMember crm ON crm.roomId.id = cm.roomId.id AND crm.userId.id = :userId
        WHERE cm.roomId.id = :roomId
        AND cm.deletedAt IS NULL
        AND (
            crm.lastReadMessageId IS NULL OR 
            cm.id > crm.lastReadMessageId.id
        )
        AND cm.senderId.id != :userId
        """)
    Integer countUnreadMessagesByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    @Query("select u.id from User u where u.email = :email")
    Optional<Long> findIdByEmail(@Param("email") String email);
}
