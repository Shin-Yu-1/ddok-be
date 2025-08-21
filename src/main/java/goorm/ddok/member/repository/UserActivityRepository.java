package goorm.ddok.member.repository;

import goorm.ddok.member.domain.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    Optional<UserActivity> findByUserId(Long userId);
}
