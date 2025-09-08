package goorm.ddok.team.domain;

import goorm.ddok.member.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "team_members")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소속 팀 (FK: team.id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /** 사용자 (FK: user.id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 역할 (LEADER / MEMBER) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeamMemberRole role;

    /** 가입 시각 (자동 입력) */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /** 탈퇴/추방 시각 (soft delete) */
    private Instant deletedAt;

    public void expel() {
        this.deletedAt = Instant.now();
    }

}
