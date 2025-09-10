package goorm.ddok.evaluation.service;

import goorm.ddok.badge.service.BadgeService;
import goorm.ddok.evaluation.domain.*;
import goorm.ddok.evaluation.dto.EvaluationItemDto;
import goorm.ddok.evaluation.dto.EvaluationMemberItem;
import goorm.ddok.evaluation.dto.ScoreItem;
import goorm.ddok.evaluation.dto.SimpleUserDto;
import goorm.ddok.evaluation.dto.response.*;
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
    private final BadgeService badgeService;

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


        // 모달 멤버 블록
        List<EvaluationMemberItem> memberItems = members.stream()
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
                            .collect(Collectors.toList());

                    SimpleUserDto simple = SimpleUserDto.builder()
                            .userId(targetId)
                            .nickname(m.getUser().getNickname())
                            .profileImageUrl(m.getUser().getProfileImageUrl())
                            .role(m.getRole() == TeamMemberRole.LEADER ? "LEADER" : "MEMBER")
                            .mainBadge(badgeService.getRepresentativeGoodBadge(m.getUser()))
                            .abandonBadge(badgeService.getAbandonBadge(m.getUser()))
                            .build();

                    return EvaluationMemberItem.builder()
                            .memberId(m.getId())
                            .isMine(isMine)
                            .user(simple)
                            .isEvaluated(evaluated)
                            .scores(myScores)
                            .build();
                })
                .collect(Collectors.toList());

        return EvaluationModalResponse.builder()
                .teamId(team.getId())
                .teamType(team.getType().name())
                .evaluationId(eval.getId())
                .status(eval.getStatus().name())
                .items(memberItems)
                .build();
    }
}