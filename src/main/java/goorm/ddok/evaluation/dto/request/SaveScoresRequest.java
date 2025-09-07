package goorm.ddok.evaluation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SaveScoresRequest {

    @NotNull
    private Long targetUserId;

    @NotNull
    private List<ItemScore> scores;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class ItemScore {
        @NotNull private Long itemId;
        @NotNull private Integer score; // 1~5 권장
    }
}