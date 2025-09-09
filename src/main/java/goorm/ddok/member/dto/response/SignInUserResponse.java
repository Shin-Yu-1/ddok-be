package goorm.ddok.member.dto.response;

import goorm.ddok.member.domain.User;
import goorm.ddok.member.domain.UserPosition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(
        name = "SignInUserResponse",
        description = "로그인 사용자 정보 응답 DTO",
        example = """
    {
      "id": 1,
      "username": "홍길동",
      "email": "test@test.com",
      "nickname": "고통스러운 개발자",
      "profileImageUrl": "https://cdn.pixabay.com/photo/2013/07/12/14/15/boy-148071_1280.png",
      "mainPosition": "백엔드",
      "isPreference": true,
      "location": {
        "latitude": 37.5665,
        "longitude": 126.9780,
        "address": "서울특별시 강남구 테헤란로 123"
      },
      "isSocial": false
    }
    """
)
public class SignInUserResponse {

    @Schema(description = "사용자 id", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private final Long id;

    @Schema(description = "사용자 실명", example = "홍길동", accessMode = Schema.AccessMode.READ_ONLY)
    private final String username;

    @Schema(description = "이메일", example = "test@test.com", accessMode = Schema.AccessMode.READ_ONLY)
    private final String email;

    @Schema(description = "닉네임", example = "고통스러운 개발자", accessMode = Schema.AccessMode.READ_ONLY)
    private final String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.pixabay.com/photo/2013/07/12/14/15/boy-148071_1280.png", accessMode = Schema.AccessMode.READ_ONLY)
    private final String profileImageUrl;

    @Schema(description = "대표 포지션", example = "백엔드", accessMode = Schema.AccessMode.READ_ONLY)
    private final String mainPosition;

    @Schema(description = "개인화 설정 여부", example = "true", accessMode = Schema.AccessMode.READ_ONLY)
    private final boolean IsPreference;

    @Schema(description = "사용자 위치 정보", implementation = LocationResponse.class, accessMode = Schema.AccessMode.READ_ONLY)
    private final LocationResponse location;

    @Schema(description = "소셜 로그인 여부", example = "false", accessMode = Schema.AccessMode.READ_ONLY)
    private final boolean IsSocial;

    public SignInUserResponse(User user, boolean isPreference, boolean isSocial, LocationResponse location) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.profileImageUrl = user.getProfileImageUrl();
        this.mainPosition = getMainPositionFromUser(user);
        this.IsPreference = isPreference;
        this.location = location;
        this.IsSocial = isSocial;
    }

    private String getMainPositionFromUser(User user) {
        if (user.getPositions() != null && !user.getPositions().isEmpty()) {
            return user.getPositions().stream()
                    .filter(position -> position.getType() == goorm.ddok.member.domain.UserPositionType.PRIMARY)
                    .findFirst()
                    .map(UserPosition::getPositionName)
                    .orElse(null);
        }
        return null;
    }
}
