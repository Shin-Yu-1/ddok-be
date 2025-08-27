package goorm.ddok.project.repository;

import goorm.ddok.project.domain.ApplicationStatus;
import goorm.ddok.project.domain.ProjectApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, Long> {

    Optional<ProjectApplication> findByUser_IdAndPosition_ProjectRecruitment_Id(Long userId, Long positionId);

    // 프로젝트 기준 전체 지원 수
    @Query("""
           select count(a)
           from ProjectApplication a
           where a.position.projectRecruitment.id = :projectId
           """)
    long countAllByProjectId(Long projectId);

    // 포지션별 applied 집계
    interface PositionCountProjection {
        String getPositionName();
        Long getCnt();
    }

    @Query("""
           select a.position.positionName as positionName, count(a) as cnt
           from ProjectApplication a
           where a.position.projectRecruitment.id = :projectId
           group by a.position.positionName
           """)
    List<PositionCountProjection> countAppliedByPosition(Long projectId);

    // 포지션별 approved 집계
    @Query("""
           select a.position.positionName as positionName, count(a) as cnt
           from ProjectApplication a
           where a.position.projectRecruitment.id = :projectId
             and a.status = goorm.ddok.project.domain.ApplicationStatus.APPROVED
           group by a.position.positionName
           """)
    List<PositionCountProjection> countApprovedByPosition(Long projectId);

    // 내 지원 여부
    boolean existsByUser_IdAndPosition_ProjectRecruitment_Id(Long userId, Long projectId);

    // 내 승인 여부
    boolean existsByUser_IdAndPosition_ProjectRecruitment_IdAndStatus(Long userId, Long projectId, ApplicationStatus status);

    // 특정 포지션 참조 지원 수
    long countByPosition_Id(Long positionId);

    // 프로젝트 참가자(멤버) 제외하고 ‘지원자 현황’ 만들 때 사용할 원본(필요시)
    @Query("""
           select a
           from ProjectApplication a
           where a.position.projectRecruitment.id = :projectId
           """)
    List<ProjectApplication> findAllByProject(Long projectId);

}
