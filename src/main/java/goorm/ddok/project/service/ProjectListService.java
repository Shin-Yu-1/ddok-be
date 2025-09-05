package goorm.ddok.project.service;

import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.project.domain.ProjectMode;
import goorm.ddok.project.domain.ProjectRecruitment;
import goorm.ddok.project.domain.ProjectRecruitmentPosition;
import goorm.ddok.project.domain.TeamStatus;
import goorm.ddok.project.dto.response.ProjectListResponse;
import goorm.ddok.project.repository.ProjectRecruitmentRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    public Page<ProjectListResponse> searchProjects(
            String keyword, String statusCsv, String positionLike, Integer capacity, String mode,
            Integer ageMin, Integer ageMax, Integer expectedMonth, LocalDate startDate,
            int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var spec = buildSpec(keyword, statusCsv, positionLike, capacity, mode, ageMin, ageMax, expectedMonth, startDate);

        Page<ProjectRecruitment> rows = projectRecruitmentRepository.findAll(spec, pageable);
        return rows.map(this::toResponse);
    }

    private Specification<ProjectRecruitment> buildSpec(
            String keyword, String statusCsv, String positionLike, Integer capacity, String mode,
            Integer ageMin, Integer ageMax, Integer expectedMonth, LocalDate startDate
    ) {
        return (root, query, cb) -> {
            // count 쿼리에는 fetch 금지 — 여기서는 @EntityGraph 사용하므로 fetch 하지 않음
            if (Objects.requireNonNull(query).getResultType() != Long.class && query.getResultType() != long.class) {
                query.distinct(true);
            }

            List<Predicate> preds = new ArrayList<>();
            preds.add(cb.isNull(root.get("deletedAt")));

            if (keyword != null && !keyword.isBlank()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";

                var titleLike = cb.like(cb.lower(root.get("title")), kw);

                var r1Like   = cb.like(cb.lower(cb.coalesce(root.get("region1depthName"), "")), kw);
                var r2Like   = cb.like(cb.lower(cb.coalesce(root.get("region2depthName"), "")), kw);
                var r3Like   = cb.like(cb.lower(cb.coalesce(root.get("region3depthName"), "")), kw);
                var roadLike = cb.like(cb.lower(cb.coalesce(root.get("roadName"), "")), kw);
                var mainLike = cb.like(cb.lower(cb.coalesce(root.get("mainBuildingNo"), "")), kw);
                var subLike  = cb.like(cb.lower(cb.coalesce(root.get("subBuildingNo"), "")), kw);

                var r1r2Concat = cb.like(
                        cb.lower(cb.concat(
                                cb.concat(cb.coalesce(root.get("region1depthName"), ""), " "),
                                cb.coalesce(root.get("region2depthName"), "")
                        )),
                        kw
                );

                preds.add(cb.or(
                        titleLike,
                        r1Like, r2Like, r3Like, roadLike, mainLike, subLike,
                        r1r2Concat
                ));
            }

            // 상태(csv) → EnumSet
            Set<TeamStatus> statuses = parseStatuses(statusCsv);
            if (!statuses.isEmpty()) {
                preds.add(root.get("teamStatus").in(statuses));
            }

            // 포지션 LIKE
            if (positionLike != null && !positionLike.isBlank()) {
                String pv = "%" + positionLike.trim().toLowerCase() + "%";
                Join<ProjectRecruitment, ProjectRecruitmentPosition> posJoin = root.join("positions", JoinType.LEFT);
                preds.add(cb.like(cb.lower(posJoin.get("positionName")), pv));
            }

            // 정원 >=
            if (capacity != null && capacity > 0) {
                preds.add(cb.greaterThanOrEqualTo(root.get("capacity"), capacity));
            }

            // 모드
            ProjectMode parsedMode = parseMode(mode);
            if (parsedMode != null) {
                preds.add(cb.equal(root.get("projectMode"), parsedMode));
            }

            // 나이: 각각 있을 때 개별 적용
            if (ageMin != null && ageMin > 0) {
                preds.add(cb.greaterThanOrEqualTo(root.get("ageMin"), ageMin));
            }
            if (ageMax != null && ageMax > 0) {
                preds.add(cb.lessThanOrEqualTo(root.get("ageMax"), ageMax));
            }

            // 예상 개월 수 ==
            if (expectedMonth != null && expectedMonth > 0) {
                preds.add(cb.equal(root.get("expectedMonths"), expectedMonth));
            }

            // 시작일 >=
            if (startDate != null) {
                preds.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDate));
            }

            return cb.and(preds.toArray(new Predicate[0]));
        };
    }

    private Set<TeamStatus> parseStatuses(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptySet();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try { return TeamStatus.valueOf(s); }
                    catch (Exception e) { return null; }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(TeamStatus.class)));
    }

    private ProjectMode parseMode(String mode) {
        if (mode == null) return null;
        String m = mode.trim().toLowerCase();
        if (m.equals("online"))  return ProjectMode.online;
        if (m.equals("offline")) return ProjectMode.offline;
        return null;
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
