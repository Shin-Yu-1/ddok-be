package goorm.ddok.project.repository;

import goorm.ddok.project.domain.ProjectApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, Long> {
    Optional<ProjectApplication> findByApplicantIdAndPosition_ProjectRecruitment_Id(Long applicantId, Long projectId);
}
