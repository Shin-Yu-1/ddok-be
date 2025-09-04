package goorm.ddok.cafe.repository;

import goorm.ddok.cafe.domain.Cafe;
import goorm.ddok.project.repository.ProjectRecruitmentRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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
    List<Cafe> findActiveWithinBounds(BigDecimal swLat, BigDecimal swLng, BigDecimal neLat, BigDecimal neLng);

    interface MapRow {
        Long getId();
        String getTitle();
        String getBannerImageUrl();
        String getRegion1depthName();
        String getRegion2depthName();
        String getRegion3depthName();
        String getRoadName();
        String getMainBuildingNo();
        String getSubBuildingNo();
        String getZoneNo();
        BigDecimal getLatitude();
        BigDecimal getLongitude();
    }

    @Query("""
        select
          c.id as id,
          c.name as title,
          c.bannerImageUrl as bannerImageUrl,
          c.region1depthName as region1depthName,
          c.region2depthName as region2depthName,
          c.region3depthName as region3depthName,
          c.roadName as roadName,
          c.mainBuildingNo as mainBuildingNo,
          c.subBuildingNo as subBuildingNo,
          c.zoneNo as zoneNo,
          c.activityLatitude as latitude,
          c.activityLongitude as longitude
        from Cafe c
        where c.deletedAt is null
          and c.activityLatitude is not null
          and c.activityLongitude is not null
          and c.activityLatitude between :swLat and :neLat
          and c.activityLongitude between :swLng and :neLng
    """)
    List<ProjectRecruitmentRepository.MapRow> findAllInBounds(
            @Param("swLat") BigDecimal swLat,
            @Param("neLat") BigDecimal neLat,
            @Param("swLng") BigDecimal swLng,
            @Param("neLng") BigDecimal neLng
    );
}