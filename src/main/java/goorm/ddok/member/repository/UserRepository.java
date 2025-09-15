package goorm.ddok.member.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import goorm.ddok.member.domain.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // 이름 + 휴대폰번호 회원 조회
    Optional<User> findByUsernameAndPhoneNumber(String username, String phoneNumber);

    // 이메일 중복 확인
    boolean existsByEmail(String email);

    // 휴대폰 번호 중복 확인
    boolean existsByPhoneNumber(String phoneNumber);

    // 로그인 계정 조회
    Optional<User> findByEmail(String email);

    // 이메일 + 이름 회원 조회
    Optional<User> findByEmailAndUsername(String email, String username);

    // 닉네임 중복 확인
    boolean existsByNickname(String nickname);

    // 닉네임 키워드로 조회
    List<User> findAllByNicknameContaining(String keyword);

    boolean existsByNicknameAndIdNot(String nickname, Long id); // 닉네임 중복(본인 제외)
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id); // 전화번호 중복(본인 제외)

    interface MapRow {
        Long getId();
        String getNickname();
        String getProfileImageUrl();
        String getPositionName();
        String getRegion1DepthName();
        String getRegion2DepthName();
        String getRegion3DepthName();
        String getRoadName();
        String getMainBuildingNo();
        String getSubBuildingNo();
        String getZoneNo();
        BigDecimal getLatitude();
        BigDecimal getLongitude();
        BigDecimal getTemperature();
    }

    @Query("""
        select distinct
          u.id as id,
          u.nickname as nickname,
          u.profileImageUrl as profileImageUrl,
          coalesce(pPri.positionName, pSec1.positionName) as positionName,
          l.region1DepthName as region1DepthName,
          l.region2DepthName as region2DepthName,
          l.region3DepthName as region3DepthName,
          l.roadName as roadName,
          l.mainBuildingNo as mainBuildingNo,
          l.subBuildingNo as subBuildingNo,
          l.zoneNo as zoneNo,
          l.activityLatitude as latitude,
          l.activityLongitude as longitude,
          r.temperature as temperature
        from User u
        join u.location l
        left join goorm.ddok.member.domain.UserPosition pPri
          on pPri.user = u and pPri.type = goorm.ddok.member.domain.UserPositionType.PRIMARY
        left join goorm.ddok.member.domain.UserPosition pSec1
          on pSec1.user = u
         and pSec1.type = goorm.ddok.member.domain.UserPositionType.SECONDARY
         and pSec1.ord = 1
        left join goorm.ddok.reputation.domain.UserReputation r
          on r.user = u
        where u.isPublic = true
          and l.activityLatitude  is not null
          and l.activityLongitude is not null
          and l.activityLatitude  between :swLat and :neLat
          and l.activityLongitude between :swLng and :neLng
    """)
    List<MapRow> findPublicPlayersInBounds(
            @Param("swLat") BigDecimal swLat,
            @Param("neLat") BigDecimal neLat,
            @Param("swLng") BigDecimal swLng,
            @Param("neLng") BigDecimal neLng
    );


    @Query("select u from User u where u.id = :userId")
    Optional<User> findByUserId(@Param("userId") Long userId);


    interface UserOverlayRow {
        Long getId();
        String getNickname();
        String getProfileImageUrl();
        String getMainBadgeType();
        String getMainBadgeTier();
        Boolean getAbandonGranted();
        Integer getAbandonCount();
        String getMainPosition();
        String getAddress();
        java.math.BigDecimal getTemperature();

        Long getLatestProjectId();
        String getLatestProjectTitle();
        goorm.ddok.project.domain.TeamStatus getLatestProjectTeamStatus();

        Long getLatestStudyId();
        String getLatestStudyTitle();
        goorm.ddok.study.domain.TeamStatus getLatestStudyTeamStatus();
    }

    @Query(value = """
        SELECT
            u.id,
            u.nickname,
            COALESCE(u.profile_image_url, '') AS profileImageUrl,
            COALESCE(
                      (SELECT up.position_name
                         FROM user_position up
                        WHERE up.user_id = u.id
                          AND up.type = 'PRIMARY'
                        ORDER BY up.created_at DESC
                        LIMIT 1),
                      (SELECT up2.position_name
                         FROM user_position up2
                        WHERE up2.user_id = u.id
                          AND up2.type = 'SECONDARY'
                          AND up2.ord = 1
                        ORDER BY up2.created_at DESC
                        LIMIT 1)
                    ) AS mainPosition,
    
            TRIM(BOTH ' ' FROM COALESCE(ul.region_1depth_name, '') || ' ' || COALESCE(ul.region_2depth_name, '')) AS address,
    
            r.temperature AS temperature,
    
            (SELECT pr.id
               FROM project_recruitment pr
               JOIN project_recruitment_position prp ON prp.project_id = pr.id
               JOIN project_participant pp ON pp.position_id = prp.id AND pp.user_id = u.id
              WHERE pr.deleted_at IS NULL
                AND pp.deleted_at IS NULL
              ORDER BY pr.created_at DESC
              LIMIT 1) AS latestProjectId,
    
            (SELECT pr.title
               FROM project_recruitment pr
               JOIN project_recruitment_position prp ON prp.project_id = pr.id
               JOIN project_participant pp ON pp.position_id = prp.id AND pp.user_id = u.id
              WHERE pr.deleted_at IS NULL
                AND pp.deleted_at IS NULL
              ORDER BY pr.created_at DESC
              LIMIT 1) AS latestProjectTitle,
    
            (SELECT pr.team_status
               FROM project_recruitment pr
               JOIN project_recruitment_position prp ON prp.project_id = pr.id
               JOIN project_participant pp ON pp.position_id = prp.id AND pp.user_id = u.id
              WHERE pr.deleted_at IS NULL
                AND pp.deleted_at IS NULL
              ORDER BY pr.created_at DESC
              LIMIT 1) AS latestProjectTeamStatus,
    
            (SELECT sr.id
               FROM study_recruitment sr
               JOIN study_participant sp ON sp.study_id = sr.id AND sp.user_id = u.id
              WHERE sr.deleted_at IS NULL
                AND sp.deleted_at IS NULL
              ORDER BY sr.created_at DESC
              LIMIT 1) AS latestStudyId,
    
            (SELECT sr.title
               FROM study_recruitment sr
               JOIN study_participant sp ON sp.study_id = sr.id AND sp.user_id = u.id
              WHERE sr.deleted_at IS NULL
                AND sp.deleted_at IS NULL
              ORDER BY sr.created_at DESC
              LIMIT 1) AS latestStudyTitle,
    
            (SELECT sr.team_status
               FROM study_recruitment sr
               JOIN study_participant sp ON sp.study_id = sr.id AND sp.user_id = u.id
              WHERE sr.deleted_at IS NULL
                AND sp.deleted_at IS NULL
              ORDER BY sr.created_at DESC
              LIMIT 1) AS latestStudyTeamStatus
    
        FROM users u
        LEFT JOIN user_location ul ON ul.user_id = u.id
        LEFT JOIN user_reputation r ON r.user_id = u.id
        WHERE u.is_public = TRUE
          AND u.id = :id
        """, nativeQuery = true)
    Optional<UserOverlayRow> findOverlayById(@Param("id") Long id);

    @Query("""
    SELECT u FROM User u
    LEFT JOIN FETCH u.location loc
    LEFT JOIN u.positions pos
    WHERE (
        :keyword IS NULL OR :keyword = '' OR
        LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        (LOWER(pos.positionName) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR
        (LOWER(COALESCE(loc.region1DepthName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR
        (LOWER(COALESCE(loc.region2DepthName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR
        (LOWER(COALESCE(loc.region3DepthName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR
        (LOWER(COALESCE(loc.roadName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR
        (LOWER(COALESCE(loc.mainBuildingNo, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR
        (LOWER(COALESCE(loc.subBuildingNo, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR
        (LOWER(CONCAT(COALESCE(loc.region1DepthName, ''), ' ', COALESCE(loc.region2DepthName, ''))) LIKE LOWER(CONCAT('%', :keyword, '%')))
    )
    AND (
        (:keyword IS NOT NULL AND :keyword != '')
    )
    ORDER BY LOWER(u.nickname) ASC, u.id ASC
    """)
    Page<User> searchPlayersWithKeyword(@Param("keyword") String keyword, Pageable pageable);
}
