package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(name = "ProfileImageUpdateRequest", example = """
{ "profileImageUrl": "https://cdn.example.com/profiles/me.png" }
""")
public class ProfileImageUpdateRequest {
    @Size(max = 1024, message = "프로필 이미지 URL은 1024자 이하여야 합니다.")
    private String profileImageUrl; // 비우면 null 저장
}