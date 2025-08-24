package goorm.ddok.project.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class ProjectBannerImageService {

    private static final String[] SAFE_BG_COLORS = {
            "#FFE599", "#B7E1CD", "#AED6F1", "#FFDAB9", "#F5CBA7", "#D7BDE2", "#D5F5E3"
    };

    public String generateBannerImageUrl(String projectName, int width, int height) {
        String text = (projectName == null || projectName.isBlank()) ? "PROJECT" : projectName;
        String bgColor = getBackgroundColor(projectName);
        String svg = buildSvg(text, bgColor, width, height);
        return encodeSvgToBase64(svg);
    }

    private String getBackgroundColor(String seed) {
        int index = Math.abs(seed.hashCode()) % SAFE_BG_COLORS.length;
        return SAFE_BG_COLORS[index];
    }

    private String buildSvg(String text, String bgColor, int width, int height) {
        int fontSize = width / 15; // 가로 사이즈 비례 폰트 크기
        String safeText = text.length() > 20 ? text.substring(0, 20) + "…" : text; // 너무 길면 잘라냄
        return String.format("""
            <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d">
              <rect width="100%%" height="100%%" fill="%s"/>
              <text x="50%%" y="55%%" font-size="%d" fill="black" font-weight="bold"
                    text-anchor="middle" dominant-baseline="middle" font-family="Inter">%s</text>
            </svg>
            """, width, height, bgColor, fontSize, safeText);
    }

    private String encodeSvgToBase64(String svg) {
        String base64 = Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
        return "data:image/svg+xml;base64," + base64;
    }
}
