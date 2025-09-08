package goorm.ddok.evaluation.domain;

import goorm.ddok.team.domain.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "team_evaluation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class TeamEvaluation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EvaluationStatus status = EvaluationStatus.OPEN;

    @Column(name = "opened_at")
    private Instant openedAt;

    @Column(name = "closes_at")
    private Instant closesAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // setter는 필요부분만 열어두기
    public void setStatus(EvaluationStatus s) { this.status = s; }
    public Instant getClosesAt() { return closesAt; }

    @PrePersist
    private void onCreate() {
        Instant now = Instant.now();
        if (openedAt == null) openedAt = now;
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        // 생성 시점 +7일 자동 마감 설정
        if (closesAt == null) closesAt = openedAt.plusSeconds(7L * 24 * 60 * 60);
    }

    @PreUpdate
    private void onUpdate() { this.updatedAt = Instant.now(); }
}