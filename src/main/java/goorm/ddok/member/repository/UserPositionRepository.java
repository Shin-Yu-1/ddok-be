package goorm.ddok.member.repository;

import goorm.ddok.member.domain.UserPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPositionRepository extends JpaRepository<UserPosition, Long> {

    Optional<UserPosition> findByUserId(Long userId);
}
