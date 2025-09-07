package goorm.ddok.evaluation.domain;

import goorm.ddok.team.domain.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "team_evaluation")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder(toBuilder = true)
public class TeamEvaluation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private EvaluationStatus status;

    @Column(name="opened_at")
    private Instant openedAt;

    @Column(name="closes_at")
    private Instant closesAt;

    @Column(name="created_at")
    private Instant createdAt;

    @Column(name="updated_at")
    private Instant updatedAt;
}