package goorm.ddok.chat.repository;

import goorm.ddok.chat.domain.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    // 특정 채팅방에 특정 사용자가 멤버로 존재하는지 확인
    boolean existsByRoom_IdAndUser_Id(Long roomId, Long userId);

    // 여러 사용자들의 멤버십(soft delete 제외)
    List<ChatRoomMember> findAllByUser_IdInAndDeletedAtIsNull(List<Long> userIds);

    // 특정 사용자의 모든 채팅방 멤버십 조회(soft delete 제외)
    List<ChatRoomMember> findAllByUser_IdAndDeletedAtIsNull(Long userId);

    // 특정 룸의 모든 멤버(soft delete 제외)
    List<ChatRoomMember> findAllByRoom_IdAndDeletedAtIsNull(Long roomId);

    // roomId + userId로 단일 멤버십 조회
    Optional<ChatRoomMember> findByRoom_IdAndUser_Id(Long roomId, Long userId);

    // User 엔티티 정보가 필요한 경우를 위한 별도 쿼리
    @Query("""
        SELECT crm, u
        FROM ChatRoomMember crm
        JOIN User u ON crm.user.id = u.id
        WHERE crm.room.id = :roomId
        AND crm.deletedAt IS NULL 
    """)
    List<Object[]> findAllByRoomIdWithUserInfo(@Param("roomId") Long roomId);

    // 특정 룸에 사용자가 속했는지 확인(soft delete 제외)
    boolean existsByRoom_IdAndUser_IdAndDeletedAtIsNull(Long roomId, Long userId);
}
