package goorm.ddok.cafe.repository;

import goorm.ddok.cafe.domain.Cafe;
import goorm.ddok.cafe.domain.CafeReview;
import goorm.ddok.member.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface CafeReviewRepository extends JpaRepository<CafeReview, Long> {
    Optional<CafeReview> findByCafeAndUser(Cafe cafe, User user);
    List<CafeReview> findAllByCafeId(Long cafeId);

    @Query("""
           select count(r)
           from CafeReview r
           where r.cafe.id = :cafeId
             and r.status = goorm.ddok.cafe.domain.CafeReviewStatus.ACTIVE
             and r.deletedAt is null
           """)
    long countActiveByCafeId(@Param("cafeId") Long cafeId);

    @Query("""
           select coalesce(avg(r.rating), 0)
           from CafeReview r
           where r.cafe.id = :cafeId
             and r.status = goorm.ddok.cafe.domain.CafeReviewStatus.ACTIVE
             and r.deletedAt is null
           """)
    Double avgRatingActiveByCafeId(@Param("cafeId") Long cafeId);
}