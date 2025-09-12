package goorm.ddok.chat.repository;

import goorm.ddok.chat.domain.ChatRoom;
import goorm.ddok.chat.domain.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
        SELECT r
        FROM ChatRoom r
        JOIN r.members m1
        JOIN r.members m2
        WHERE r.roomType = :roomType
          AND m1.user.id = :userId1
          AND m2.user.id = :userId2
    """)
    Optional<ChatRoom> findPrivateRoomBetweenUsers(
            @Param("roomType") ChatRoomType roomType,
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );
}
