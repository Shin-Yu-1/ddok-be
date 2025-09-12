package goorm.ddok.study.repository;

import goorm.ddok.study.domain.ApplicationStatus;
import goorm.ddok.study.domain.StudyApplication;
import goorm.ddok.study.domain.StudyRecruitment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Long> {

    /** 특정 모집글(StudyRecruitment)에 대한 전체 지원자 수 조회 */
    long countByStudyRecruitment(StudyRecruitment study);

    /** 특정 유저(userId)가 특정 스터디 모집글(studyId)에 지원했는지 여부 확인 */
    Optional<StudyApplication> findByUser_IdAndStudyRecruitment_Id(Long userId, Long studyId);

    /** 특정 모집글(StudyRecruitment)에 대해, 지정한 상태(status)의 지원자 목록 조회 (페이징) */
    Page<StudyApplication> findByStudyRecruitment_IdAndApplicationStatus(Long recruitmentId, ApplicationStatus applicationStatus, Pageable pageable);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from StudyApplication a where a.id = :id and a.applicationStatus = 'PENDING'")
    int deleteIfPending(@Param("id") Long id);

    long countByStudyRecruitmentAndApplicationStatus(StudyRecruitment study, ApplicationStatus status);

    boolean existsByUser_IdAndStudyRecruitment_IdAndApplicationStatus(Long userId, Long studyId, ApplicationStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update StudyApplication a set a.applicationStatus = 'PENDING' " +
            "where a.id = :id and a.applicationStatus = 'REJECTED'")
    int reapplyIfRejected(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update StudyApplication a
           set a.applicationStatus = :rejected
         where a.studyRecruitment.id = :recruitmentId
           and a.user.id = :userId
           and a.applicationStatus = :approved
    """)
    int markApprovedAsRejectedByRecruitmentAndUser(@Param("recruitmentId") Long recruitmentId,
                                                   @Param("userId") Long userId,
                                                   @Param("approved") goorm.ddok.study.domain.ApplicationStatus approved,
                                                   @Param("rejected") goorm.ddok.study.domain.ApplicationStatus rejected);

    // 사용 편의를 위한 디폴트 메서드
    default int markApprovedAsRejectedByRecruitmentAndUser(Long recruitmentId, Long userId) {
        return markApprovedAsRejectedByRecruitmentAndUser(
                recruitmentId, userId,
                goorm.ddok.study.domain.ApplicationStatus.APPROVED,
                goorm.ddok.study.domain.ApplicationStatus.REJECTED
        );
    }
}
