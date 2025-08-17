package goorm.ddok.member.service;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.jwt.JwtTokenProvider;
import goorm.ddok.global.security.token.ReauthTokenService;
import goorm.ddok.global.security.token.RefreshTokenService;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.dto.request.SignUpRequest;
import goorm.ddok.member.dto.response.SignUpResponse;
import goorm.ddok.member.repository.PhoneVerificationRepository;
import goorm.ddok.member.repository.UserRepository;
import goorm.ddok.member.util.NicknameGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PhoneVerificationRepository verificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ProfileImageService profileImageService;
    private final EmailVerificationService emailVerificationService;
    private final RefreshTokenService refreshTokenService;
    private final ReauthTokenService reauthTokenService;

    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%]).{8,}$";
    private static final Pattern PASSWORD_REGEX = Pattern.compile(PASSWORD_PATTERN);
    private static final String NAME_PATTERN = "^[가-힣]{2,20}$";
    private static final Pattern NAME_REGEX = Pattern.compile(NAME_PATTERN);


    @Transactional
    public SignUpResponse signup(SignUpRequest request) {
        if (!NAME_REGEX.matcher(request.getUsername()).matches()) {
            throw new GlobalException(ErrorCode.INVALID_USERNAME);
        }

        if (!request.getPassword().equals(request.getPasswordCheck())) {
            throw new GlobalException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }

        if (request.getPassword().length() < 8) {
            throw new GlobalException(ErrorCode.PASSWORD_TOO_SHORT);
        }

        if (!PASSWORD_REGEX.matcher(request.getPassword()).matches()) {
            throw new GlobalException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new GlobalException(ErrorCode.DUPLICATE_EMAIL);
        }
        Pattern phonePattern = Pattern.compile("^01[0|1|6|7|8|9][0-9]{7,8}$");
        if (!phonePattern.matcher(request.getPhoneNumber()).matches()) {
            throw new GlobalException(ErrorCode.INVALID_PHONE_FORMAT);
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new GlobalException(ErrorCode.PHONE_NUMBER_ALREADY_USED);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        String code = emailVerificationService.createVerification(request.getEmail());
        emailVerificationService.sendVerificationEmail(request.getEmail(), code);

        User user = userRepository.save(User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(encodedPassword)
                .emailVerified(false)
                .build());

        return SignUpResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build();
    }

    public boolean isEmailAlreadyExist(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new GlobalException(ErrorCode.DUPLICATE_EMAIL);
        }
        return true;
    }
}
