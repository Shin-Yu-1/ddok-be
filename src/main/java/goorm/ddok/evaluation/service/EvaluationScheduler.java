package goorm.ddok.evaluation.service;

import goorm.ddok.evaluation.domain.EvaluationItem;
import goorm.ddok.evaluation.domain.EvaluationStatus;
import goorm.ddok.evaluation.domain.TeamEvaluation;
import goorm.ddok.evaluation.domain.TeamEvaluationScore;
import goorm.ddok.evaluation.repository.EvaluationItemRepository;
import goorm.ddok.evaluation.repository.TeamEvaluationRepository;
import goorm.ddok.evaluation.repository.TeamEvaluationScoreRepository;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.repository.UserRepository;
import goorm.ddok.reputation.domain.UserReputation;
import goorm.ddok.reputation.repository.UserReputationRepository;
import goorm.ddok.team.domain.TeamMember;
import goorm.ddok.team.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "ddok.legacy.evaluation.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class EvaluationScheduler {

    private final TeamEvaluationRepository evaluationRepository;
    private final TeamEvaluationScoreRepository scoreRepository;
    private final EvaluationItemRepository itemRepository;
    private final TeamMemberRepository teamMemberRepository;

    private final UserRepository userRepository;
    private final UserReputationRepository userReputationRepository;

    /** 완충값 c: 평가 초반 과민 반응 억제 / 후반 안정화 */
    private static final double C = 10.0;

    /**
     * 매일 자정(서울) 실행
     * - 마감일이 지난 OPEN 라운드를 CLOSED로 전환
     * - 미제출 (evaluator → target) 조합은 전 항목 3점으로 자동 채움
     * - 온도 갱신은 “기존 값 유지 + 점진 업데이트” 원칙
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void closeExpiredEvaluations() {
        Instant now = Instant.now();

        List<TeamEvaluation> toClose =
                evaluationRepository.findAllByStatusAndClosesAtBefore(EvaluationStatus.OPEN, now);

        if (toClose.isEmpty()) return;

        List<EvaluationItem> items = itemRepository.findAll();
        for (TeamEvaluation eval : toClose) {
            Long teamId = eval.getTeam().getId();

            List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
            if (members.isEmpty()) {
                eval.setStatus(EvaluationStatus.CLOSED);
                evaluationRepository.save(eval);
                continue;
            }

            // 이미 제출된 (evaluator-target) 쌍
            List<TeamEvaluationScore> existingScores = scoreRepository.findByEvaluationId(eval.getId());
            Set<String> existingPairs = existingScores.stream()
                    .map(s -> s.getEvaluatorUserId() + "-" + s.getTargetUserId())
                    .collect(Collectors.toSet());

            // target별 고유 평가자 집합(자동입력 전)
            Map<Long, Set<Long>> distinctEvaluatorsByTarget = new HashMap<>();
            for (TeamEvaluationScore s : existingScores) {
                distinctEvaluatorsByTarget
                        .computeIfAbsent(s.getTargetUserId(), k -> new HashSet<>())
                        .add(s.getEvaluatorUserId());
            }

            for (TeamMember evaluator : members) {
                Long evaluatorId = evaluator.getUser().getId();

                for (TeamMember target : members) {
                    Long targetId = target.getUser().getId();
                    if (Objects.equals(evaluatorId, targetId)) continue;

                    String key = evaluatorId + "-" + targetId;
                    if (existingPairs.contains(key)) continue;

                    // 1) 자동 채움: 모든 항목 3점
                    if (!items.isEmpty()) {
                        for (EvaluationItem item : items) {
                            scoreRepository.save(TeamEvaluationScore.builder()
                                    .evaluationId(eval.getId())
                                    .evaluatorUserId(evaluatorId)
                                    .targetUserId(targetId)
                                    .itemId(item.getId())
                                    .score(3)
                                    .createdAt(now)
                                    .build());
                        }
                    }

                    // 2) 온도 갱신(평균=3 → 0~100 스케일 30)
                    double targetScore100 = 30.0;

                    // === 기존 레코드가 있으면 그대로 쓰고, 없을 때만 새로 생성 ===
                    User targetUser = userRepository.findById(targetId).orElse(null);
                    if (targetUser == null) continue;

                    UserReputation rep = userReputationRepository.findByUserId(targetId)
                            .orElseGet(() -> userReputationRepository.save(
                                    UserReputation.builder().user(targetUser).build()
                            ));

                    // 이번 자동 입력 후 고유 평가자 수 n = 기존 + 1
                    Set<Long> set = distinctEvaluatorsByTarget.computeIfAbsent(targetId, k -> new HashSet<>());
                    if (set.add(evaluatorId)) {
                        // 추가되면 집합 크기가 증가
                    }
                    long n = set.size();

                    double alpha = 1.0 / (n + C);

                    double current = rep.getTemperature().doubleValue();
                    double updated = current + alpha * (targetScore100 - current);

                    if (updated < 0.0) updated = 0.0;
                    if (updated > 100.0) updated = 100.0;

                    rep.applyTemperature(BigDecimal.valueOf(updated).setScale(1, RoundingMode.HALF_UP));
                    userReputationRepository.save(rep);
                }
            }

            eval.setStatus(EvaluationStatus.CLOSED);
            evaluationRepository.save(eval);
        }
    }
}