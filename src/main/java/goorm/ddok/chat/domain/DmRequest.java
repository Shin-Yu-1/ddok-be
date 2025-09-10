package goorm.ddok.chat.domain;

import goorm.ddok.member.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(
        name = "dm_request",
        uniqueConstraints = {
                // 동일 from→to 에서 PENDING 중복 방지
                @UniqueConstraint(name = "uk_dm_req_from_to_status", columnNames = {"from_user_id", "to_user_id", "status"})
        },
        indexes = {
                @Index(name = "idx_dm_req_to_status", columnList = "to_user_id,status"),
                @Index(name = "idx_dm_req_from_to", columnList = "from_user_id,to_user_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class DmRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 요청 보낸 사람 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    /** 요청 받는 사람 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DmRequestStatus status;

    /** 수락 시 연결될 채팅방 ID (없으면 null) */
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** 응답(수락/거절) 시각 */
    @Column(name = "responded_at")
    private Instant respondedAt;

    // === 도메인 메서드 ===
    public boolean isPending() {
        return this.status == DmRequestStatus.PENDING;
    }

    public void accept() {
        if (this.status != DmRequestStatus.PENDING) throw new IllegalStateException("Already processed");
        this.status = DmRequestStatus.ACCEPTED;
    }

    public void reject() {
        if (this.status != DmRequestStatus.PENDING) throw new IllegalStateException("Already processed");
        this.status = DmRequestStatus.REJECTED;
    }

    public void cancel() {
        this.status = DmRequestStatus.CANCELED;
        this.respondedAt = Instant.now();
    }
}
