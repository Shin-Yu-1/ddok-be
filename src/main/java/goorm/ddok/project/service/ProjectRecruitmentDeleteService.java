package goorm.ddok.project.service;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.project.domain.ProjectRecruitment;
import goorm.ddok.project.dto.request.ProjectDeleteRequest;
import goorm.ddok.project.repository.ProjectApplicationRepository;
import goorm.ddok.project.repository.ProjectParticipantRepository;
import goorm.ddok.project.repository.ProjectRecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectRecruitmentDeleteService {

    private final ProjectRecruitmentRepository recruitmentRepository;
    private final ProjectParticipantRepository projectParticipantRepository;
    private final ProjectApplicationRepository projectApplicationRepository;

    public void deleteProject(Long projectId, ProjectDeleteRequest req, CustomUserDetails me) {
        if (me == null || me.getUser() == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }

        ProjectRecruitment pr = recruitmentRepository.findById(projectId)
                .orElseThrow(() -> new GlobalException(ErrorCode.PROJECT_NOT_FOUND));

        // 리더만 삭제 가능
        if (!Objects.equals(pr.getUser().getId(), me.getUser().getId())) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }

        // 확인 문구 검증
        if (req == null || !"삭제합니다.".equals(req.getConfirmText())) {
            // ErrorCode 에 INVALID_CONFIRM_TEXT 를 추가(권장)
            throw new GlobalException(ErrorCode.INVALID_CONFIRM_TEXT);
        }

        // 1) 지원/참가자 선삭제 (position -> project 경유)
        projectApplicationRepository.deleteByPosition_ProjectRecruitment_Id(projectId);
        projectParticipantRepository.deleteByPosition_ProjectRecruitment_Id(projectId);

        // 2) 프로젝트 삭제 (positions/traits 는 cascade+orphanRemoval 로 함께 제거)
        recruitmentRepository.delete(pr);
    }
}