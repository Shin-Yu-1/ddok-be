package goorm.ddok.study.domain;

import goorm.ddok.member.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study_recruitment")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class StudyRecruitment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 리더 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leader_user_id", nullable = false)
    private User user;

    /** 제목 */
    @Column(nullable = false, length = 100)
    private String title;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeamStatus teamStatus;

    /** 시작일 */
    @Column(nullable = false)
    private LocalDate startDate;

    /** 예상 기간(개월 단위) */
    @Column(nullable = false)
    private Integer expectedMonths;

    /** 모드 (온라인/오프라인) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyMode mode;

    /** 지역 정보 (오프라인일 경우 필수) */
    @Column(length = 50)
    private String region1DepthName;

    @Column(length = 50)
    private String region2DepthName;

    @Column(length = 50)
    private String region3DepthName;

    @Column(length = 50)
    private String roadName;

    private BigDecimal latitude;
    private BigDecimal longitude;

    /** 나이 제한 */
    private Integer ageMin;
    private Integer ageMax;

    /** 정원 */
    @Column(nullable = false)
    private Integer capacity;

    /** 배너 이미지 */
    @Column(nullable = false, length = 1024)
    private String bannerImageUrl;

    /** 스터디 유형 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StudyType studyType;

    /** 본문 */
    @Column(nullable = false)
    private String contentMd;

    /** 시간 정보 */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    private Instant deletedAt;

    /** 연관관계 */
    @OneToMany(mappedBy = "studyRecruitment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyRecruitmentTrait> traits = new ArrayList<>();

    @OneToMany(mappedBy = "studyRecruitment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyApplication> applications = new ArrayList<>();

    @OneToMany(mappedBy = "studyRecruitment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyParticipant> participants = new ArrayList<>();
}