package goorm.ddok.evaluation.service;

import goorm.ddok.badge.service.BadgeService;
import goorm.ddok.evaluation.domain.EvaluationStatus;
import goorm.ddok.evaluation.domain.TeamEvaluation;
import goorm.ddok.evaluation.domain.TeamEvaluationScore;
import goorm.ddok.evaluation.dto.EvaluationMemberItem;
import goorm.ddok.evaluation.dto.ScoreItem;
import goorm.ddok.evaluation.dto.SimpleUserDto;
import goorm.ddok.evaluation.dto.response.EvaluationModalResponse;
import goorm.ddok.evaluation.repository.TeamEvaluationRepository;
import goorm.ddok.evaluation.repository.TeamEvaluationScoreRepository;
import goorm.ddok.global.dto.AbandonBadgeDto;
import goorm.ddok.global.dto.BadgeDto;
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
    private final BadgeService badgeService; // ✅ 배지 서비스 주입

    public EvaluationModalResponse getModal(Long teamId, Long meUserId) {
        // 팀 확인
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND));

        // 최신 라운드
        TeamEvaluation eval = evaluationRepository.findTopByTeam_IdOrderByIdDesc(teamId)
                .orElseThrow(() -> new GlobalException(ErrorCode.EVALUATION_NOT_FOUND));

        // 팀원 조회 (자기 자신 제외)
        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId).stream()
                .filter(m -> !Objects.equals(m.getUser().getId(), meUserId))
                .toList();

        // 내가 준 점수들(타겟별 그룹)
        var given = scoreRepository.findByEvaluationIdAndEvaluatorUserId(eval.getId(), meUserId);
        Map<Long, List<TeamEvaluationScore>> givenByTarget =
                given.stream().collect(Collectors.groupingBy(TeamEvaluationScore::getTargetUserId));

        // 멤버 블록 생성
        List<EvaluationMemberItem> memberItems = members.stream()
                .map(m -> {
                    Long targetId = m.getUser().getId();
                    boolean evaluated = givenByTarget.containsKey(targetId);
                    boolean isMine = Objects.equals(targetId, meUserId);

                    // 내가 해당 멤버에게 준 점수 목록
                    List<ScoreItem> myScores = givenByTarget
                            .getOrDefault(targetId, List.of())
                            .stream()
                            .map(s -> ScoreItem.builder()
                                    .itemId(s.getItemId())
                                    .score(s.getScore())
                                    .build())
                            .collect(Collectors.toList());

                    // ✅ 배지 조회
                    // - 착한 배지들 중 가장 높은 티어 1개를 mainBadge로 선택
                    BadgeDto mainBadge = null;
                    var goodBadges = badgeService.getGoodBadges(m.getUser());
                    if (goodBadges != null && !goodBadges.isEmpty()) {
                        mainBadge = goodBadges.stream()
                                // enum 순서(브론즈 < 실버 < 골드)로 비교할 수 있도록 비교자 지정
                                .max(Comparator.comparing(b ->
                                        b.getTier() == null ? 0 :
                                                switch (b.getTier()) {
                                                    case bronze -> 1;
                                                    case silver -> 2;
                                                    case gold -> 3;
                                                }
                                ))
                                .orElse(null);
                    }
                    // - 탈주(나쁜) 배지
                    AbandonBadgeDto abandonBadge = badgeService.getAbandonBadge(m.getUser());

                    SimpleUserDto simple = SimpleUserDto.builder()
                            .userId(targetId)
                            .nickname(m.getUser().getNickname())
                            .profileImageUrl(m.getUser().getProfileImageUrl())
                            .role(m.getRole() == TeamMemberRole.LEADER ? "LEADER" : "MEMBER")
                            .mainBadge(mainBadge)           // ✅ 채움
                            .abandonBadge(abandonBadge)     // ✅ 채움
                            .build();

                    return EvaluationMemberItem.builder()
                            .memberId(m.getId())
                            .isMine(isMine)          // 자기 자신은 목록에서 제외했으므로 항상 false
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