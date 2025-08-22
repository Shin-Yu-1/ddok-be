package goorm.ddok.chat.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import goorm.ddok.member.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Check(constraints = """
    (room_type = 'PRIVATE' AND private_a_user_id IS NOT NULL AND private_b_user_id IS NOT NULL
     AND private_a_user_id <> private_b_user_id AND owner_user_id IS NULL)
    OR
    (room_type = 'GROUP' AND owner_user_id IS NOT NULL
     AND private_a_user_id IS NULL AND private_b_user_id IS NULL)
""")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chat_room")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private  ChatRoomType roomType;

    @Column()
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_chat_room_owner_user"))
    @ToString.Exclude
    @JsonIgnore
    private User ownerUserId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_chat_room_private_a_user"))
    @ToString.Exclude
    @JsonIgnore
    private User privateAUserId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_chat_room_private_b_user"))
    @ToString.Exclude
    @JsonIgnore
    private User privateBUserId;

    @Column()
    private Instant lastMessageAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
