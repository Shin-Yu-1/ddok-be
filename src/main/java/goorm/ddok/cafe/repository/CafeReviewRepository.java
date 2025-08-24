package goorm.ddok.cafe.repository;

import goorm.ddok.cafe.domain.Cafe;
import goorm.ddok.cafe.domain.CafeReview;
import goorm.ddok.member.domain.User;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface CafeReviewRepository extends JpaRepository<CafeReview, Long> {
    Optional<CafeReview> findByCafeAndUser(Cafe cafe, User user);
    List<CafeReview> findAllByCafeId(Long cafeId);


    @Query("""
           select count(r)
    @EntityGraph(attributePaths = "user") // user 즉시 로딩으로 N+1 회피
    @Query("""
           select r
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
    Page<CafeReview> findPageActiveByCafeId(Long cafeId, Pageable pageable);
           
}