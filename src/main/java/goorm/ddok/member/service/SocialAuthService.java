package goorm.ddok.member.service;

import goorm.ddok.member.domain.SocialAccount;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.repository.SocialAccountRepository;
import goorm.ddok.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final SocialAccountRepository socialAccountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String PROVIDER_KAKAO = "KAKAO";

    @Transactional
    public User upsertKakaoUser(String kakaoId, String email, String kakaoNickname, String profileImageUrl) {
        if (kakaoId == null || kakaoId.isBlank()) {
            throw new IllegalArgumentException("kakaoId is required");
        }

        return socialAccountRepository.findByProviderAndProviderUserId(PROVIDER_KAKAO, kakaoId)
                .map(sa -> {
                    User u = sa.getUser();
                    // 기존 유저도 원하는 규칙으로 동기화
                    updateUserFields(u, kakaoId, email, kakaoNickname, profileImageUrl);
                    return u;
                })
                .orElseGet(() -> {
                    // 신규 유저 생성 + 소셜계정 연결
                    User u = createNewUserFromKakao(kakaoId, email, kakaoNickname, profileImageUrl);
                    SocialAccount sa = SocialAccount.builder()
                            .provider(PROVIDER_KAKAO)
                            .providerUserId(kakaoId)
                            .user(u)
                            .build();
                    socialAccountRepository.save(sa);
                    return u;
                });
    }

    private void updateUserFields(User u, String kakaoId, String email, String kakaoNickname, String profileImageUrl) {
        // username ← 카카오 닉네임(사람 이름/표시용)
        String desiredUsername = safeUsernameFromKakaoNickname(kakaoNickname);
        if (!desiredUsername.equals(u.getUsername())) {
            u.setUsername(desiredUsername);
        }


        if (email != null && !email.isBlank() && !email.equals(u.getEmail())) {
            u.setEmail(email);
        }
        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
            u.setProfileImageUrl(profileImageUrl);
        }
    }

    private User createNewUserFromKakao(String kakaoId, String email, String kakaoNickname, String profileImageUrl) {
        String desiredUsername = safeUsernameFromKakaoNickname(kakaoNickname);

        String safeEmail  = ""; // 그냥 없는 거로 처리
        String safePhone  = ""; // 그냥 없는 거로 처리
        String encodedPw  = passwordEncoder.encode(UUID.randomUUID().toString()); // NOT NULL

        return userRepository.save(
                User.builder()
                        .username(desiredUsername)      // 카카오 닉네임 → username
                        .nickname(null)
                        .email(safeEmail)
                        .phoneNumber(safePhone)
                        .password(encodedPw)
                        .profileImageUrl(profileImageUrl)
                        .build()
        );
    }

    // 사람 표시용 username: 카카오 닉네임 없으면 기본값
    private String safeUsernameFromKakaoNickname(String kakaoNickname) {
        String base = (kakaoNickname == null || kakaoNickname.isBlank()) ? "카카오사용자" : kakaoNickname.trim();

        return base;
    }

    // nickname은 DB 제약 length=12 → "k_" + 뒤10자리 = 정확히 12자
    private String compactKakaoNickFromId(String kakaoId) {
        String digits = kakaoId.replaceAll("\\D", "");
        String last10 = (digits.length() >= 10) ? digits.substring(digits.length() - 10) : pad10(digits);
        return "k_" + last10;
    }

    private String pad10(String s) {
        String seed = (s == null ? "" : s) + UUID.randomUUID().toString().replaceAll("\\D", "");
        return seed.substring(0, 10);
    }

}