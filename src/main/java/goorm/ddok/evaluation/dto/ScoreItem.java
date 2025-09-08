package goorm.ddok.evaluation.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ScoreItem {
    private Long itemId;
    private Integer score;
}