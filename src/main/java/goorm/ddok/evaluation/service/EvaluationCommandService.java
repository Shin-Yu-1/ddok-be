package goorm.ddok.evaluation.service;

import goorm.ddok.evaluation.domain.*;
import goorm.ddok.evaluation.dto.request.SaveScoresRequest;
import goorm.ddok.evaluation.dto.response.SaveScoresResponse;
import goorm.ddok.evaluation.repository.*;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.member.domain.User;
import goorm.ddok.member.repository.UserRepository;
import goorm.ddok.reputation.domain.UserReputation;
import goorm.ddok.reputation.repository.UserReputationRepository;
import goorm.ddok.team.domain.TeamMember;
import goorm.ddok.team.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluationCommandService {

    private final TeamEvaluationRepository evaluationRepository;
    private final TeamEvaluationScoreRepository scoreRepository;
    private final EvaluationItemRepository itemRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final UserReputationRepository userReputationRepository;
    /** 온도 누적 가중치 완충값 */
    private static final double REPUTATION_DAMPING_C = 10.0;

    public SaveScoresResponse saveScores(Long teamId, Long evaluationId, Long meUserId, SaveScoresRequest req) {
        TeamEvaluation eval = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new GlobalException(ErrorCode.EVALUATION_NOT_FOUND));

        // === 자동 마감 처리: closesAt이 지났으면 즉시 CLOSED로 전환 ===
        if (eval.getStatus() == EvaluationStatus.OPEN
                && eval.getClosesAt() != null
                && eval.getClosesAt().isBefore(Instant.now())) {
            eval.setStatus(EvaluationStatus.CLOSED);
            evaluationRepository.save(eval);
        }

        if (!Objects.equals(eval.getTeam().getId(), teamId)) throw new GlobalException(ErrorCode.FORBIDDEN);
        if (eval.getStatus() != EvaluationStatus.OPEN) throw new GlobalException(ErrorCode.EVALUATION_CLOSED);

        Long targetUserId = req.getTargetUserId();
        if (Objects.equals(meUserId, targetUserId)) {
            throw new GlobalException(ErrorCode.EVALUATION_SELF_NOT_ALLOWED);
        }

        // 팀 멤버 검증
        teamMemberRepository.findByTeamIdAndUserId(teamId, meUserId)
                .orElseThrow(() -> new GlobalException(ErrorCode.FORBIDDEN));
        teamMemberRepository.findByTeamIdAndUserId(teamId, targetUserId)
                .orElseThrow(() -> new GlobalException(ErrorCode.EVALUATION_TARGET_NOT_MEMBER));

        // 중복 제출 방지(타겟 기준 1회)
        boolean already = scoreRepository.existsByEvaluationIdAndEvaluatorUserIdAndTargetUserId(
                evaluationId, meUserId, targetUserId);
        if (already) throw new GlobalException(ErrorCode.EVALUATION_ALREADY_SUBMITTED);

        // === 점수 목록 준비: 비어 있으면 모든 항목을 3점으로 자동 채우기 ===
        var incoming = (req.getScores() == null || req.getScores().isEmpty())
                ? itemRepository.findAll().stream()
                .map(it -> new SaveScoresRequest.Score(it.getId(), 3))
                .toList()
                : req.getScores();

        double sum = 0.0;
        int cnt = 0;

        for (var s : incoming) {
            EvaluationItem item = itemRepository.findById(s.getItemId())
                    .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND));

            Integer v = s.getScore();
            if (v == null || v < item.getScaleMin() || v > item.getScaleMax()) {
                throw new GlobalException(ErrorCode.EVALUATION_ITEM_OUT_OF_RANGE);
            }

            scoreRepository.save(TeamEvaluationScore.builder()
                    .evaluationId(evaluationId)
                    .evaluatorUserId(meUserId)
                    .targetUserId(targetUserId)
                    .itemId(item.getId())
                    .score(v)
                    .createdAt(Instant.now())
                    .build());

            sum += v;
            cnt++;
        }

        // === 온도 갱신 (점진적 누적 가중치 방식) ===
        double avg = (cnt == 0) ? 0.0 : (sum / cnt);   // 항목 평균(1~5)
        double targetScore100 = avg * 10.0;            // 0~100 스케일

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        UserReputation rep = userReputationRepository.findByUserId(targetUserId)
                .orElseGet(() -> userReputationRepository.save(
                        UserReputation.builder()
                                .user(target)
                                .temperature(BigDecimal.valueOf(36.5))
                                .build()
                ));

        long n = scoreRepository.countDistinctEvaluatorsByTargetUserId(targetUserId); // 이번 포함 고유 평가자 수
        double alpha = 1.0 / (n + REPUTATION_DAMPING_C);

        double current = rep.getTemperature().doubleValue();
        double updated = current + alpha * (targetScore100 - current);
        if (updated < 0.0) updated = 0.0;
        if (updated > 100.0) updated = 100.0;

        rep.applyTemperature(BigDecimal.valueOf(updated).setScale(1, RoundingMode.HALF_UP));
        userReputationRepository.save(rep);

        return SaveScoresResponse.builder()
                .evaluationId(evaluationId)
                .targetUserId(targetUserId)
                .isEvaluated(true)
                .build();
    }
}