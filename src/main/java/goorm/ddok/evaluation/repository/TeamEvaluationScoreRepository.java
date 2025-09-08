package goorm.ddok.evaluation.repository;

import goorm.ddok.evaluation.domain.TeamEvaluationScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamEvaluationScoreRepository extends JpaRepository<TeamEvaluationScore, Long> {

    List<TeamEvaluationScore> findByEvaluationIdAndEvaluatorUserId(Long evaluationId, Long evaluatorUserId);

    boolean existsByEvaluationIdAndEvaluatorUserIdAndTargetUserId(Long evaluationId, Long evaluatorUserId, Long targetUserId);

    @Query("""
    select count(distinct s.evaluatorUserId)
    from TeamEvaluationScore s
    where s.targetUserId = :targetUserId
""")
    long countDistinctEvaluatorsByTargetUserId(@Param("targetUserId") Long targetUserId);

    List<TeamEvaluationScore> findByEvaluationId(Long evaluationId);
}