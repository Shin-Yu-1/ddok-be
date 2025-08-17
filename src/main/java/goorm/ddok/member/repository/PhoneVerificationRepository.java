package goorm.ddok.member.repository;

import goorm.ddok.member.domain.AuthType;
import goorm.ddok.member.domain.PhoneVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {
    // 검증용
    Optional<PhoneVerification> findTopByPhoneNumberAndPhoneCodeOrderByCreatedAtDesc(String phoneNumber, String phoneCode);

    Optional<PhoneVerification> findByPhoneNumberAndPhoneCodeAndAuthType(String phoneNumber, String phoneCode, AuthType authType);
}
