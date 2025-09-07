package goorm.ddok.evaluation.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "team_evaluation_score",
        indexes = {
                @Index(name="idx_eval_target", columnList = "evaluation_id,target_user_id"),
                @Index(name="idx_eval_evaluator", columnList = "evaluation_id,evaluator_user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name="uk_eval_once_per_item",
                        columnNames = {"evaluation_id","evaluator_user_id","target_user_id","item_id"})
        })
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder(toBuilder = true)
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

    @Column(name="score", nullable=false)
    private Integer score;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;

    // === 편의 게터 (ID 사용) ===
    public Long getTargetUserId() { return targetUserId; }
    public Long getItemId() { return itemId; }
}