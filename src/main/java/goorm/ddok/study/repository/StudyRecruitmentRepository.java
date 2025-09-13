package goorm.ddok.study.repository;

import goorm.ddok.study.domain.TeamStatus;
import goorm.ddok.study.domain.StudyRecruitment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface StudyRecruitmentRepository extends JpaRepository<StudyRecruitment, Long>, JpaSpecificationExecutor<StudyRecruitment> {
    Optional<StudyRecruitment> findByIdAndDeletedAtIsNull(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update StudyRecruitment s set s.deletedAt = :now where s.id = :id and s.deletedAt is null")
    int softDeleteById(@Param("id") Long id, @Param("now") Instant now);


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
          sr.id as id,
          sr.title as title,
          sr.teamStatus as teamStatus,
          sr.bannerImageUrl as bannerImageUrl,
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

    @Query("""
    select distinct
      sr.id               as id,
      sr.title            as title,
      sr.teamStatus       as teamStatus,
      sr.bannerImageUrl   as bannerImageUrl,
      sr.region1depthName as region1depthName,
      sr.region2depthName as region2depthName,
      sr.region3depthName as region3depthName,
      sr.roadName         as roadName,
      sr.mainBuildingNo   as mainBuildingNo,
      sr.subBuildingNo    as subBuildingNo,
      sr.zoneNo           as zoneNo,
      sr.latitude         as latitude,
      sr.longitude        as longitude
    from StudyParticipant sp
      join sp.studyRecruitment sr
    where sp.deletedAt is null
      and sr.deletedAt is null
      and sp.user.id = :userId
      and sr.latitude  is not null
      and sr.longitude is not null
      and sr.latitude  between :swLat and :neLat
      and sr.longitude between :swLng and :neLng
""")
    List<MapRow> findAllInBoundsForProfile(
            @Param("userId") Long userId,
            @Param("swLat") BigDecimal swLat,
            @Param("neLat") BigDecimal neLat,
            @Param("swLng") BigDecimal swLng,
            @Param("neLng") BigDecimal neLng
    );

    interface StudyOverlayRow {
        Long getId();
        String getTitle();
        goorm.ddok.study.domain.TeamStatus getTeamStatus();
        String getBannerImageUrl();
        goorm.ddok.study.domain.StudyType getStudyType();
        Integer getCapacity();
        goorm.ddok.study.domain.StudyMode getMode();
        String getAddress();
        Integer getAgeMin();
        Integer getAgeMax();
        Integer getExpectedMonth();
        java.time.LocalDate getStartDate();
    }

    @Query(value = """
        SELECT
            s.id,
            s.title,
            s.team_status                          AS teamStatus,
            s.banner_image_url                     AS bannerImageUrl,
            s.study_type                           AS studyType,
            s.capacity                              AS capacity,
            s.mode                                  AS mode,
            TRIM(BOTH ' ' FROM COALESCE(s.region1depth_name, '') || ' ' || COALESCE(s.region2depth_name, '')) AS address,
            s.age_min                              AS ageMin,
            s.age_max                              AS ageMax,
            s.expected_months                AS expectedMonth,
            s.start_date                           AS startDate
        FROM study_recruitment s
        WHERE s.deleted_at IS NULL
          AND s.id = :id
        """, nativeQuery = true)
    Optional<StudyOverlayRow> findOverlayById(@Param("id") Long id);

    Page<StudyRecruitment> findByDeletedAtIsNull(Pageable pageable);

    long countByTeamStatusAndDeletedAtIsNull(goorm.ddok.study.domain.TeamStatus teamStatus);

}
