package goorm.ddok.player.service;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.member.repository.UserRepository;
import goorm.ddok.player.dto.response.ProjectParticipationResponse;
import goorm.ddok.project.domain.ProjectParticipant;
import goorm.ddok.project.repository.ProjectParticipantRepository;
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

    public Page<ProjectParticipationResponse> getUserProjects(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new GlobalException(ErrorCode.USER_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<ProjectParticipant> participations =
                projectParticipantRepository.findByUser_IdAndDeletedAtIsNull(userId, pageable);

        return participations.map(ProjectParticipationResponse::from);
    }
}