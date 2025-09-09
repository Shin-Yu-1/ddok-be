package goorm.ddok.reputation.repository;

import goorm.ddok.member.domain.User;
import goorm.ddok.reputation.domain.UserReputation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserReputationRepository extends JpaRepository<UserReputation, Long> {

    /** user_id 기준으로 조회 */
    Optional<UserReputation> findByUserId(Long userId);

    Optional<UserReputation> findByUser_Id(Long userId);

    Optional<UserReputation> findByUser(User user);

    @Query("select ur.user.id, ur.temperature from UserReputation ur where ur.user.id in :userIds")
    List<Object[]> findTempsByUserIds(@Param("userIds") Collection<Long> userIds);
}
