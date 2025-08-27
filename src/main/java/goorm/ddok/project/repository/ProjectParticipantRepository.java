package goorm.ddok.project.repository;

import goorm.ddok.project.domain.ParticipantRole;
import goorm.ddok.project.domain.ProjectParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectParticipantRepository extends JpaRepository<ProjectParticipant, Long> {

    // 리더/참가자 조회용 기존 메서드들 유지
    Optional<ProjectParticipant> findFirstByPosition_ProjectRecruitment_IdAndRoleAndDeletedAtIsNull(Long projectId, ParticipantRole role);
    List<ProjectParticipant> findByPosition_ProjectRecruitment_IdAndDeletedAtIsNull(Long projectId);

    long countByPosition_IdAndDeletedAtIsNull(Long positionId);

    // 삭제용
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ProjectParticipant p where p.position.projectRecruitment.id = :projectId")
    void deleteByPosition_ProjectRecruitment_Id(@Param("projectId") Long projectId);
}
