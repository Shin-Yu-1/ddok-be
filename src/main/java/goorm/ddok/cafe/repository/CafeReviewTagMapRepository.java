package goorm.ddok.cafe.repository;

import goorm.ddok.cafe.domain.CafeReviewTagMap;
import goorm.ddok.cafe.domain.CafeReview;
import goorm.ddok.cafe.domain.CafeReviewTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CafeReviewTagMapRepository extends JpaRepository<CafeReviewTagMap, Long> {
    List<CafeReviewTagMap> findAllByReview(CafeReview review);
    List<CafeReviewTagMap> findAllByTag(CafeReviewTag tag);
}