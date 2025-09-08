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
public class EvaluationScheduler {

    private final TeamEvaluationRepository evaluationRepository;
    private final TeamEvaluationScoreRepository scoreRepository;
    private final EvaluationItemRepository itemRepository;
    private final TeamMemberRepository teamMemberRepository;

    // 온도 반영에 필요
    private final UserRepository userRepository;
    private final UserReputationRepository userReputationRepository;

    // 완충값 c (초반 민감도 억제, 후반 안정화)
    private static final double C = 10.0;

    /**
     * 매일 자정(서울) 실행: 마감일이 지난 OPEN 라운드 자동 마감
     * - 아직 제출되지 않은 평가자→피평가자 조합은 각 항목 3점으로 자동 채움
     * - 자동 채움에도 동일한 온도 갱신 규칙 적용
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void closeExpiredEvaluations() {
        Instant now = Instant.now();

        // 1) 마감 대상 라운드
        List<TeamEvaluation> toClose =
                evaluationRepository.findAllByStatusAndClosesAtBefore(EvaluationStatus.OPEN, now);

        if (toClose.isEmpty()) return;

        // 평가 항목 전체 (빈배열 요청 시 기본 채워줄 때도 사용)
        List<EvaluationItem> items = itemRepository.findAll();
        if (items.isEmpty()) {
            // 항목이 없다면 마감만 수행
            for (TeamEvaluation eval : toClose) {
                eval.setStatus(EvaluationStatus.CLOSED);
                evaluationRepository.save(eval);
            }
            return;
        }

        for (TeamEvaluation eval : toClose) {
            Long teamId = eval.getTeam().getId();

            // 2) 팀 멤버
            List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
            if (members.isEmpty()) {
                eval.setStatus(EvaluationStatus.CLOSED);
                evaluationRepository.save(eval);
                continue;
            }

            // 3) 이미 존재하는 (평가자→피평가자) 조합
            List<TeamEvaluationScore> existingScores = scoreRepository.findByEvaluationId(eval.getId());
            Set<String> existingPairs = existingScores.stream()
                    .map(s -> s.getEvaluatorUserId() + "-" + s.getTargetUserId())
                    .collect(Collectors.toSet());

            // 3-1) 타겟별 "기존 고유 평가자" 집합 (이번 자동 입력 전)
            Map<Long, Set<Long>> distinctEvaluatorsByTarget = new HashMap<>();
            for (TeamEvaluationScore s : existingScores) {
                distinctEvaluatorsByTarget
                        .computeIfAbsent(s.getTargetUserId(), k -> new HashSet<>())
                        .add(s.getEvaluatorUserId());
            }

            // 4) 누락된 조합을 기본 3점으로 채우고, 온도 갱신
            for (TeamMember evaluator : members) {
                Long evaluatorId = evaluator.getUser().getId();

                for (TeamMember target : members) {
                    Long targetId = target.getUser().getId();
                    if (Objects.equals(evaluatorId, targetId)) continue;

                    String key = evaluatorId + "-" + targetId;
                    if (existingPairs.contains(key)) continue; // 이미 제출됨

                    // 4-1) 기본 점수(3점) 저장 (모든 항목)
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

                    // 4-2) 온도 갱신 (개별 평가자 1명이 타겟에게 제출했다고 보고 반영)
                    // 개별평가점수(avg) = 3.0  →  0~100 스케일 = 30.0
                    double targetScore100 = 30.0;

                    // 타겟 유저/레퓨 불러오기(없으면 생성)
                    User targetUser = userRepository.findById(targetId).orElse(null);
                    if (targetUser == null) continue; // 방어

                    UserReputation rep = userReputationRepository.findByUserId(targetId)
                            .orElseGet(() -> userReputationRepository.save(
                                    UserReputation.builder()
                                            .user(targetUser)
                                            .temperature(BigDecimal.valueOf(36.5))
                                            .build()
                            ));

                    // 이번 자동 입력 "후"의 고유 평가자 수 n = 기존 집합 크기 + 1
                    Set<Long> set = distinctEvaluatorsByTarget.computeIfAbsent(targetId, k -> new HashSet<>());
                    if (!set.contains(evaluatorId)) set.add(evaluatorId);
                    long n = set.size();

                    // α = 1 / (n + C)
                    double alpha = 1.0 / (n + C);

                    // 새온도 = 기존온도 + α × (targetScore100 - 기존온도)
                    double current = rep.getTemperature().doubleValue();
                    double updated = current + alpha * (targetScore100 - current);

                    if (updated < 0.0) updated = 0.0;
                    if (updated > 100.0) updated = 100.0;

                    BigDecimal next = BigDecimal.valueOf(updated).setScale(1, RoundingMode.HALF_UP);
                    rep.applyTemperature(next);
                    userReputationRepository.save(rep);
                }
            }

            // 5) 상태 CLOSE
            eval.setStatus(EvaluationStatus.CLOSED);
            evaluationRepository.save(eval);
        }
    }
}