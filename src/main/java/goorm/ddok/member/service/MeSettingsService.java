package goorm.ddok.member.service;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.dto.request.NicknameUpdateRequest;
import goorm.ddok.member.dto.request.PhoneUpdateRequest;
import goorm.ddok.member.dto.request.ProfileImageUpdateRequest;
import goorm.ddok.member.dto.response.SettingsPageResponse;
import goorm.ddok.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class MeSettingsService {

    private final UserRepository userRepository;
    private final ProfileImageService profileImageService;

    private User requireMe(CustomUserDetails me) {
        if (me == null || me.getUser() == null) throw new GlobalException(ErrorCode.UNAUTHORIZED);
        return userRepository.findById(me.getUser().getId())
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));
    }

    /* ---------- 공통 빌더 ---------- */
    @Transactional(readOnly = true)
    public SettingsPageResponse getSettings(CustomUserDetails me) {
        User user = requireMe(me);
        return toSettingsDto(user);
    }

    /* ---------- 수정 후 개인정보 블록 반환 ---------- */
    public SettingsPageResponse updateProfileImage(ProfileImageUpdateRequest req, CustomUserDetails me) { // [CHANGED] 반환형
        User user = requireMe(me);

        // 전달된 URL 정리
        String url = (req.getProfileImageUrl() != null && !req.getProfileImageUrl().trim().isEmpty())
                ? req.getProfileImageUrl().trim()
                : null;

        // 값이 비었으면 닉네임(없으면 username) 기반 기본 이미지 생성
        if (url == null) { // [NEW]
            String seed = (user.getNickname() != null && !user.getNickname().isBlank())
                    ? user.getNickname()
                    : user.getUsername();
            url = profileImageService.generateProfileImageUrl(seed, 200); // 200px 고정(필요 시 상수화/옵션화)
        }

        user.setProfileImageUrl(url);
        userRepository.save(user);

        // 수정 후 최신 정보 반환
        return toSettingsDto(user); // [NEW]
    }

    public SettingsPageResponse updateNickname(NicknameUpdateRequest req, CustomUserDetails me) {
        User user = requireMe(me);
        String nick = (req.getNickname() == null) ? null : req.getNickname().trim();
        if (!StringUtils.hasText(nick)) {
            throw new GlobalException(ErrorCode.NICKNAME_INVALID);
        }
        // 중복 체크 (내 닉네임으로 바꾸는 건 허용)
        boolean exists = userRepository.existsByNicknameAndIdNot(nick, user.getId());
        if (exists) throw new GlobalException(ErrorCode.DUPLICATE_NICKNAME);

        user.setNickname(nick);
        userRepository.save(user);
        return toSettingsDto(user);
    }

    public SettingsPageResponse updatePhone(PhoneUpdateRequest req, CustomUserDetails me) {
        User user = requireMe(me);
        String phone = (req.getPhoneNumber() == null) ? null : req.getPhoneNumber().trim();
        if (!StringUtils.hasText(phone)) {
            throw new GlobalException(ErrorCode.PHONE_FORMAT_INVALID);
        }
        // (선택) 형식 검증: 01X + 7~8자리
        if (!phone.matches("^01[016789]\\d{7,8}$")) {
            throw new GlobalException(ErrorCode.PHONE_FORMAT_INVALID);
        }
        boolean exists = userRepository.existsByPhoneNumberAndIdNot(phone, user.getId());
        if (exists) throw new GlobalException(ErrorCode.DUPLICATE_PHONE_NUMBER);

        user.setPhoneNumber(phone);
        userRepository.save(user);
        return toSettingsDto(user);
    }

    public void deleteMe(CustomUserDetails me) {
        User user = requireMe(me);
        // 연관관계는 User 엔티티에 Cascade REMOVE가 이미 걸려 있으므로 하드 딜리트 가능
        userRepository.delete(user);
    }

    private SettingsPageResponse toSettingsDto(User user) {
        return SettingsPageResponse.builder()
                .userId(user.getId())
                .profileImageUrl(user.getProfileImageUrl())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .password("********") // 실제 비밀번호는 절대 반환하지 않음
                .build();
    }
}