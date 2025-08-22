package goorm.ddok.project.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project_recruitment_trait")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class ProjectRecruitmentTrait {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectRecruitment projectRecruitment;

    @Column(nullable = false, length = 64)
    private String traitName;

    @PrePersist
    @PreUpdate
    private void normalize() {
        if(traitName != null) traitName = traitName.trim();
    }

}
