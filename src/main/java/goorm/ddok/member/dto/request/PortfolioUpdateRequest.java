package goorm.ddok.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@Schema(name = "PortfolioUpdateRequest", description = "포트폴리오 링크 목록 전체 치환 요청", example = """
    {
      "portfolio": [
        { "linkTitle": "GitHub", "link": "https://github.com/xxx" },
        { "linkTitle": "Blog", "link": "https://blog.example.com" }
      ]
    }
    """)
public class PortfolioUpdateRequest {
    private List<Link> portfolio;

    @Getter @Setter
    @Schema(name = "PortfolioUpdateRequest.Link", description = "포트폴리오 링크", example = """
        { "linkTitle": "GitHub", "link": "https://github.com/xxx" }
        """)
    public static class Link {
        @NotBlank @Schema(description = "링크 제목", example = "GitHub")
        private String linkTitle;
        @NotBlank @Schema(description = "URL", example = "https://github.com/xxx")
        private String link;
    }
}