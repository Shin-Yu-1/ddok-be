package goorm.ddok.member.repository;

import goorm.ddok.member.domain.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
}
