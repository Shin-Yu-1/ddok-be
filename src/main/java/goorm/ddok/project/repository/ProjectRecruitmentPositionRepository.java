package goorm.ddok.project.repository;

import goorm.ddok.project.domain.ProjectRecruitmentPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRecruitmentPositionRepository extends JpaRepository<ProjectRecruitmentPosition, Long> {
    Optional<ProjectRecruitmentPosition> findByProjectRecruitmentIdAndPositionName(Long projectId, String positionName);
}
