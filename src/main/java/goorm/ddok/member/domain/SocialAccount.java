package goorm.ddok.member.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "social_account",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "providerUserId"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SocialAccount {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)  // ex) "KAKAO"
    private String provider;

    @Column(nullable = false)  // 카카오의 id
    private String providerUserId;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    @JsonIgnore
    @ToString.Exclude
    private User user;
}