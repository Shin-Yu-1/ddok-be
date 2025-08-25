package goorm.ddok.cafe.repository;

import goorm.ddok.cafe.domain.Cafe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CafeRepository extends JpaRepository<Cafe, Long> {
    Optional<Cafe> findByKakaoPlaceId(String kakaoPlaceId);
    boolean existsByNameAndActivityLatitudeAndActivityLongitude(String name,
                                                                java.math.BigDecimal lat,
                                                                java.math.BigDecimal lng);

    @Query("""
        select c
        from Cafe c
        where c.deletedAt is null
          and c.activityLatitude between :swLat and :neLat
          and c.activityLongitude between :swLng and :neLng
        """)
    List<Cafe> findActiveWithinBounds(double swLat, double swLng, double neLat, double neLng);
}