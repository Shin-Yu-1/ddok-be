package goorm.ddok.evaluation.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EvaluationItemDto {
    private Long itemId;
    private String code;
    private String name;
}