package goorm.ddok.team.domain;

import goorm.ddok.member.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 모집글 id (project_recruitment.id 또는 study_recruitment.id) */
    @Column(nullable = false)
    private Long recruitmentId;

    /** 팀 타입 : PROJECT / STUDY */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeamType type;

    /** 팀명(기본: 공고 제목) (2~30자, 필수) */
    @Column(nullable = false, length = 100)
    private String title;

    /** 리더 사용자 (FK: user.id) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_user_id", nullable = false)
    private User user;

    /** 생성 시각 (자동 입력) */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /** 최종 수정 시각 (자동 입력) */
    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    /** 삭제 시각 (soft delete) */
    private Instant deletedAt;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TeamMember> members = new ArrayList<>();

    /** 팀원 추가 */
    public void addMember(User user,TeamMemberRole role) {
        TeamMember member = TeamMember.builder()
                .team(this)
                .user(user)
                .role(role)
                .build();
        this.members.add(member);
    }
}
