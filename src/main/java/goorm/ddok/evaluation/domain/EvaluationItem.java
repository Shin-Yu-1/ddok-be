package goorm.ddok.evaluation.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "evaluation_item")
@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class EvaluationItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=40, unique=true)
    private String code;

    @Column(nullable=false, length=60)
    private String name;

    @Column(nullable=false, length=300)
    private String description;

    @Column(name="scale_min", nullable=false)
    private short scaleMin;

    @Column(name="scale_max", nullable=false)
    private short scaleMax;

    @Column(name="created_at") private Instant createdAt;
    @Column(name="updated_at") private Instant updatedAt;
}