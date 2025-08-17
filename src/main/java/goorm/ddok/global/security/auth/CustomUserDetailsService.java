package goorm.ddok.global.security.auth;

import goorm.ddok.member.domain.User;
import goorm.ddok.member.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // 생성자 주입 (lombok @RequiredArgsConstructor 써도 OK)
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // userId를 문자열로 받아서 User 조회
    @Override
    public UserDetails loadUserByUsername(String userIdStr) throws UsernameNotFoundException {
        long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("잘못된 유저 ID: " + userIdStr);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. ID: " + userIdStr));
        return new CustomUserDetails(user);
    }
}
