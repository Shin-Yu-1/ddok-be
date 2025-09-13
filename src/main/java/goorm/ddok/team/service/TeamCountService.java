package goorm.ddok.team.service;

import goorm.ddok.project.repository.ProjectRecruitmentRepository;
import goorm.ddok.study.repository.StudyRecruitmentRepository;
import goorm.ddok.team.dto.response.TeamCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamCountService {

    private final ProjectRecruitmentRepository projectRecruitmentRepository;
    private final StudyRecruitmentRepository studyRecruitmentRepository;

    public TeamCountResponse getTeamCountResponse() {

        Long projectTeamCount = projectRecruitmentRepository.countByTeamStatusAndDeletedAtIsNull(goorm.ddok.project.domain.TeamStatus.ONGOING);
        Long studyTeamCount = studyRecruitmentRepository.countByTeamStatusAndDeletedAtIsNull(goorm.ddok.study.domain.TeamStatus.ONGOING);

        return TeamCountResponse.builder()
                .count(projectTeamCount+studyTeamCount).build();
    }
}
