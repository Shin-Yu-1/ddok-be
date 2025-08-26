package goorm.ddok.member.repository;

import goorm.ddok.member.domain.UserTechStack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTechStackRepository extends JpaRepository<UserTechStack, Long> {

    Optional<UserTechStack> findByUserId(Long userId);
}
