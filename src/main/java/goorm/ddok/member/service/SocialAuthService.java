package goorm.ddok.member.service;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.member.domain.SocialAccount;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.repository.SocialAccountRepository;
import goorm.ddok.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

        try {
            return socialAccountRepository.findByProviderAndProviderUserId(PROVIDER_KAKAO, kakaoId)
                    .map(sa -> {
                        User u = sa.getUser();
                        updateUserFields(u, kakaoId, email, kakaoNickname, profileImageUrl);
                        return u;
                    })
                    .orElseGet(() -> {
                        // 신규 생성
                        User u = createNewUserFromKakao(kakaoId, email, kakaoNickname, profileImageUrl);
                        SocialAccount sa = SocialAccount.builder()
                                .provider(PROVIDER_KAKAO)
                                .providerUserId(kakaoId)
                                .user(u)
                                .build();
                        socialAccountRepository.save(sa);
                        return u;
                    });

        } catch (DataIntegrityViolationException e) {
            // 동시성으로 인한 Unique 제약 위반 발생 시 → 다시 조회
            return socialAccountRepository.findByProviderAndProviderUserId(PROVIDER_KAKAO, kakaoId)
                    .map(SocialAccount::getUser)
                    .orElseThrow(() -> new GlobalException(ErrorCode.SOCIAL_LOGIN_FAILED));
        }
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

        String safeEmail  = (email != null && !email.isBlank()) ? email : null;
        String encodedPw  = passwordEncoder.encode(UUID.randomUUID().toString());

        return userRepository.save(
                User.builder()
                        .username(desiredUsername)
                        .nickname(null)
                        .email(safeEmail)
                        .phoneNumber(null)
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