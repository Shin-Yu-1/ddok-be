package goorm.ddok.evaluation.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "team_evaluation")
@Getter @NoArgsConstructor @AllArgsConstructor @Builder(toBuilder = true)
public class TeamEvaluation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="team_id", nullable=false)
    private Long teamId;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private EvaluationStatus status;   // OPEN/CLOSED/CANCELED

    @Column(name="opened_at")
    private Instant openedAt;

    @Column(name="closes_at")
    private Instant closesAt;

    @Column(name="created_at") private Instant createdAt;
    @Column(name="updated_at") private Instant updatedAt;
}