package goorm.ddok.project.service;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.project.domain.ProjectParticipant;
import goorm.ddok.project.domain.ProjectRecruitment;
import goorm.ddok.project.dto.request.ProjectDeleteRequest;
import goorm.ddok.project.repository.ProjectParticipantRepository;
import goorm.ddok.project.repository.ProjectRecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectRecruitmentSoftDeleteService {

    private final ProjectRecruitmentRepository recruitmentRepository;
    private final ProjectParticipantRepository participantRepository;

    public void softDelete(Long projectId, ProjectDeleteRequest req, CustomUserDetails me) {
        if (me == null || me.getUser() == null) throw new GlobalException(ErrorCode.UNAUTHORIZED);
        if (!"삭제합니다.".equals(req.getConfirmText())) {
            throw new GlobalException(ErrorCode.INVALID_CONFIRM_TEXT); // 없으면 새로 추가(아래 참고)
        }

        // 소프트 삭제된 건 제외하고 조회
        ProjectRecruitment pr = recruitmentRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new GlobalException(ErrorCode.PROJECT_NOT_FOUND));

        // 리더만 삭제 가능
        if (!Objects.equals(pr.getUser().getId(), me.getUser().getId())) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }

        Instant now = Instant.now();

        // 1) 프로젝트 소프트 삭제
        pr = pr.toBuilder()
                .deletedAt(now)
                .build();
        recruitmentRepository.save(pr);

        // 2) 참가자(Participant)들도 소프트 삭제 (필요 시)
        List<ProjectParticipant> participants = participantRepository.findByPosition_ProjectRecruitment_Id(projectId);
        boolean hasAny = false;
        for (ProjectParticipant pp : participants) {
            if (pp.getDeletedAt() == null) {
                hasAny = true;
                ProjectParticipant soft = pp.toBuilder()
                        .deletedAt(now)
                        .build();
                participantRepository.save(soft);
            }
        }

    }
}