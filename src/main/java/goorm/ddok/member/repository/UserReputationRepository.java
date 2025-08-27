package goorm.ddok.member.repository;

import goorm.ddok.reputation.domain.UserReputation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserReputationRepository extends JpaRepository<UserReputation, Long> {

    /** user_id 기준으로 조회 */
    Optional<UserReputation> findByUserId(Long userId);
}
