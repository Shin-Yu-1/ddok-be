package goorm.ddok.cafe.repository;

import goorm.ddok.cafe.domain.Cafe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CafeRepository extends JpaRepository<Cafe, Long> {
    Optional<Cafe> findByKakaoPlaceId(String kakaoPlaceId);
    boolean existsByNameAndActivityLatitudeAndActivityLongitude(String name,
                                                                java.math.BigDecimal lat,
                                                                java.math.BigDecimal lng);
}