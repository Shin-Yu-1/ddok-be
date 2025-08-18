package goorm.ddok.member.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "user_position")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@EntityListeners(AuditingEntityListener.class)
@Check(constraints = "(type = 'PRIMARY' AND ord IS NULL) OR (type = 'SECONDARY' AND ord IN (1,2))")
public class UserPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 FK (N:1) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private User user;

    /** 포지션 이름: 입력받은 그대로 저장 */
    @Column(name = "position_name", nullable = false, length = 64)
    private String positionName;

    /** PRIMARY / SECONDARY */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private UserPositionType type;

    /** SECONDARY 순서(1/2), PRIMARY는 null */
    @Column(name = "ord")
    private Short ord;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** 입력값 가볍게 정리 */
    @PrePersist
    @PreUpdate
    private void normalize() {
        if (positionName != null) positionName = positionName.trim();
    }

    /** 팩토리 */
    public static UserPosition primaryOf(User user, String name) {
        return UserPosition.builder()
                .user(user)
                .positionName(name)
                .type(UserPositionType.PRIMARY)
                .ord(null)
                .build();
    }

    public static UserPosition secondaryOf(User user, String name, short ord) {
        return UserPosition.builder()
                .user(user)
                .positionName(name)
                .type(UserPositionType.SECONDARY)
                .ord(ord)
                .build();
    }
}
