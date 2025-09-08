package goorm.ddok.evaluation.repository;

import goorm.ddok.evaluation.domain.EvaluationItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationItemRepository extends JpaRepository<EvaluationItem, Long> {

}