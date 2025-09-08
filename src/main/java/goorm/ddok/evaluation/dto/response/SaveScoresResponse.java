package goorm.ddok.evaluation.dto.response;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SaveScoresResponse {
    private Long evaluationId;
    private Long targetUserId;
    private Boolean isEvaluated;
}