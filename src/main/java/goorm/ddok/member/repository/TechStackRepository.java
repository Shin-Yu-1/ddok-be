package goorm.ddok.member.repository;

import goorm.ddok.member.domain.TechStack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TechStackRepository extends JpaRepository<TechStack, Long> {

    Optional<TechStack> findByName(String techStackName);
}
