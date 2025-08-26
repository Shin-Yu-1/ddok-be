package goorm.ddok.cafe.repository;

import goorm.ddok.cafe.domain.CafeReviewTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CafeReviewTagRepository extends JpaRepository<CafeReviewTag, Long> {
    Optional<CafeReviewTag> findByName(String name);
    boolean existsByName(String name);
}