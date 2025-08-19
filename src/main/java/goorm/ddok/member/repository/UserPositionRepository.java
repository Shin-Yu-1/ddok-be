package goorm.ddok.member.repository;

import goorm.ddok.member.domain.UserPosition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPositionRepository extends JpaRepository<UserPosition, Long> {
}
