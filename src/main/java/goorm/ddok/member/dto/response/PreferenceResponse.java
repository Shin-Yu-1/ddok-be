package goorm.ddok.member.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenceResponse {
    private Long id;
    private String username;
    private String email;
    private String mainPosition;
    private String profileImageUrl;
    private String nickname;
    private boolean isPreferences;
    private PreferenceDetailResponse preferences;
}
