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

    List<ChatRoomMember> findAllByUserIdInAndDeletedAtIsNull(List<Long> userIds);


    // 특정 사용자의 모든 채팅방 멤버십 조회
    List<ChatRoomMember> findAllByUserIdAndDeletedAtIsNull(Long userId);

    // roomID와 userId가 있는 멤버십 조회
    Optional<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId);


    // User 엔티티 정보가 필요한 경우를 위한 별도 쿼리
    @Query("""
        SELECT crm, u FROM ChatRoomMember crm
        JOIN User u ON crm.userId = u.id
        WHERE crm.roomId = :roomId
        AND crm.deletedAt IS NULL
        """)
    List<Object[]> findAllByRoomIdWithUserInfo(@Param("roomId") Long roomId);
}
