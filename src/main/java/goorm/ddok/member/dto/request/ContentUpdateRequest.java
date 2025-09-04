package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(name = "ContentUpdateRequest", description = "자기소개 생성/수정 요청", example = """
    { "content": "안녕하세요! 백엔드 개발자입니다 :)" }
    """)
public class ContentUpdateRequest {
    @NotBlank
    @Schema(description = "자기소개 본문 (Markdown 허용)", example = "안녕하세요! 백엔드 개발자입니다 :)")
    private String content;
}