package goorm.ddok.study.repository;

import goorm.ddok.study.domain.StudyParticipant;
import goorm.ddok.study.domain.StudyRecruitment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

import java.util.List;
import java.util.Optional;


public interface StudyParticipantRepository extends JpaRepository<StudyParticipant, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update StudyParticipant p
              set p.deletedAt = :now
            where p.studyRecruitment.id = :studyId
              and p.deletedAt is null
           """)
    int softDeleteByStudyId(@Param("studyId") Long studyId, @Param("now") Instant now);

    // 스터디 모집글 기준으로 참가자 전체 조회
    List<StudyParticipant> findByStudyRecruitment(StudyRecruitment study);

    List<StudyParticipant> findByStudyRecruitment_IdAndDeletedAtIsNull(Long studyId);

    // 유저 기준 참여 스터디 조회 (Soft Delete 제외)
    @Query("""
       select p
         from StudyParticipant p
        where p.user.id = :userId
          and p.deletedAt is null
       """)
    Page<StudyParticipant> findByUserId(@Param("userId") Long userId, Pageable pageable);


    // 스터디 모집글 기준 현재 참가자 수 (Soft Delete 제외)
    long countByStudyRecruitment_IdAndDeletedAtIsNull(Long studyId);

    /**
     * 스터디 단위에서 특정 유저의 참가자 정보를 조회한다.
     * - Soft Delete 제외
     */
    Optional<StudyParticipant> findByStudyRecruitment_IdAndUser_IdAndDeletedAtIsNull(
            Long studyId,
            Long userId
    );


}
