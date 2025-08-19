package goorm.ddok.member.service;

import goorm.ddok.member.domain.SocialAccount;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.repository.SocialAccountRepository;
import goorm.ddok.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final SocialAccountRepository socialRepo;
    private final UserRepository userRepo;

    @Transactional
    public User upsertKakaoUser(String kakaoId, String email, String nickname, String profileImageUrl) {
        // 1) 소셜 계정으로 기존 사용자 찾기
        var linked = socialRepo.findByProviderAndProviderUserId("KAKAO", kakaoId)
                .map(SocialAccount::getUser);
        if (linked.isPresent()) return linked.get();

        // 2) 이메일로 기존 사용자 매칭(있으면 연결)
        User user = null;
        if (email != null) {
            user = userRepo.findByEmail(email).orElse(null);
        }
        if (user == null) {
            // 3) 새 사용자 생성 (User 필드명에 맞게 최소값만)
            user = User.builder()
                    .email(email)
                    .nickname(nickname != null ? nickname : "user-" + kakaoId)
                    .build();
            user = userRepo.save(user);
        }

        // 4) 소셜 계정 링크 저장
        SocialAccount sa = SocialAccount.builder()
                .provider("KAKAO")
                .providerUserId(kakaoId)
                .user(user)
                .build();
        socialRepo.save(sa);

        return user;
    }
}