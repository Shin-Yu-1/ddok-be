package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
@Schema(name = "ProfileImageUploadForm", description = "프로필 이미지 업로드 폼")
public class ProfileImageUploadForm {

    @Schema(
            description = "프로필 이미지 파일",
            type = "string",
            format = "binary"
    )
    private MultipartFile file;

    @Schema(
            description = "true면 닉네임 기반 Placeholder로 강제 교체",
            example = "false"
    )
    private Boolean forcePlaceholder;
}