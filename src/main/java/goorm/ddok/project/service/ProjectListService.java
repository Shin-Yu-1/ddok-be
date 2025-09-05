package goorm.ddok.project.service;

import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.project.domain.ProjectMode;
import goorm.ddok.project.domain.ProjectRecruitment;
import goorm.ddok.project.domain.ProjectRecruitmentPosition;
import goorm.ddok.project.dto.response.ProjectListResponse;
import goorm.ddok.project.repository.ProjectRecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectListService {

    private final ProjectRecruitmentRepository projectRecruitmentRepository;

    @Transactional(readOnly = true)
    public Page<ProjectListResponse> getProjects(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ProjectRecruitment> rows = projectRecruitmentRepository.findByDeletedAtIsNull(pageable);

        return rows.map(this::toResponse);
    }

    private ProjectListResponse toResponse(ProjectRecruitment p) {
        List<String> positions = p.getPositions() == null ? List.of()
                : p.getPositions().stream()
                .map(ProjectRecruitmentPosition::getPositionName)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        String address = (p.getProjectMode() == ProjectMode.online)
                ? "online"
                : shortAddress(p.getRegion1depthName(), p.getRegion2depthName());

        return ProjectListResponse.builder()
                .projectId(p.getId())
                .title(p.getTitle())
                .teamStatus(String.valueOf(p.getTeamStatus()))
                .bannerImageUrl(p.getBannerImageUrl())
                .positions(positions)
                .capacity(p.getCapacity())
                .mode(p.getProjectMode())
                .address(address)
                .preferredAges(PreferredAgesDto.builder()
                        .ageMin(p.getAgeMin())
                        .ageMax(p.getAgeMax())
                        .build())
                .expectedMonth(p.getExpectedMonths())
                .startDate(p.getStartDate())
                .build();
    }

    private String shortAddress(String region1, String region2) {
        String r1 = region1 == null ? "" : region1.trim();
        String r2 = region2 == null ? "" : region2.trim();
        if (r1.isEmpty() && r2.isEmpty()) return "-";
        return (r1 + " " + r2).trim();
    }
}
