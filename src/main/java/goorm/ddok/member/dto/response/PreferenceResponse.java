package goorm.ddok.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "PreferenceResponse",
        description = "개인화 설정 응답 DTO",
        example = """
    {
      "id": 123,
      "username": "dev_yeji",
      "email": "yeji@example.com",
      "mainPosition": "백엔드",
      "profileImageUrl": "https://cdn.example.com/profiles/yeji.png",
      "nickname": "예지",
      "isPreferences": true,
      "preferences": {
        "mainPosition": "백엔드",
        "subPosition": ["프론트엔드", "데브옵스"],
        "techStacks": ["Java", "Spring Boot", "React"],
        "location": { "latitude": 37.5665, "longitude": 126.9780, "address": "서울특별시 강남구 테헤란로 123" },
        "traits": ["협업", "문제 해결"],
        "birthDate": "1997-10-10",
        "activeHours": { "start": "19", "end": "23" }
      }
    }
    """
)
public class PreferenceResponse {

    @Schema(description = "개인화 설정 ID", example = "123", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "사용자 아이디(계정명)", example = "dev_yeji", accessMode = Schema.AccessMode.READ_ONLY)
    private String username;

    @Schema(description = "사용자 이메일", example = "yeji@example.com", accessMode = Schema.AccessMode.READ_ONLY)
    private String email;

    @Schema(description = "대표 포지션", example = "백엔드", accessMode = Schema.AccessMode.READ_ONLY)
    private String mainPosition;

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profiles/yeji.png", accessMode = Schema.AccessMode.READ_ONLY)
    private String profileImageUrl;

    @Schema(description = "닉네임", example = "예지", accessMode = Schema.AccessMode.READ_ONLY)
    private String nickname;

    @Schema(
            description = "개인화 설정 여부",
            example = "true",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private boolean isPreferences;

    @Schema(
            description = "개인화 상세 정보",
            implementation = PreferenceDetailResponse.class,
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private PreferenceDetailResponse preferences;
}
