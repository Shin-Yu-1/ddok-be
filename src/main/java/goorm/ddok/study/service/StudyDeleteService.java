package goorm.ddok.study.service;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.member.domain.User;
import goorm.ddok.study.domain.StudyRecruitment;
import goorm.ddok.study.dto.request.StudyDeleteRequest;
import goorm.ddok.study.repository.StudyParticipantRepository;
import goorm.ddok.study.repository.StudyRecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyDeleteService {

    private final StudyRecruitmentRepository studyRecruitmentRepository;
    private final StudyParticipantRepository studyParticipantRepository;

    public void delete(Long studyId, StudyDeleteRequest req, CustomUserDetails me) {
        // 인증 체크
        if (me == null || me.getUser() == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }
        User user = me.getUser();

        // 스터디 조회 (미삭제 + 존재)
        StudyRecruitment study = studyRecruitmentRepository.findByIdAndDeletedAtIsNull(studyId)
                .orElseThrow(() -> new GlobalException(ErrorCode.STUDY_NOT_FOUND));

        // 소유자(리더)만 삭제 가능
        if (!Objects.equals(study.getUser().getId(), user.getId())) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }

        // 요청 DTO 유효성은 Controller(@Valid)에서 걸리지만, 방어적으로 한 번 더 확인해도 OK
        if (!"삭제합니다.".equals(req.getConfirmText())) {
            throw new GlobalException(ErrorCode.INVALID_INPUT);
        }

        // soft delete (참가자 먼저, 스터디 나중)
        Instant now = Instant.now();
        studyParticipantRepository.softDeleteByStudyId(studyId, now);

        int affected = studyRecruitmentRepository.softDeleteById(studyId, now);
        if (affected == 0) {
            // 이미 삭제되었거나 동시성 이슈 등
            throw new GlobalException(ErrorCode.STUDY_NOT_FOUND);
        }
    }
}