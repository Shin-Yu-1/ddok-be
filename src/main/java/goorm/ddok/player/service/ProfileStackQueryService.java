package goorm.ddok.player;

import goorm.ddok.member.repository.UserTechStackRepository;
import goorm.ddok.player.dto.response.TechStackResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileStackQueryService {

    private final UserTechStackRepository userTechStackRepository;

    /**
     * 사용자 기술스택 상세 조회
     */
    public Page<TechStackResponse> getUserTechStacks(Long userId, Pageable pageable) {
        return userTechStackRepository.findByUserId(userId, pageable)
                .map(uts -> TechStackResponse.builder()
                        .techStack(uts.getTechStack().getName())
                        .build());
    }
}
