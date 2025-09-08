package goorm.ddok.evaluation.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SimpleUserDto {
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private String role; // LEADER/MEMBER (문자열)
    private Object mainBadge;   // 추후 실제 타입 연결
    private Object abandonBadge;
}