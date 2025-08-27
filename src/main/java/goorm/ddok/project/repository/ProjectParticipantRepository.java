package goorm.ddok.project.repository;

import goorm.ddok.project.domain.ProjectParticipant;
import goorm.ddok.project.domain.ProjectRecruitment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectParticipantRepository extends JpaRepository<ProjectParticipant, Long> {

    List<ProjectParticipant> findByPosition_ProjectRecruitment(ProjectRecruitment recruitment);
}
