package goorm.ddok.project.repository;

import goorm.ddok.project.domain.ParticipantRole;
import goorm.ddok.project.domain.ProjectParticipant;
import goorm.ddok.project.domain.ProjectRecruitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProjectParticipantRepository extends JpaRepository<ProjectParticipant, Long> {
    /** 프로젝트 리더 조회 */
    @Query("SELECT p FROM ProjectParticipant p WHERE p.projectRecruitment.id = :projectId AND p.role = goorm.ddok.project.domain.ParticipantRole.LEADER")
    Optional<ProjectParticipant> findLeaderByProjectId(Long projectId);

    /** 특정 프로젝트의 모든 참여자 조회 */
    List<ProjectParticipant> findByProjectRecruitment(ProjectRecruitment recruitment);

    /** 특정 프로젝트 + 유저로 참여자 조회 (신청/참여 여부 확인용) */
    Optional<ProjectParticipant> findByProjectRecruitmentAndUserId(ProjectRecruitment recruitment, Long userId);

    /** 특정 프로젝트에서 특정 역할(MEMBER LEADER) 가진 사람들 */
    List<ProjectParticipant> findByProjectRecruitmentAndRole(ProjectRecruitment recruitment, ParticipantRole role);
}