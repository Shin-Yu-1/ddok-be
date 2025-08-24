package goorm.ddok.chat.repository;

import goorm.ddok.chat.domain.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    boolean existsByRoomId_IdAndUserId_Id(Long roomId, Long userId);

    List<ChatRoomMember> findAllByRoomId_Id(Long roomId);

    @Query("SELECT crm FROM ChatRoomMember crm JOIN FETCH crm.userId WHERE crm.roomId.id = :roomId")
    List<ChatRoomMember> findAllByRoomIdWithUser(@Param("roomId") Long roomId);
}
