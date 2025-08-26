package goorm.ddok.member.repository;

import goorm.ddok.member.domain.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    // 이메일 인증 코드 조회
    Optional<EmailVerification> findByEmailCode(String code);

    Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);
}