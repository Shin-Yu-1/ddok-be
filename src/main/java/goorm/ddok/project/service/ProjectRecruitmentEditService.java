package goorm.ddok.project.service;

import goorm.ddok.global.dto.LocationDto;
import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.global.file.FileService;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.global.util.BannerImageService;
import goorm.ddok.member.domain.User;
import goorm.ddok.project.domain.*;
import goorm.ddok.project.dto.request.ProjectRecruitmentUpdateRequest;
import goorm.ddok.project.dto.response.ProjectEditPageResponse;
import goorm.ddok.project.dto.response.ProjectUpdateResultResponse;
import goorm.ddok.project.repository.ProjectApplicationRepository;
import goorm.ddok.project.repository.ProjectParticipantRepository;
import goorm.ddok.project.repository.ProjectRecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectRecruitmentEditService {

    private final ProjectRecruitmentRepository recruitmentRepository;
    private final ProjectParticipantRepository participantRepository;
    private final ProjectApplicationRepository applicationRepository;
    private final FileService fileService;
    private final BannerImageService bannerImageService;

    /* =========================
     *  수정 페이지 조회
     * ========================= */
    @Transactional(readOnly = true)
    public ProjectEditPageResponse getEditPage(Long projectId, CustomUserDetails me) {
        if (me == null || me.getUser() == null) throw new GlobalException(ErrorCode.UNAUTHORIZED);

        ProjectRecruitment pr = recruitmentRepository.findById(projectId)
                .orElseThrow(() -> new GlobalException(ErrorCode.PROJECT_NOT_FOUND));

        // 리더만 조회 가능
        if (!Objects.equals(pr.getUser().getId(), me.getUser().getId())) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }

        long applicantCount = applicationRepository.countAllByProjectId(projectId);

        Map<String, Long> applied = applicationRepository.countAppliedByPosition(projectId)
                .stream().collect(Collectors.toMap(
                        ProjectApplicationRepository.PositionCountProjection::getPositionName,
                        ProjectApplicationRepository.PositionCountProjection::getCnt
                ));

        Map<String, Long> confirmed = applicationRepository.countApprovedByPosition(projectId)
                .stream().collect(Collectors.toMap(
                        ProjectApplicationRepository.PositionCountProjection::getPositionName,
                        ProjectApplicationRepository.PositionCountProjection::getCnt
                ));

        List<ProjectEditPageResponse.PositionItem> positions = pr.getPositions().stream()
                .map(p -> new ProjectEditPageResponse.PositionItem(
                        p.getPositionName(),
                        applied.getOrDefault(p.getPositionName(), 0L),
                        confirmed.getOrDefault(p.getPositionName(), 0L)
                ))
                .toList();

        return ProjectEditPageResponse.builder()
                .title(pr.getTitle())
                .teamStatus(pr.getTeamStatus().name())
                .bannerImageUrl(pr.getBannerImageUrl())
                .traits(pr.getTraits().stream().map(ProjectRecruitmentTrait::getTraitName).toList())
                .capacity(pr.getCapacity())
                .applicantCount(applicantCount)
                .mode(pr.getProjectMode().name().toLowerCase())
                .address(formatAddress(pr))       // 합쳐서 반환
                .preferredAges(new PreferredAgesDto(pr.getAgeMin(), pr.getAgeMax()))
                .expectedMonth(pr.getExpectedMonths())
                .startDate(pr.getStartDate())
                .detail(pr.getContentMd())
                .leaderPosition(resolveLeaderPositionName(projectId))
                .positions(positions)
                .build();
    }

    /* =========================
     *  수정 저장 (업데이트 방식)
     * ========================= */
    public ProjectUpdateResultResponse updateProject(Long projectId,
                                                     ProjectRecruitmentUpdateRequest req,
                                                     MultipartFile bannerImage,
                                                     CustomUserDetails me) {
        if (me == null || me.getUser() == null) throw new GlobalException(ErrorCode.UNAUTHORIZED);

        ProjectRecruitment pr = recruitmentRepository.findById(projectId)
                .orElseThrow(() -> new GlobalException(ErrorCode.PROJECT_NOT_FOUND));

        // 권한(리더만)
        if (!Objects.equals(pr.getUser().getId(), me.getUser().getId())) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }

        // ========= 비즈니스 검증 =========

        // 과거 시작일 금지 (오늘 포함 OK)
        LocalDate today = LocalDate.now();
        if (req.getExpectedStart() == null || req.getExpectedStart().isBefore(today)) {
            throw new GlobalException(ErrorCode.INVALID_START_DATE);
        }

        // 연령대 범위
        if (req.getPreferredAges() != null &&
                req.getPreferredAges().getAgeMin() > req.getPreferredAges().getAgeMax()) {
            throw new GlobalException(ErrorCode.INVALID_AGE_RANGE);
        }

        // 포지션 최소 1개
        if (req.getPositions() == null || req.getPositions().isEmpty()) {
            throw new GlobalException(ErrorCode.INVALID_POSITIONS);
        }

        // 리더 포지션 포함 규칙
        if (req.getLeaderPosition() == null ||
                req.getPositions().stream().noneMatch(p -> p.equals(req.getLeaderPosition()))) {
            throw new GlobalException(ErrorCode.INVALID_LEADER_POSITION);
        }

        // 오프라인이면 위치 필수
        if (req.getMode() == ProjectMode.OFFLINE) {
            LocationDto loc = req.getLocation();
            if (loc == null || loc.getLatitude() == null || loc.getLongitude() == null
                    || isBlank(loc.getRegion1depthName())
                    || isBlank(loc.getRegion2depthName())
                    || isBlank(loc.getRegion3depthName())
                    || isBlank(loc.getRoadName())) {
                throw new GlobalException(ErrorCode.INVALID_LOCATION);
            }
        }

        // ========= 배너 선택(파일 > 요청URL > 기존 > 기본생성) =========
        String bannerUrl = resolveBannerUrl(bannerImage, req.getBannerImageUrl(), pr.getBannerImageUrl(), req.getTitle());

        // ========= 위치 반영 =========
        boolean offline = (req.getMode() == ProjectMode.OFFLINE);
        if (offline) {
            pr = applyOfflineLocation(pr, req.getLocation());
        } else {
            pr = clearLocation(pr);
        }

        // ========= 기본 필드 업데이트 =========
        int ageMin = (req.getPreferredAges() != null) ? req.getPreferredAges().getAgeMin() : pr.getAgeMin();
        int ageMax = (req.getPreferredAges() != null) ? req.getPreferredAges().getAgeMax() : pr.getAgeMax();

        pr = pr.toBuilder()
                .title(req.getTitle())
                .teamStatus(req.getTeamStatus())
                .startDate(req.getExpectedStart())
                .expectedMonths(req.getExpectedMonth())
                .projectMode(req.getMode())
                .bannerImageUrl(bannerUrl)
                .contentMd(req.getDetail())
                .capacity(req.getCapacity())
                .ageMin(ageMin)
                .ageMax(ageMax)
                .build();

        // ========= 포지션/성향 머지 =========
        mergeTraits(pr, req.getTraits());
        mergePositions(pr, req.getPositions());

        ProjectRecruitment saved = recruitmentRepository.save(pr);

        // ========= 응답 =========
        return buildUpdateResult(saved, me);
    }

    /* ---------- merge helpers ---------- */

    private void mergeTraits(ProjectRecruitment pr, List<String> incoming) {
        List<String> desired = (incoming == null) ? List.of() : incoming.stream()
                .filter(Objects::nonNull).map(String::trim).filter(s -> !s.isBlank()).distinct().toList();

        Map<String, ProjectRecruitmentTrait> curr = pr.getTraits().stream()
                .collect(Collectors.toMap(ProjectRecruitmentTrait::getTraitName, t -> t, (a,b)->a));

        // 추가
        for (String name : desired) {
            if (!curr.containsKey(name)) {
                pr.getTraits().add(ProjectRecruitmentTrait.builder()
                        .projectRecruitment(pr)
                        .traitName(name)
                        .build());
            }
        }
        // 제거
        pr.getTraits().removeIf(t -> !desired.contains(t.getTraitName()));
    }

    /**
     * 포지션은 ‘이름 기준’으로 머지한다.
     * - 신규는 추가
     * - 원본에만 있는 이름은 삭제 시도하되, 지원/참가자 참조가 있으면 **유지**
     */
    private void mergePositions(ProjectRecruitment pr, List<String> incoming) {
        List<String> desired = incoming.stream()
                .filter(Objects::nonNull).map(String::trim).filter(s -> !s.isBlank()).distinct().toList();

        Map<String, ProjectRecruitmentPosition> byName = pr.getPositions().stream()
                .collect(Collectors.toMap(ProjectRecruitmentPosition::getPositionName, p -> p, (a,b)->a));

        // 추가
        for (String name : desired) {
            if (!byName.containsKey(name)) {
                pr.getPositions().add(ProjectRecruitmentPosition.builder()
                        .projectRecruitment(pr)
                        .positionName(name)
                        .build());
            }
        }

        // 삭제(참조 없는 경우에만)
        Iterator<ProjectRecruitmentPosition> it = pr.getPositions().iterator();
        while (it.hasNext()) {
            ProjectRecruitmentPosition pos = it.next();
            if (!desired.contains(pos.getPositionName())) {
                long refByParticipants = participantRepository.countByPosition_IdAndDeletedAtIsNull(pos.getId());
                long refByApplications = applicationRepository.countByPosition_Id(pos.getId());
                if (refByParticipants == 0 && refByApplications == 0) {
                    it.remove();
                }
            }
        }
    }

    /* ---------- location helpers ---------- */

    /** 카카오 road_address 매핑 필드 그대로 반영 */
    private ProjectRecruitment applyOfflineLocation(ProjectRecruitment pr, LocationDto loc) {
        BigDecimal lat = loc.getLatitude();
        BigDecimal lng = loc.getLongitude();

        return pr.toBuilder()
                .region1depthName(safeTrim(loc.getRegion1depthName()))
                .region2depthName(safeTrim(loc.getRegion2depthName()))
                .region3depthName(safeTrim(loc.getRegion3depthName()))
                .roadName(safeTrim(loc.getRoadName()))
                .latitude(lat)
                .longitude(lng)
                .build();
    }

    private ProjectRecruitment clearLocation(ProjectRecruitment pr) {
        return pr.toBuilder()
                .region1depthName(null)
                .region2depthName(null)
                .region3depthName(null)
                .roadName(null)
                .latitude(null)
                .longitude(null)
                .build();
    }

    /* ---------- banner helper ---------- */
    private String resolveBannerUrl(MultipartFile file, String requestUrl, String currentUrl, String titleForDefault) {
        if (file != null && !file.isEmpty()) {
            try {
                return fileService.upload(file);
            } catch (IOException e) {
                throw new GlobalException(ErrorCode.BANNER_UPLOAD_FAILED);
            }
        }
        if (!isBlank(requestUrl)) return requestUrl.trim();
        if (!isBlank(currentUrl)) return currentUrl;
        return bannerImageService.generateBannerImageUrl(
                (titleForDefault == null ? "PROJECT" : titleForDefault), "PROJECT", 1200, 600
        );
    }

    /* ---------- response builders ---------- */

    private String resolveLeaderPositionName(Long projectId) {
        return participantRepository
                .findFirstByPosition_ProjectRecruitment_IdAndRoleAndDeletedAtIsNull(projectId, ParticipantRole.LEADER)
                .map(pp -> pp.getPosition() != null ? pp.getPosition().getPositionName() : null)
                .orElse(null);
    }

    private ProjectUpdateResultResponse.LeaderBlock resolveLeader(Long projectId, CustomUserDetails me) {
        return participantRepository
                .findFirstByPosition_ProjectRecruitment_IdAndRoleAndDeletedAtIsNull(projectId, ParticipantRole.LEADER)
                .map(pp -> {
                    User u = pp.getUser();
                    boolean mine = me != null && me.getUser() != null && Objects.equals(u.getId(), me.getUser().getId());
                    return ProjectUpdateResultResponse.LeaderBlock.builder()
                            .userId(u.getId())
                            .nickname(u.getNickname())
                            .profileImageUrl(u.getProfileImageUrl())
                            .mainPosition(null)
                            .temperature(null)
                            .decidedPosition(pp.getPosition() != null ? pp.getPosition().getPositionName() : null)
                            .IsMine(mine)
                            .chatRoomId(null)
                            .dmRequestPending(false)
                            .build();
                })
                .orElse(null);
    }

    private List<ProjectUpdateResultResponse.ParticipantBlock> resolveParticipants(Long projectId, CustomUserDetails me) {
        Long meId = (me != null && me.getUser() != null) ? me.getUser().getId() : null;

        return participantRepository.findByPosition_ProjectRecruitment_IdAndDeletedAtIsNull(projectId).stream()
                .filter(pp -> pp.getRole() == ParticipantRole.MEMBER) // 리더 제외
                .map(pp -> {
                    User u = pp.getUser();
                    return ProjectUpdateResultResponse.ParticipantBlock.builder()
                            .userId(u.getId())
                            .nickname(u.getNickname())
                            .profileImageUrl(u.getProfileImageUrl())
                            .mainPosition(null)
                            .temperature(null)
                            .decidedPosition(pp.getPosition() != null ? pp.getPosition().getPositionName() : null)
                            .IsMine(meId != null && Objects.equals(meId, u.getId()))
                            .chatRoomId(null)
                            .dmRequestPending(false)
                            .build();
                })
                .toList();
    }

    private ProjectUpdateResultResponse buildUpdateResult(ProjectRecruitment pr, CustomUserDetails me) {
        Long projectId = pr.getId();

        long applicantCount = applicationRepository.countAllByProjectId(projectId);

        Map<String, Long> applied = applicationRepository.countAppliedByPosition(projectId).stream()
                .collect(Collectors.toMap(
                        ProjectApplicationRepository.PositionCountProjection::getPositionName,
                        ProjectApplicationRepository.PositionCountProjection::getCnt
                ));
        Map<String, Long> confirmed = applicationRepository.countApprovedByPosition(projectId).stream()
                .collect(Collectors.toMap(
                        ProjectApplicationRepository.PositionCountProjection::getPositionName,
                        ProjectApplicationRepository.PositionCountProjection::getCnt
                ));

        Long meId = (me != null && me.getUser() != null) ? me.getUser().getId() : null;
        boolean myApplied = (meId != null) && applicationRepository
                .existsByUser_IdAndPosition_ProjectRecruitment_Id(meId, projectId);
        boolean myApproved = (meId != null) && applicationRepository
                .existsByUser_IdAndPosition_ProjectRecruitment_IdAndStatus(meId, projectId, ApplicationStatus.APPROVED);

        List<ProjectUpdateResultResponse.PositionItem> positionItems = pr.getPositions().stream()
                .map(p -> ProjectUpdateResultResponse.PositionItem.builder()
                        .position(p.getPositionName())
                        .applied(applied.getOrDefault(p.getPositionName(), 0L))
                        .confirmed(confirmed.getOrDefault(p.getPositionName(), 0L))
                        .IsApplied(myApplied)
                        .IsApproved(myApproved)
                        .IsAvailable(pr.getTeamStatus() == TeamStatus.RECRUITING)
                        .build())
                .toList();

        boolean isMine = meId != null && Objects.equals(pr.getUser().getId(), meId);

        return ProjectUpdateResultResponse.builder()
                .projectId(projectId)
                .IsMine(isMine)
                .title(pr.getTitle())
                .teamStatus(pr.getTeamStatus().name())
                .bannerImageUrl(pr.getBannerImageUrl())
                .traits(pr.getTraits().stream().map(ProjectRecruitmentTrait::getTraitName).toList())
                .capacity(pr.getCapacity())
                .applicantCount(applicantCount)
                .mode(pr.getProjectMode().name().toLowerCase())
                .address(formatAddress(pr)) // 합친 주소
                .preferredAges(new PreferredAgesDto(pr.getAgeMin(), pr.getAgeMax()))
                .expectedMonth(pr.getExpectedMonths())
                .startDate(pr.getStartDate())
                .detail(pr.getContentMd())
                .positions(positionItems)
                .leader(resolveLeader(projectId, me))
                .participants(resolveParticipants(projectId, me))
                .build();
    }

    /* ---------- 주소 포맷터 ---------- */

    private String formatAddress(ProjectRecruitment pr) {
        if (pr.getProjectMode() == ProjectMode.ONLINE) return "ONLINE";
        String merged = joinSpace(
                pr.getRegion1depthName(),
                pr.getRegion2depthName(),
                pr.getRegion3depthName(),
                pr.getRoadName()
        );
        return isBlank(merged) ? "-" : merged;
    }

    private static String joinSpace(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!isBlank(p)) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(p.trim());
            }
        }
        return sb.toString();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}