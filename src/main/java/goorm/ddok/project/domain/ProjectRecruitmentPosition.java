package goorm.ddok.project.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "project_recruitment_position")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class ProjectRecruitmentPosition {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 모집 공고 FK (N:1)*/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectRecruitment projectRecruitment;

    /** 모집 포지션명 (예 : 백엔드, 프론트엔드, 디자이너) */
    @Column(nullable = false, length = 64)
    private String positionName;

    /** 입력값 정리: 공백 제거*/
    @PrePersist
    @PreUpdate
    private void normalize() {
        if(positionName != null) positionName = positionName.trim();
    }
}
