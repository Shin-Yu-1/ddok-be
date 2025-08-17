package goorm.ddok.member.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;


@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = "password")
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("실명")
    @Column(nullable = false)
    private String username;

    @Comment("닉네임(고유)")
    @Column(name = "nickname", unique = true, length = 12)
    private String nickname;

    @Comment("이메일(고유)")
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Comment("전화번호(고유)")
    @Column(name = "phone_number", nullable = false, unique = true, length = 11)
    private String phoneNumber;

    @Comment("비밀번호 해시")
    @JsonIgnore
    @Column(nullable = false, length = 100)
    private String password;

    @Comment("프로필 이미지 URL")
    @Column(length = 1024)
    private String profileImageUrl;

    @Comment("이메일 인증 여부")
    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @CreatedDate
    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false, columnDefinition = "TIMESTAMPTZ")
    @Setter(AccessLevel.NONE)
    private Instant updatedAt;


    public User(String username, String nickname, String email, String phoneNumber, String password, String profileImageUrl) {
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
    }
}
