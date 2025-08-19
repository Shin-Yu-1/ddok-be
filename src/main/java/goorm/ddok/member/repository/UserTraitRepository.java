package goorm.ddok.member.repository;

import goorm.ddok.member.domain.UserTrait;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTraitRepository extends JpaRepository<UserTrait, Long> {

    Optional<UserTrait> findByUserId(Long userId);

}
