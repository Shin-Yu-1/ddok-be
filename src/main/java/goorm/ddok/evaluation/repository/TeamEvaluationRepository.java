package goorm.ddok.evaluation.repository;

import goorm.ddok.evaluation.domain.TeamEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamEvaluationRepository extends JpaRepository<TeamEvaluation, Long> {

    Optional<TeamEvaluation> findTopByTeam_IdOrderByIdDesc(Long teamId);
}