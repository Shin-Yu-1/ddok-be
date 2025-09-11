package goorm.ddok.team.service;

import goorm.ddok.evaluation.domain.EvaluationStatus;
import goorm.ddok.evaluation.domain.TeamEvaluation;
import goorm.ddok.evaluation.repository.TeamEvaluationRepository;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.project.domain.ProjectRecruitment;
import goorm.ddok.project.repository.ProjectRecruitmentRepository;
import goorm.ddok.study.domain.StudyRecruitment;
import goorm.ddok.study.repository.StudyRecruitmentRepository;
import goorm.ddok.team.domain.Team;
import goorm.ddok.team.domain.TeamType;
import goorm.ddok.team.dto.response.TeamCloseResponse;
import goorm.ddok.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamLifecycleService {

    private final TeamRepository teamRepository;
    private final ProjectRecruitmentRepository projectRecruitmentRepository;
    private final StudyRecruitmentRepository studyRecruitmentRepository;
    private final TeamEvaluationRepository teamEvaluationRepository;

    public TeamCloseResponse closeTeamAndOpenEvaluation(Long teamId, CustomUserDetails me) {
        if (me == null || me.getUser() == null) throw new GlobalException(ErrorCode.UNAUTHORIZED);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new GlobalException(ErrorCode.TEAM_NOT_FOUND));

        // 리더만 종료 가능: Team.user (리더 FK)
        if (!team.getUser().getId().equals(me.getUser().getId())) {
            throw new GlobalException(ErrorCode.FORBIDDEN_LEADER_ONLY);
        }



        // 1) 타입 판별 → 해당 모집 teamStatus 를 CLOSED 로
        if (team.getType() == TeamType.PROJECT) {
            closeProjectRecruitment(team.getRecruitmentId());
        } else if (team.getType() == TeamType.STUDY) {
            closeStudyRecruitment(team.getRecruitmentId());
        } else {
            throw new GlobalException(ErrorCode.NOT_SUPPORT_CATEGORY);
        }

        // 2) 열린 평가 라운드가 없으면 새 OPEN 라운드 생성 (closesAt 은 엔티티 @PrePersist 로 +7일)
        boolean hasOpen = teamEvaluationRepository.existsByTeam_IdAndStatus(team.getId(), EvaluationStatus.OPEN);
        if (!hasOpen) {
            TeamEvaluation ev = TeamEvaluation.builder()
                    .team(team)
                    .status(EvaluationStatus.OPEN)
                    .openedAt(Instant.now())
                    .closesAt(Instant.now().plus(7, ChronoUnit.DAYS)) // 엔티티에도 기본 +7일이 있으나, 명시적으로 한 번 더 보장
                    .build();
            teamEvaluationRepository.save(ev);
        }

        return TeamCloseResponse.builder()
                .teamId(team.getId())
                .status("CLOSED")
                .build();
    }

    private void closeProjectRecruitment(Long recruitmentId) {
        if (recruitmentId == null) throw new GlobalException(ErrorCode.PROJECT_NOT_FOUND);
        ProjectRecruitment pr = projectRecruitmentRepository.findById(recruitmentId)
                .orElseThrow(() -> new GlobalException(ErrorCode.PROJECT_NOT_FOUND));

        if (pr.getTeamStatus() == goorm.ddok.project.domain.TeamStatus.CLOSED) {
            // ✅ 이미 종료된 상태면 에러 던지기
            throw new GlobalException(ErrorCode.ALREADY_CLOSED);
        }

        pr = pr.toBuilder().teamStatus(goorm.ddok.project.domain.TeamStatus.CLOSED).build();
        projectRecruitmentRepository.save(pr);
    }

    private void closeStudyRecruitment(Long recruitmentId) {
        if (recruitmentId == null) throw new GlobalException(ErrorCode.STUDY_NOT_FOUND);
        StudyRecruitment sr = studyRecruitmentRepository.findById(recruitmentId)
                .orElseThrow(() -> new GlobalException(ErrorCode.STUDY_NOT_FOUND));

        if (sr.getTeamStatus() == goorm.ddok.study.domain.TeamStatus.CLOSED) {
            // ✅ 이미 종료된 상태면 에러 던지기
            throw new GlobalException(ErrorCode.ALREADY_CLOSED);
        }

        sr = sr.toBuilder().teamStatus(goorm.ddok.study.domain.TeamStatus.CLOSED).build();
        studyRecruitmentRepository.save(sr);
    }
}