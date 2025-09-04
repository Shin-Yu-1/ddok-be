package goorm.ddok.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@Schema(name = "SettingsPageResponse", description = "개인정보변경 페이지 조회 응답")
public class SettingsPageResponse {
    private Long userId;
    private String profileImageUrl;
    private String username;
    private String nickname;
    private LocalDate birthDate;
    private String email;
    private String phoneNumber;

    @Schema(description = "실제 비밀번호는 내려주지 않습니다. 마스킹 값.", example = "********")
    private String password;
}