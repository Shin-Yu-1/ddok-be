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

    // 사용자의 개인 채팅(1:1) 목록 조회
    Page<ChatRoom> findByRoomTypeAndAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId, ChatRoomType roomType, Pageable pageable);

    // 사용자의 그룹 채팅 목록 조회
    @Query("""
        SELECT DISTINCT cr FROM ChatRoom cr
        INNER JOIN ChatRoomMember crm ON cr.id = crm.roomId
        WHERE cr.roomType = 'GROUP'
        AND crm.userId = :userId
        AND crm.deletedAt IS NULL
        AND cr.deletedAt IS NULL
        ORDER BY COALESCE(cr.lastMessageAt, cr.createdAt) DESC
        """)
    Page<ChatRoom> findTeamChatsByUserId(@Param("userId") Long userId, Pageable pageable);

    // 사용자의 모든 채팅 목록에서 검색 (채팅방 이름 기준)
    // 주의: User 엔티티와의 직접 관계가 없으므로 닉네임 검색 기능은 별도 구현 필요
    @Query("""
        SELECT DISTINCT cr FROM ChatRoom cr
        INNER JOIN ChatRoomMember crm ON cr.id = crm.roomId
        WHERE crm.userId = :userId
        AND crm.deletedAt IS NULL
        AND cr.deletedAt IS NULL
        AND cr.roomType = :roomType
        AND LOWER(cr.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
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

    // 두 사용자 간의 기존 개인 채팅방 찾기
    @Query("""
        SELECT cr FROM ChatRoom cr
        WHERE cr.roomType = 'PRIVATE'
        AND cr.deletedAt IS NULL
        AND EXISTS (
            SELECT 1 FROM ChatRoomMember crm1
            WHERE crm1.roomId = cr.id
            AND crm1.userId = :userId1
            AND crm1.deletedAt IS NULL
        )
        AND EXISTS (
            SELECT 1 FROM ChatRoomMember crm2
            WHERE crm2.roomId = cr.id
            AND crm2.userId = :userId2
            AND crm2.deletedAt IS NULL
        )
        AND (
            SELECT COUNT(crm) FROM ChatRoomMember crm
            WHERE crm.roomId = cr.id 
            AND crm.deletedAt IS NULL
        ) = 2
        """)
    Optional<ChatRoom> findPrivateChatBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // 사용자 이메일로 ID 조회 (User 엔티티가 별도 패키지에 있다고 가정)
    @Query("SELECT u.id FROM User u WHERE u.email = :email")
    Optional<Long> findIdByEmail(@Param("email") String email);
}
