package goorm.ddok.evaluation.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "team_evaluation_score",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_eval_evaluator_target_item",
                columnNames = {"evaluation_id","evaluator_user_id","target_user_id","item_id"}
        ))
@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamEvaluationScore {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="evaluation_id", nullable=false)
    private Long evaluationId;

    @Column(name="evaluator_user_id", nullable=false)
    private Long evaluatorUserId;

    @Column(name="target_user_id", nullable=false)
    private Long targetUserId;

    @Column(name="item_id", nullable=false)
    private Long itemId;

    @Column(nullable=false)
    private short score; // tinyint

    @Column(name="created_at")
    private Instant createdAt;
}