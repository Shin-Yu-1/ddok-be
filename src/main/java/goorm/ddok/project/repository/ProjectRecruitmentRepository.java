package goorm.ddok.project.repository;

import goorm.ddok.project.domain.ProjectRecruitment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRecruitmentRepository extends JpaRepository<ProjectRecruitment, Long> {
    Optional<ProjectRecruitment> findByIdAndDeletedAtIsNull(Long id);
}
