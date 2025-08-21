package goorm.ddok.cafe.domain;

import goorm.ddok.member.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "cafe_review",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cafe_review_cafe_user", columnNames = {"cafe_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_cafe_review_cafe_id", columnList = "cafe_id"),
                @Index(name = "idx_cafe_review_user_id", columnList = "user_id"),
                @Index(name = "idx_cafe_review_rating", columnList = "rating")
        }
)
public class CafeReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cafe_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cafe_review_cafe"))
    private Cafe cafe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cafe_review_user"))
    private User user;

    // numeric(2,1) → BigDecimal(2,1)
    @Column(precision = 2, scale = 1, nullable = false)
    private BigDecimal rating;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private CafeReviewStatus status = CafeReviewStatus.ACTIVE;

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}