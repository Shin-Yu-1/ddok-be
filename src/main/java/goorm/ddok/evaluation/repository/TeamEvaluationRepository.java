package goorm.ddok.evaluation.repository;

import goorm.ddok.evaluation.domain.EvaluationStatus;
import goorm.ddok.evaluation.domain.TeamEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TeamEvaluationRepository extends JpaRepository<TeamEvaluation, Long> {

    Optional<TeamEvaluation> findTopByTeam_IdOrderByIdDesc(Long teamId);
    List<TeamEvaluation> findAllByStatusAndClosesAtBefore(EvaluationStatus status, Instant before);

    boolean existsByTeam_IdAndStatus(Long teamId, EvaluationStatus status);
}