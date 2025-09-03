package goorm.ddok.study.repository;

import goorm.ddok.project.domain.TeamStatus;
import goorm.ddok.study.domain.StudyRecruitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface StudyRecruitmentRepository extends JpaRepository<StudyRecruitment, Long> {
    Optional<StudyRecruitment> findByIdAndDeletedAtIsNull(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update StudyRecruitment s set s.deletedAt = :now where s.id = :id and s.deletedAt is null")
    int softDeleteById(@Param("id") Long id, @Param("now") Instant now);


    interface MapRow {
        Long getId();
        String getTitle();
        TeamStatus getTeamStatus();

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
          sr.id as id,
          sr.title as title,
          sr.teamStatus as teamStatus,
          sr.region1depthName as region1depthName,
          sr.region2depthName as region2depthName,
          sr.region3depthName as region3depthName,
          sr.roadName as roadName,
          sr.mainBuildingNo as mainBuildingNo,
          sr.subBuildingNo as subBuildingNo,
          sr.zoneNo as zoneNo,
          sr.latitude as latitude,
          sr.longitude as longitude
        from StudyRecruitment sr
        where sr.deletedAt is null
          and sr.latitude is not null
          and sr.longitude is not null
          and sr.latitude between :swLat and :neLat
          and sr.longitude between :swLng and :neLng
    """)
    List<StudyRecruitmentRepository.MapRow> findAllInBounds(
            @Param("swLat") BigDecimal swLat,
            @Param("neLat") BigDecimal neLat,
            @Param("swLng") BigDecimal swLng,
            @Param("neLng") BigDecimal neLng
    );
}
