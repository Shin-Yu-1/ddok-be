package goorm.ddok.team.repository;

import goorm.ddok.team.domain.Team;
import goorm.ddok.team.domain.TeamType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByRecruitmentIdAndType(Long recruitmentId, TeamType type);

}
