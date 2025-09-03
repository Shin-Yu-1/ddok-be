package goorm.ddok.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import goorm.ddok.member.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이름 + 휴대폰번호 회원 조회
    Optional<User> findByUsernameAndPhoneNumber(String username, String phoneNumber);

    // 이메일 중복 확인
    boolean existsByEmail(String email);

    // 휴대폰 번호 중복 확인
    boolean existsByPhoneNumber(String phoneNumber);

    // 로그인 계정 조회
    Optional<User> findByEmail(String email);

    // 이메일 + 이름 회원 조회
    Optional<User> findByEmailAndUsername(String email, String username);

    // 닉네임 중복 확인
    boolean existsByNickname(String nickname);

    // 닉네임 키워드로 조회
    List<User> findAllByNicknameContaining(String keyword);

    boolean existsByNicknameAndIdNot(String nickname, Long id); // 닉네임 중복(본인 제외)
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id); // 전화번호 중복(본인 제외)
}
