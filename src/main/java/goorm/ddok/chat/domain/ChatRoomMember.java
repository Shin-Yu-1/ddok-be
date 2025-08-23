package goorm.ddok.chat.domain;

import goorm.ddok.member.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chat_room_member",
        indexes = { @Index(name = "idx_chat_member_user", columnList = "user_id") })
@EntityListeners(AuditingEntityListener.class)
public class ChatRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @MapsId와 @IdClass 제거하고 일반적인 @ManyToOne 관계로 변경
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", foreignKey = @ForeignKey(name = "fk_chat_room_member_room"))
    private ChatRoom roomId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_chat_room_member_user"))
    private User userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatMemberRole role = ChatMemberRole.MEMBER;

    @Column(nullable = false)
    private Boolean muted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_read_message_id", foreignKey = @ForeignKey(name = "fk_chat_room_member_last_read"))
    private ChatMessage lastReadMessageId;

    @Column(nullable = false, updatable = false)
    private Instant joinedAt;

    @PrePersist
    void onCreate() {
        if (joinedAt == null) joinedAt = Instant.now();
    }
}