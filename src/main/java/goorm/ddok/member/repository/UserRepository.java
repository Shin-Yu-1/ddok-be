package goorm.ddok.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import goorm.ddok.member.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이름 + 휴대폰번호 회원 조회
    Optional<User> findByUsernameAndPhoneNumber(String username, String phoneNumber);
}
