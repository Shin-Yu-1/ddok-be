package goorm.ddok.chat.repository;

import goorm.ddok.chat.domain.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    // 특정 채팅방에 특정 사용자가 멤버로 존재하는지 확인
    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    // 특정 채팅방의 모든 멤버 조회
    List<ChatRoomMember> findAllByRoomId(Long roomId);

    // 삭제되지 않은 특정 채팅방의 모든 멤버 조회
    List<ChatRoomMember> findAllByRoomIdAndDeletedAtIsNull(Long roomId);

    // 특정 채팅방에서 삭제되지 않은 특정 사용자 멤버십 조회
    Optional<ChatRoomMember> findByRoomIdAndUserIdAndDeletedAtIsNull(Long roomId, Long userId);

    // 특정 사용자의 모든 채팅방 멤버십 조회
    List<ChatRoomMember> findAllByUserIdAndDeletedAtIsNull(Long userId);

    // 특정 사용자의 모든 채팅방 멤버십 id 추출

    // 특정 채팅방의 멤버 수 조회 (삭제되지 않은 멤버만)
    long countByRoomIdAndDeletedAtIsNull(Long roomId);

    // User 엔티티 정보가 필요한 경우를 위한 별도 쿼리
    // 주의: User 엔티티가 goorm.ddok.member.domain 패키지에 있다고 가정
    @Query("""
        SELECT crm, u FROM ChatRoomMember crm 
        JOIN User u ON crm.userId = u.id 
        WHERE crm.roomId = :roomId 
        AND crm.deletedAt IS NULL
        """)
    List<Object[]> findAllByRoomIdWithUserInfo(@Param("roomId") Long roomId);

    // 특정 채팅방의 멤버들의 senderId 목록만 조회
    @Query("SELECT crm.userId FROM ChatRoomMember crm WHERE crm.roomId = :roomId AND crm.deletedAt IS NULL")
    List<Long> findUserIdsByRoomId(@Param("roomId") Long roomId);
}
