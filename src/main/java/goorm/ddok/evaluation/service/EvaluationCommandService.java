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

    public SaveScoresResponse saveScores(Long teamId, Long evaluationId, Long meUserId, SaveScoresRequest req) {
        TeamEvaluation eval = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new GlobalException(ErrorCode.EVALUATION_NOT_FOUND));

        if (!Objects.equals(eval.getTeam().getId(), teamId)) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }
        if (eval.getStatus() != EvaluationStatus.OPEN) {
            throw new GlobalException(ErrorCode.EVALUATION_CLOSED);
        }

        Long targetUserId = req.getTargetUserId();
        if (Objects.equals(meUserId, targetUserId)) {
            throw new GlobalException(ErrorCode.EVALUATION_SELF_NOT_ALLOWED);
        }

        // 둘 다 해당 팀의 멤버인지 확인
        teamMemberRepository.findByTeamIdAndUserId(teamId, meUserId)
                .orElseThrow(() -> new GlobalException(ErrorCode.FORBIDDEN));
        teamMemberRepository.findByTeamIdAndUserId(teamId, targetUserId)
                .orElseThrow(() -> new GlobalException(ErrorCode.EVALUATION_TARGET_NOT_MEMBER));

        // 이미 제출했는지(타겟 기준 한 번만)
        boolean already = scoreRepository.existsByEvaluationIdAndEvaluatorUserIdAndTargetUserId(
                evaluationId, meUserId, targetUserId);
        if (already) {
            throw new GlobalException(ErrorCode.EVALUATION_ALREADY_SUBMITTED);
        }

        // 점수 저장 + 범위 체크
        double sum = 0; int cnt = 0;
        for (var s : req.getScores()) {
            EvaluationItem item = itemRepository.findById(s.getItemId())
                    .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND));

            if (s.getScore() == null ||
                    s.getScore() < item.getScaleMin() ||
                    s.getScore() > item.getScaleMax()) {
                throw new GlobalException(ErrorCode.EVALUATION_ITEM_OUT_OF_RANGE);
            }

            scoreRepository.save(TeamEvaluationScore.builder()
                    .evaluationId(evaluationId)
                    .evaluatorUserId(meUserId)
                    .targetUserId(targetUserId)
                    .itemId(item.getId())
                    .score(s.getScore())
                    .createdAt(Instant.now())
                    .build());

            sum += s.getScore();
            cnt++;
        }

        // === 온도 반영(간단 예시): 평균 점수 -> 델타 -> 현재 온도에 적용 ===
        double avg = (cnt == 0) ? 0 : (sum / cnt);
        BigDecimal delta = BigDecimal.valueOf(avg - 3.0) // 3점을 기준점
                .setScale(1, RoundingMode.HALF_UP);

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new GlobalException(ErrorCode.USER_NOT_FOUND));

        UserReputation rep = userReputationRepository.findByUserId(targetUserId)
                .orElseGet(() -> userReputationRepository.save(
                        UserReputation.builder()
                                .user(target)
                                .temperature(BigDecimal.valueOf(36.5))
                                .build()
                ));

        BigDecimal next = rep.getTemperature().add(delta);
        if (next.compareTo(BigDecimal.ZERO) < 0) next = BigDecimal.ZERO;
        if (next.compareTo(BigDecimal.valueOf(100.0)) > 0) next = BigDecimal.valueOf(100.0);
        next = next.setScale(1, RoundingMode.HALF_UP);

        rep.applyTemperature(next);
        userReputationRepository.save(rep);

        return SaveScoresResponse.builder()
                .evaluationId(evaluationId)
                .targetUserId(targetUserId)
                .isEvaluated(true)
                .build();
    }
}