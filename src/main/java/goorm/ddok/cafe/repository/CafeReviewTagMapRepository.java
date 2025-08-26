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


    interface TagCountProjection {
        String getTagName();
        long getTagCount();
    }

    @Query("""
        select t.name as tagName, count(m.id) as tagCount
        from CafeReviewTagMap m
          join m.review r
          join m.tag t
        where r.cafe.id = :cafeId
          and r.status = goorm.ddok.cafe.domain.CafeReviewStatus.ACTIVE
          and r.deletedAt is null
        group by t.name
        order by count(m.id) desc, t.name asc
        """)
    List<TagCountProjection> countTagsByCafeId(Long cafeId);
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