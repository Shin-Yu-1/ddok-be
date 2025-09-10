package goorm.ddok.evaluation.service;

import goorm.ddok.evaluation.domain.*;
import goorm.ddok.evaluation.dto.EvaluationMemberItem;
import goorm.ddok.evaluation.dto.ScoreItem;
import goorm.ddok.evaluation.dto.SimpleUserDto;
import goorm.ddok.evaluation.dto.response.EvaluationModalResponse;
import goorm.ddok.evaluation.repository.*;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.team.domain.Team;
import goorm.ddok.team.domain.TeamMember;
import goorm.ddok.team.domain.TeamMemberRole;
import goorm.ddok.team.repository.TeamMemberRepository;
import goorm.ddok.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvaluationQueryService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamEvaluationRepository evaluationRepository;
    private final TeamEvaluationScoreRepository scoreRepository;

    public EvaluationModalResponse getModal(Long teamId, Long meUserId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND));

        TeamEvaluation eval = evaluationRepository.findTopByTeam_IdOrderByIdDesc(teamId)
                .orElseThrow(() -> new GlobalException(ErrorCode.EVALUATION_NOT_FOUND));

        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);

        // 내가 준 점수들(타겟별 그룹)
        var given = scoreRepository.findByEvaluationIdAndEvaluatorUserId(eval.getId(), meUserId);
        Map<Long, List<TeamEvaluationScore>> givenByTarget =
                given.stream().collect(Collectors.groupingBy(TeamEvaluationScore::getTargetUserId));

        // === 나를 제외한 멤버만 노출 ===
        List<EvaluationMemberItem> memberItems = members.stream()
                .filter(m -> !Objects.equals(m.getUser().getId(), meUserId)) // 본인 제외
                .map(m -> {
                    Long targetId = m.getUser().getId();
                    boolean evaluated = givenByTarget.containsKey(targetId);
                    boolean isMine = Objects.equals(targetId, meUserId);

                    List<ScoreItem> myScores = givenByTarget
                            .getOrDefault(targetId, List.of())
                            .stream()
                            .map(s -> ScoreItem.builder()
                                    .itemId(s.getItemId())
                                    .score(s.getScore())
                                    .build())
                            .toList();

                    SimpleUserDto simple = SimpleUserDto.builder()
                            .userId(targetId)
                            .nickname(m.getUser().getNickname())
                            .profileImageUrl(m.getUser().getProfileImageUrl())
                            .role(m.getRole() == TeamMemberRole.LEADER ? "LEADER" : "MEMBER")
                            .mainBadge(null)
                            .abandonBadge(null)
                            .build();

                    return EvaluationMemberItem.builder()
                            .memberId(m.getId())
                            .isMine(isMine)               // 본인 제외했으므로 항상 false
                            .user(simple)
                            .isEvaluated(evaluated)
                            .scores(myScores)
                            .build();
                })
                .toList();

        return EvaluationModalResponse.builder()
                .teamId(team.getId())
                .teamType(team.getType().name())
                .evaluationId(eval.getId())
                .status(eval.getStatus().name())
                .items(memberItems)
                .build();
    }
}