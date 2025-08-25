package goorm.ddok.global.util;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class BannerImageService {

    private static final String[] SAFE_BG_COLORS = {
            "#FFE599", "#B7E1CD", "#AED6F1", "#FFDAB9", "#F5CBA7", "#D7BDE2", "#D5F5E3"
    };

    public String generateBannerImageUrl(String text, String defaultText, int width, int height) {
        String safeText = (text == null || text.isBlank()) ? defaultText : text;
        String bgColor = getBackgroundColor(safeText);
        String svg = buildSvg(safeText, bgColor, width, height);
        return encodeSvgToBase64(svg);
    }

    private String getBackgroundColor(String seed) {
        int index = Math.abs(seed.hashCode()) % SAFE_BG_COLORS.length;
        return SAFE_BG_COLORS[index];
    }

    private String buildSvg(String text, String bgColor, int width, int height) {
        int fontSize = width / 15;
        String safeText = text.length() > 20 ? text.substring(0, 20) + "…" : text;
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