package goorm.ddok.team.service;

import goorm.ddok.chat.dto.response.PaginationResponse;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.project.domain.ProjectApplication;
import goorm.ddok.project.repository.ProjectApplicationRepository;
import goorm.ddok.study.domain.StudyApplication;
import goorm.ddok.study.repository.StudyApplicationRepository;
import goorm.ddok.team.domain.Team;
import goorm.ddok.team.domain.TeamType;
import goorm.ddok.team.dto.response.TeamApplicantResponse;
import goorm.ddok.team.dto.response.TeamApplicantsResponse;
import goorm.ddok.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamApplicantQueryService {

    private final StudyApplicationRepository studyApplicationRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final TeamRepository teamRepository;

    public TeamApplicantsResponse getApplicants(
            Long teamId,
            CustomUserDetails user,
            int page, int size) {

        Long currentUserId = user.getId();

        // teamId 로 teamType 조회 → STUDY / PROJECT 분기
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new GlobalException(ErrorCode.TEAM_NOT_FOUND));
        TeamType teamType = team.getType();
        boolean IsLeader = team.getUser().getId().equals(currentUserId);
        Long recruitmentId = team.getRecruitmentId();

        // STUDY
        if (teamType == TeamType.STUDY) {
            Page<StudyApplication> apps =
                    studyApplicationRepository.findByStudyRecruitment_IdAndStatus(
                            recruitmentId,
                            goorm.ddok.study.domain.ApplicationStatus.PENDING,
                            PageRequest.of(page, size)
                    );

            List<TeamApplicantResponse> items = apps.stream()
                    .map(app -> TeamApplicantResponse.fromStudy(app, currentUserId))
                    .toList();

            return TeamApplicantsResponse.builder()
                    .pagination(PaginationResponse.of(apps))
                    .teamId(team.getId())
                    .recruitmentId(recruitmentId)
                    .IsLeader(IsLeader)
                    .teamType(teamType)
                    .items(items)
                    .build();
        }

        // PROJECT
        if (teamType == TeamType.PROJECT) {
            Page<ProjectApplication> apps =
                    projectApplicationRepository.findByPosition_ProjectRecruitment_IdAndStatus(
                            recruitmentId,
                            goorm.ddok.project.domain.ApplicationStatus.PENDING,
                            PageRequest.of(page, size)
                    );

            List<TeamApplicantResponse> items = apps.stream()
                    .map(app -> TeamApplicantResponse.fromProject(app, currentUserId))
                    .toList();

            return TeamApplicantsResponse.builder()
                    .pagination(PaginationResponse.of(apps))
                    .teamId(team.getId())
                    .recruitmentId(recruitmentId)
                    .IsLeader(IsLeader)
                    .teamType(teamType)
                    .items(items)
                    .build();
        }

        throw new GlobalException(ErrorCode.INVALID_TEAM_TYPE);
    }
}