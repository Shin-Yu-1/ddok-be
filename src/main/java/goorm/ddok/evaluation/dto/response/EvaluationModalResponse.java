package goorm.ddok.evaluation.dto.response;

import goorm.ddok.evaluation.dto.EvaluationItemDto;
import goorm.ddok.evaluation.dto.EvaluationMemberItem;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EvaluationModalResponse {

    private Long teamId;
    private String teamType;       // PROJECT/STUDY
    private Long evaluationId;
    private String status;         // OPEN/CLOSED/CANCELED
    private List<EvaluationMemberItem> items;
}