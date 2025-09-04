package goorm.ddok.project.repository;

import goorm.ddok.project.domain.ProjectRecruitment;
import goorm.ddok.project.domain.TeamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProjectRecruitmentRepository extends JpaRepository<ProjectRecruitment, Long> {
    Optional<ProjectRecruitment> findByIdAndDeletedAtIsNull(Long id);

    interface MapRow {
        Long getId();
        String getTitle();
        TeamStatus getTeamStatus();
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
          pr.id as id,
          pr.title as title,
          pr.teamStatus as teamStatus,
          pr.bannerImageUrl as bannerImageUrl,
          pr.region1depthName as region1depthName,
          pr.region2depthName as region2depthName,
          pr.region3depthName as region3depthName,
          pr.roadName as roadName,
          pr.mainBuildingNo as mainBuildingNo,
          pr.subBuildingNo as subBuildingNo,
          pr.zoneNo as zoneNo,
          pr.latitude as latitude,
          pr.longitude as longitude
        from ProjectRecruitment pr
        where pr.deletedAt is null
          and pr.latitude is not null
          and pr.longitude is not null
          and pr.latitude between :swLat and :neLat
          and pr.longitude between :swLng and :neLng
    """)
    List<MapRow> findAllInBounds(
            @Param("swLat") BigDecimal swLat,
            @Param("neLat") BigDecimal neLat,
            @Param("swLng") BigDecimal swLng,
            @Param("neLng") BigDecimal neLng
    );

    @Query("""
    select distinct
      pr.id                as id,
      pr.title             as title,
      pr.teamStatus        as teamStatus,
      pr.bannerImageUrl    as bannerImageUrl,
      pr.region1depthName  as region1depthName,
      pr.region2depthName  as region2depthName,
      pr.region3depthName  as region3depthName,
      pr.roadName          as roadName,
      pr.mainBuildingNo    as mainBuildingNo,
      pr.subBuildingNo     as subBuildingNo,
      pr.zoneNo            as zoneNo,
      pr.latitude          as latitude,
      pr.longitude         as longitude
    from ProjectParticipant pp
      join pp.position pos
      join pos.projectRecruitment pr
    where pp.deletedAt is null
      and pr.deletedAt is null
      and pp.user.id = :userId
      and pr.teamStatus in (goorm.ddok.project.domain.TeamStatus.ONGOING,
                            goorm.ddok.project.domain.TeamStatus.CLOSED)
      and pr.latitude  is not null
      and pr.longitude is not null
      and pr.latitude  between :swLat and :neLat
      and pr.longitude between :swLng and :neLng
""")
    List<MapRow> findAllInBoundsForProfile(
            @Param("userId") Long userId,
            @Param("swLat") BigDecimal swLat,
            @Param("neLat") BigDecimal neLat,
            @Param("swLng") BigDecimal swLng,
            @Param("neLng") BigDecimal neLng
    );
}

