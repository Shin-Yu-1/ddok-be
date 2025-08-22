package goorm.ddok.cafe.repository;

import goorm.ddok.cafe.domain.Cafe;
import goorm.ddok.cafe.domain.CafeReview;
import goorm.ddok.member.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CafeReviewRepository extends JpaRepository<CafeReview, Long> {
    Optional<CafeReview> findByCafeAndUser(Cafe cafe, User user);
    List<CafeReview> findAllByCafeId(Long cafeId);
}