package goorm.ddok.evaluation.dto.request;

// package goorm.ddok.evaluation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveScoresRequest {

    @NotNull
    private Long targetUserId;

    // 점수 목록. 비어 있으면 서비스에서 "모든 항목 3점"으로 대체
    @Builder.Default
    private List<Score> scores = List.of();

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Score {
        private Long itemId;
        private Integer score;
    }
}