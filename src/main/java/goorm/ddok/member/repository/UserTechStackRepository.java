package goorm.ddok.member.repository;

import goorm.ddok.member.domain.UserTechStack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTechStackRepository extends JpaRepository<UserTechStack, Long> {

    // 스택도 1:N
    List<UserTechStack> findByUserId(Long userId);
}