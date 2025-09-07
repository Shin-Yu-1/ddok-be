package goorm.ddok.evaluation.repository;

import goorm.ddok.evaluation.domain.TeamEvaluationScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamEvaluationScoreRepository extends JpaRepository<TeamEvaluationScore, Long> {

    List<TeamEvaluationScore> findByEvaluationIdAndEvaluatorUserId(Long evaluationId, Long evaluatorUserId);

    boolean existsByEvaluationIdAndEvaluatorUserIdAndTargetUserId(Long evaluationId, Long evaluatorUserId, Long targetUserId);
}