package goorm.ddok.notification.domain;

import goorm.ddok.member.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "notification",
        indexes = {
                @Index(name = "idx_notification_receiver", columnList = "receiver_id"),
                @Index(name = "idx_notification_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 알림 수신자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User receiver;

    /** 알림 타입 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private NotificationType type;

    /** 표시 메시지 */
    @Column(nullable = false, length = 1000)
    private String message;

    /** 읽음 여부 */
    @Column(name = "is_read", nullable = false)
    private Boolean read = false;

    /** 처리 여부(수락/거절 등 액션 완료) */
    @Column(name = "is_processed", nullable = false)
    private Boolean processed = false;

    /** 처리 시각 */
    @Column(name = "processed_at")
    private Instant processedAt;

    /** 생성 시각 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ===== 컨텍스트 식별자들 =====
    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "study_id")
    private Long studyId;

    @Column(name = "team_id")
    private Long teamId;

    /** 신청자/요청자 식별자 */
    @Column(name = "applicant_user_id")
    private Long applicantUserId;   // 프로젝트/스터디 신청자

    @Column(name = "requester_user_id")
    private Long requesterUserId;   // DM 요청 보낸 사람

    // ===== 표시용 타이틀/이름(프론트 메시지 구성 편의) =====
    @Column(name = "project_title")
    private String projectTitle;

    @Column(name = "study_title")
    private String studyTitle;

    @Column(name = "team_name")
    private String teamName;

    @Column(name = "achievement_name")
    private String achievementName;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (read == null) read = false;
        if (processed == null) processed = false;
    }

    // ===== 헬퍼 메서드 =====
    public void markRead() {
        this.read = true;
    }

    public void markProcessed() {
        this.processed = true;
        this.processedAt = Instant.now();
    }
}
