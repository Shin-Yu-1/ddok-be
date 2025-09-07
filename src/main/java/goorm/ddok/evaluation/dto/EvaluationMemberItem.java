package goorm.ddok.evaluation.dto;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EvaluationMemberItem {

    private Long memberId;      // team_member.id
    private Boolean isMine;     // 항상 false (모달에서는 자기 자신 제외)
    private SimpleUserDto user;

    private Boolean isEvaluated; // 내가 이미 이 멤버를 평가했는지
    private List<ScoreItem> scores; // 내가 준 점수 목록(없으면 빈 배열)
}