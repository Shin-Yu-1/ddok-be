package goorm.ddok.reputation.repository;

import goorm.ddok.member.domain.User;
import goorm.ddok.reputation.domain.UserReputation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserReputationRepository extends JpaRepository<UserReputation, Long> {

    /** user_id 기준으로 조회 */
    Optional<UserReputation> findByUserId(Long userId);

    Optional<UserReputation> findByUser_Id(Long userId);

    Optional<UserReputation> findByUser(User user);

    @Query("select ur.user.id, ur.temperature from UserReputation ur where ur.user.id in :userIds")
    List<Object[]> findTempsByUserIds(@Param("userIds") Collection<Long> userIds);

    /**
     * 온도 TOP10 조회
     * 정렬 조건 :
     *  1) temperature 내림차순 (높은 온도 -> 낮은 온도)
     *  2) 같은 온도일 경우 updatedAt 내림차순 (가장 최근에 업데이트 된 사람 우선)
     *  LIMIT 10
     */
    List<UserReputation> findTop10ByOrderByTemperatureDescUpdatedAtDesc();

    /**
     * 온도 TOP1 조회
     * 정렬 조건은 TOP10 과 같다.
     */
    Optional<UserReputation> findTop1ByOrderByTemperatureDescUpdatedAtDesc();

//    @Query("""
//    SELECT ur
//    FROM UserReputation ur
//    JOIN ur.user u
//    JOIN u.location loc
//    WHERE loc.region1DepthName IN :regions
//    ORDER BY ur.temperature DESC, ur.updatedAt DESC
//    """)
//    Optional<UserReputation> findTop1ByRegions(@Param("regions") List<String> regions);

    Optional<UserReputation> findFirstByUser_Location_Region1DepthNameInOrderByTemperatureDescUpdatedAtDesc(List<String> regions);

}
