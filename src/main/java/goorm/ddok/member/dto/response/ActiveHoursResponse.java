package goorm.ddok.member.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveHoursResponse {
    private String start;
    private String end;
}
