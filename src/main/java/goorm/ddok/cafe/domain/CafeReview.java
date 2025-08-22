package goorm.ddok.cafe.domain;

import goorm.ddok.member.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter @Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cafe_review",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cafe_review_cafe_user_status",
                        columnNames = {"cafe_id","user_id","status"})
        },
        indexes = {
                @Index(name = "idx_cafe_review_cafe_id", columnList = "cafe_id"),
                @Index(name = "idx_cafe_review_user_id", columnList = "user_id"),
                @Index(name = "idx_cafe_review_rating", columnList = "rating")
        })
public class CafeReview {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cafe_id", nullable = false)
    private Cafe cafe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(name = "rating",precision = 2, scale = 1, nullable = false)
    private BigDecimal rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",length = 20, nullable = false)
    @Builder.Default
    private CafeReviewStatus status = CafeReviewStatus.ACTIVE;

    @Column(name="created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name="updated_at")
    private Instant updatedAt;

    @Column(name="deleted_at")
    private Instant deletedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
        this.status = CafeReviewStatus.DELETED;
    }
}