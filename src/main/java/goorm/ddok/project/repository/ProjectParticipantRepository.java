package goorm.ddok.project.repository;

import goorm.ddok.project.domain.ProjectParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectParticipantRepository extends JpaRepository<ProjectParticipant, Long> {
}
