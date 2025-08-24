package goorm.ddok.cafe.repository;

import goorm.ddok.cafe.domain.CafeReviewTagMap;
import goorm.ddok.cafe.domain.CafeReview;
import goorm.ddok.cafe.domain.CafeReviewTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface CafeReviewTagMapRepository extends JpaRepository<CafeReviewTagMap, Long> {
    List<CafeReviewTagMap> findAllByReview(CafeReview review);
    List<CafeReviewTagMap> findAllByTag(CafeReviewTag tag);

    interface ReviewTagProjection {
        Long getReviewId();
        String getTagName();
    }

    @Query("""
        select m.review.id as reviewId, t.name as tagName
        from CafeReviewTagMap m
          join m.tag t
        where m.review.id in :reviewIds
        """)
    List<ReviewTagProjection> findTagNamesByReviewIds(Collection<Long> reviewIds);
}