package goorm.ddok.member.repository;

import goorm.ddok.member.domain.UserPosition;
import goorm.ddok.member.domain.UserPositionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPositionRepository extends JpaRepository<UserPosition, Long> {

    // 전체(1:N)
    List<UserPosition> findByUserId(Long userId);

    // 타입별(주/부 포지션)
    List<UserPosition> findByUserIdAndType(Long userId, UserPositionType type);

    // 주 포지션 1개
    Optional<UserPosition> findFirstByUserIdAndType(Long userId, UserPositionType type);
}