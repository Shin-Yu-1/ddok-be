package goorm.ddok.member.repository;

import goorm.ddok.member.domain.UserTrait;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTraitRepository extends JpaRepository<UserTrait, Long> {

    // 성향은 1:N
    List<UserTrait> findByUserId(Long userId);
}