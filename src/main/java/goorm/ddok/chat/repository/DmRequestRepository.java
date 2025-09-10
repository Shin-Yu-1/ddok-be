package goorm.ddok.chat.repository;

import goorm.ddok.chat.domain.DmRequest;
import goorm.ddok.chat.domain.DmRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DmRequestRepository extends JpaRepository<DmRequest, Long> {

    @Query("""
        select case when count(dr) > 0 then true else false end
        from DmRequest dr
        where dr.status = :status
          and (
            (dr.fromUser.id = :a and dr.toUser.id = :b)
            or
            (dr.fromUser.id = :b and dr.toUser.id = :a)
          )
    """)
    boolean existsPendingBetween(
            @Param("a") Long userA,
            @Param("b") Long userB,
            @Param("status") DmRequestStatus status
    );

    Optional<DmRequest> findTopByFromUser_IdAndToUser_IdAndStatusOrderByCreatedAtDesc(
            Long fromUserId, Long toUserId, DmRequestStatus status);
}