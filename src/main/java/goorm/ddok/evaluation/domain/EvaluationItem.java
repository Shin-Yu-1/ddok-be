package goorm.ddok.evaluation.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "evaluation_item")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor @Builder(toBuilder = true)
public class EvaluationItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=40, unique = true)
    private String code;

    @Column(nullable=false, length=60)
    private String name;

    @Column(nullable=false, length=300)
    private String description;

    @Column(name="scale_min", nullable=false)
    private Integer scaleMin; // 보통 1

    @Column(name="scale_max", nullable=false)
    private Integer scaleMax; // 보통 5
}