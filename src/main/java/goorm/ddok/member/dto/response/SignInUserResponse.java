package goorm.ddok.member.dto.response;

import goorm.ddok.member.domain.User;
import goorm.ddok.member.domain.UserPosition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "로그인 사용자 정보 응답 DTO")
public class SignInUserResponse {

    @Schema(description = "사용자 id", example = "1")
    private final Long id;

    @Schema(description = "사용자 실명", example = "홍길동")
    private final String username;

    @Schema(description = "이메일", example = "test@test.com")
    private final String email;

    @Schema(description = "닉네임", example = "고통스러운 개발자")
    private final String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.pixabay.com/photo/2013/07/12/14/15/boy-148071_1280.png")
    private final String profileImageUrl;

    @Schema(description = "대표 포지션", example = "백엔드")
    private final String mainPosition;

    @Schema(description = "개인화 설정 여부", example = "true")
    private final boolean IsPreference;

    @Schema(description = "사용자 위치 정보")
    private final LocationResponse location;

    public SignInUserResponse(User user, boolean isPreference, LocationResponse location) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.profileImageUrl = user.getProfileImageUrl();
        this.mainPosition = getMainPositionFromUser(user);
        this.IsPreference = isPreference;
        this.location = location;
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
