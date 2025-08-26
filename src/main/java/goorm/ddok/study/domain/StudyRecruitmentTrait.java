package goorm.ddok.study.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "study_recruitment_trait")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class StudyRecruitmentTrait {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 공고 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id", nullable = false)
    private StudyRecruitment studyRecruitment;

    /** 성향명 */
    @Column(nullable = false, length = 64)
    private String traitName;

    @PrePersist
    @PreUpdate
    private void normalize() {
        if(traitName != null) traitName = traitName.trim();
    }

}
