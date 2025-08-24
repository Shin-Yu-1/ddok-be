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

    // 사용자의 개인 채팅(1:1) 목록 조회 (수정됨)
    @Query("""
        SELECT DISTINCT cr FROM ChatRoom cr
        INNER JOIN ChatRoomMember crm ON cr.id = crm.roomId.id
        WHERE cr.roomType = 'PRIVATE'
        AND crm.userId.id = :userId
        AND crm.deletedAt IS NULL
        ORDER BY COALESCE(cr.lastMessageAt, cr.createdAt) DESC
        """)
    Page<ChatRoom> findPrivateChatsByUserId(@Param("userId") Long userId, Pageable pageable);

    // 사용자의 그룹 채팅 목록 조회
    @Query("""
        SELECT DISTINCT cr FROM ChatRoom cr
        INNER JOIN ChatRoomMember crm ON cr.id = crm.roomId.id
        WHERE cr.roomType = 'GROUP'
        AND crm.userId.id = :userId
        AND crm.deletedAt IS NULL
        ORDER BY COALESCE(cr.lastMessageAt, cr.createdAt) DESC
        """)
    Page<ChatRoom> findTeamChatsByUserId(@Param("userId") Long userId, Pageable pageable);

    // 사용자의 모든 채팅 목록에서 검색 (채팅방 이름, 닉네임 기준)
    @Query("""
        SELECT DISTINCT cr FROM ChatRoom cr
        INNER JOIN ChatRoomMember crm ON cr.id = crm.roomId.id
        WHERE crm.userId.id = :userId
        AND crm.deletedAt IS NULL
        AND cr.roomType = :roomType
        AND (
            LOWER(cr.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            (cr.roomType = 'PRIVATE' AND EXISTS (
                SELECT 1 FROM ChatRoomMember otherMember
                JOIN otherMember.userId otherUser
                WHERE otherMember.roomId.id = cr.id 
                AND otherMember.userId.id != :userId
                AND otherMember.deletedAt IS NULL
                AND LOWER(otherUser.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )) OR
            (cr.roomType = 'GROUP' AND EXISTS (
                SELECT 1 FROM ChatRoomMember groupMember
                JOIN groupMember.userId groupUser
                WHERE groupMember.roomId.id = cr.id
                AND groupMember.deletedAt IS NULL
                AND LOWER(groupUser.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
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
        WHERE crm.roomId.id = :roomId 
        AND crm.userId.id = :userId
        AND crm.deletedAt IS NULL
        """)
    Optional<ChatRoomMember> findMembershipByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    // 특정 채팅방의 멤버 수 조회
    @Query("""
        SELECT COUNT(crm) FROM ChatRoomMember crm
        WHERE crm.roomId.id = :roomId
        AND crm.deletedAt IS NULL
        """)
    Long countMembersByRoomId(@Param("roomId") Long roomId);

    // 특정 채팅방의 마지막 메시지 조회
    @Query("""
        SELECT cm FROM ChatMessage cm
        WHERE cm.roomId.id = :roomId
        AND cm.deletedAt IS NULL
        ORDER BY cm.createdAt DESC
        LIMIT 1
        """)
    Optional<ChatMessage> findLastMessageByRoomId(@Param("roomId") Long roomId);

    // 사용자의 특정 채팅방 읽지 않은 메시지 수 조회
    @Query("""
        SELECT COUNT(cm) FROM ChatMessage cm
        LEFT JOIN ChatRoomMember crm ON crm.roomId.id = cm.roomId.id AND crm.userId.id = :userId
        WHERE cm.roomId.id = :roomId
        AND cm.deletedAt IS NULL
        AND crm.deletedAt IS NULL
        AND (
            crm.lastReadMessageId IS NULL OR 
            cm.id > crm.lastReadMessageId.id
        )
        AND cm.senderId.id != :userId
        """)
    Integer countUnreadMessagesByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    // 두 사용자 간의 기존 개인 채팅방 찾기
    @Query("""
        SELECT cr FROM ChatRoom cr
        WHERE cr.roomType = 'PRIVATE'
        AND cr.deletedAt IS NULL
        AND EXISTS (
            SELECT 1 FROM ChatRoomMember crm1
            WHERE crm1.roomId.id = cr.id 
            AND crm1.userId.id = :userId1
            AND crm1.deletedAt IS NULL
        )
        AND EXISTS (
            SELECT 1 FROM ChatRoomMember crm2
            WHERE crm2.roomId.id = cr.id 
            AND crm2.userId.id = :userId2
            AND crm2.deletedAt IS NULL
        )
        AND (
            SELECT COUNT(crm) FROM ChatRoomMember crm 
            WHERE crm.roomId.id = cr.id 
            AND crm.deletedAt IS NULL
        ) = 2
        """)
    Optional<ChatRoom> findPrivateChatBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // 사용자 이메일로 ID 조회
    @Query("SELECT u.id FROM User u WHERE u.email = :email")
    Optional<Long> findIdByEmail(@Param("email") String email);
}
