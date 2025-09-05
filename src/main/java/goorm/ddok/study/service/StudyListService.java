package goorm.ddok.study.service;

import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.study.domain.StudyMode;
import goorm.ddok.study.domain.StudyRecruitment;
import goorm.ddok.study.dto.response.StudyListResponse;
import goorm.ddok.study.repository.StudyRecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyListService {

    private final StudyRecruitmentRepository studyRecruitmentRepository;

    @Transactional(readOnly = true)
    public Page<StudyListResponse> getStudies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StudyRecruitment> rows = studyRecruitmentRepository.findByDeletedAtIsNull(pageable);
        return rows.map(this::toResponse);
    }

    private StudyListResponse toResponse(StudyRecruitment s) {
        String address = (s.getMode() == StudyMode.online)
                ? "online"
                : shortAddress(s.getRegion1depthName(), s.getRegion2depthName());

        return StudyListResponse.builder()
                .studyId(s.getId())
                .title(s.getTitle())
                .teamStatus(String.valueOf(s.getTeamStatus()))
                .bannerImageUrl(s.getBannerImageUrl())
                .capacity(s.getCapacity())
                .mode(s.getMode())
                .address(address)
                .studyType(s.getStudyType())
                .preferredAges(PreferredAgesDto.builder()
                        .ageMin(s.getAgeMin())
                        .ageMax(s.getAgeMax())
                        .build())
                .expectedMonth(s.getExpectedMonths())
                .startDate(s.getStartDate())
                .build();
    }

    private String shortAddress(String region1, String region2) {
        String r1 = region1 == null ? "" : region1.trim();
        String r2 = region2 == null ? "" : region2.trim();
        if (r1.isEmpty() && r2.isEmpty()) return "-";
        return (r1 + " " + r2).trim();
    }

}
