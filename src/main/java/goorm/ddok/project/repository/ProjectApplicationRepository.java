package goorm.ddok.project.repository;

import goorm.ddok.project.domain.ProjectApplication;
import goorm.ddok.project.domain.ProjectRecruitment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, Long> {

    int countByProjectRecruitment(ProjectRecruitment recruitment);

    Optional<ProjectApplication> findByUser_IdAndPosition_ProjectRecruitment_Id(Long userId, Long positionId);
}
