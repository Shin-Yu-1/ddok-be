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
    private String mainPosition;
    private List<String> subPosition;
    private List<String> techStacks;
    private List<String> traits;
    private LocalDate birthDate;
    private LocationResponse location;
    private ActiveHoursResponse activeHours;
}
