package goorm.ddok.player.service;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.member.repository.UserRepository;
import goorm.ddok.player.dto.response.ProjectParticipationResponse;
import goorm.ddok.project.domain.ProjectParticipant;
import goorm.ddok.project.domain.ProjectRecruitment;
import goorm.ddok.project.domain.TeamStatus;
import goorm.ddok.project.repository.ProjectParticipantRepository;
import goorm.ddok.team.domain.Team;
import goorm.ddok.team.domain.TeamType;
import goorm.ddok.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileProjectQueryService {

    private final ProjectParticipantRepository projectParticipantRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public Page<ProjectParticipationResponse> getUserProjects(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new GlobalException(ErrorCode.USER_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<ProjectParticipant> participations =
                projectParticipantRepository.findByUser_IdAndDeletedAtIsNull(userId, pageable);

        return participations
                .map(p -> {
            ProjectRecruitment recruitment = p.getPosition().getProjectRecruitment();

            Long teamId = teamRepository
                    .findByRecruitmentIdAndType(recruitment.getId(), TeamType.PROJECT)
                    .map(Team::getId)
                    .orElseThrow(() -> new GlobalException(ErrorCode.TEAM_NOT_FOUND));

            String statusGroup = (recruitment.getTeamStatus() == TeamStatus.RECRUITING
                    || recruitment.getTeamStatus() == TeamStatus.ONGOING)
                    ? "ONGOING"
                    : "CLOSED";

            return ProjectParticipationResponse.from(p, teamId, statusGroup);
                }
        );
    }
}