package goorm.ddok.project.repository;

import goorm.ddok.project.domain.ParticipantRole;
import goorm.ddok.project.domain.ProjectParticipant;
import goorm.ddok.project.domain.ProjectRecruitment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectParticipantRepository extends JpaRepository<ProjectParticipant, Long> {
    // 프로젝트 기준 참가자(리더/멤버)
    List<ProjectParticipant> findByPosition_ProjectRecruitment_IdAndDeletedAtIsNull(Long projectId);

    // 프로젝트의 리더 1명
    Optional<ProjectParticipant> findFirstByPosition_ProjectRecruitment_IdAndRoleAndDeletedAtIsNull(
            Long projectId, ParticipantRole role
    );

    // 특정 포지션을 참조하는 참가자 수
    long countByPosition_IdAndDeletedAtIsNull(Long positionId);

    List<ProjectParticipant> findByPosition_ProjectRecruitment(ProjectRecruitment recruitment);
}
