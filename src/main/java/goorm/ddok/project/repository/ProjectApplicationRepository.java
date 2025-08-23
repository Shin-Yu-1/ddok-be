package goorm.ddok.project.repository;

import goorm.ddok.project.domain.ProjectApplication;
import goorm.ddok.project.domain.ProjectRecruitment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, Long> {
    int countByProjectRecruitment(ProjectRecruitment recruitment);
}
