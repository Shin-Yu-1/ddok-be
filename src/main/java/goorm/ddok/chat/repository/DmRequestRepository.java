package goorm.ddok.chat.repository;

import goorm.ddok.chat.domain.DmRequest;
import goorm.ddok.chat.domain.DmRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update DmRequest d
           set d.status = goorm.ddok.chat.domain.DmRequestStatus.ACCEPTED,
               d.respondedAt = :now
         where d.fromUser.id = :fromId
           and d.toUser.id   = :toId
           and d.status      = goorm.ddok.chat.domain.DmRequestStatus.PENDING
        """)
    int acceptIfPending(@Param("fromId") Long fromId,
                        @Param("toId") Long toId,
                        @Param("now") java.time.Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update DmRequest d
           set d.status = goorm.ddok.chat.domain.DmRequestStatus.REJECTED,
               d.respondedAt = :now
         where d.fromUser.id = :fromId
           and d.toUser.id   = :toId
           and d.status      = goorm.ddok.chat.domain.DmRequestStatus.PENDING
        """)
    int rejectIfPending(@Param("fromId") Long fromId,
                        @Param("toId") Long toId,
                        @Param("now") java.time.Instant now);



    boolean existsByFromUser_IdAndToUser_IdAndStatusIn(
            Long fromUserId, Long toUserId, Collection<DmRequestStatus> statuses
    );

    default boolean existsEitherDirectionWithStatuses(
            Long userId1, Long userId2, Collection<DmRequestStatus> statuses
    ) {
        return existsByFromUser_IdAndToUser_IdAndStatusIn(userId1, userId2, statuses)
                || existsByFromUser_IdAndToUser_IdAndStatusIn(userId2, userId1, statuses);
    }
}