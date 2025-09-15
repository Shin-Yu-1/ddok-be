package goorm.ddok.evaluation.batch;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EvaluationCloseTasklet {

    private final TeamEvaluationRepository evaluationRepository;
    private final TeamEvaluationScoreRepository scoreRepository;
    private final EvaluationItemRepository itemRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final UserReputationRepository userReputationRepository;

    /** 완충 상수 */
    private static final double C = 10.0;

    /**
     * 배치 트랜잭션 경계: Step 단위 트랜잭션.
     * 평가 라운드별로 saveAll을 최대한 활용해 INSERT 부하를 줄임.
     */
    @Transactional
    public void run(Instant now) {
        List<TeamEvaluation> toClose =
                evaluationRepository.findAllByStatusAndClosesAtBefore(EvaluationStatus.OPEN, now);

        if (toClose.isEmpty()) {
            log.info("No OPEN evaluations to close.");
            return;
        }

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

            // 새로 삽입할 점수 버퍼
            List<TeamEvaluationScore> toInsertScores = new ArrayList<>();

            // 온도 갱신을 위한 캐시 (targetUserId -> rep 엔티티)
            Map<Long, UserReputation> repCache = new HashMap<>();

            for (TeamMember evaluator : members) {
                Long evaluatorId = evaluator.getUser().getId();

                for (TeamMember target : members) {
                    Long targetId = target.getUser().getId();
                    if (Objects.equals(evaluatorId, targetId)) continue;

                    String key = evaluatorId + "-" + targetId;
                    if (existingPairs.contains(key)) continue;

                    // 1) 자동 채움: 모든 항목 3점 -> bulk insert 대비 버퍼에 쌓기
                    if (!items.isEmpty()) {
                        for (EvaluationItem item : items) {
                            toInsertScores.add(TeamEvaluationScore.builder()
                                    .evaluationId(eval.getId())
                                    .evaluatorUserId(evaluatorId)
                                    .targetUserId(targetId)
                                    .itemId(item.getId())
                                    .score(3)
                                    .createdAt(now)
                                    .build());
                        }
                    }

                    // 2) 온도 갱신 (평균 3점 → 100점 스케일 30)
                    double targetScore100 = 30.0;

                    UserReputation rep = repCache.computeIfAbsent(targetId, tid -> {
                        User targetUser = userRepository.findById(tid).orElse(null);
                        if (targetUser == null) return null;
                        return userReputationRepository.findByUserId(tid)
                                .orElseGet(() -> userReputationRepository.save(
                                        UserReputation.builder().user(targetUser).build()
                                ));
                    });
                    if (rep == null) continue;

                    // 이번 자동 입력 후 고유 평가자 수 n = 기존 + 1
                    Set<Long> set = distinctEvaluatorsByTarget.computeIfAbsent(targetId, k -> new HashSet<>());
                    boolean added = set.add(evaluatorId);
                    if (!added) {
                        // 이론상 없지만, 방어적으로 continue
                        continue;
                    }
                    long n = set.size();

                    double alpha = 1.0 / (n + C);
                    double current = rep.getTemperature().doubleValue();
                    double updated = current + alpha * (targetScore100 - current);
                    if (updated < 0.0) updated = 0.0;
                    if (updated > 100.0) updated = 100.0;

                    rep.applyTemperature(BigDecimal.valueOf(updated).setScale(1, RoundingMode.HALF_UP));
                }
            }

            // 벌크 저장
            if (!toInsertScores.isEmpty()) {
                scoreRepository.saveAll(toInsertScores);
            }
            if (!repCache.isEmpty()) {
                userReputationRepository.saveAll(repCache.values());
            }

            eval.setStatus(EvaluationStatus.CLOSED);
            evaluationRepository.save(eval);

            log.info("CLOSED evaluationId={} insertedScores={} updatedReps={}",
                    eval.getId(), toInsertScores.size(), repCache.size());
        }
    }
}
