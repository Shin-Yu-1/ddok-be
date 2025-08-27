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

        // 소유자만 조회 허용(정책에 맞게 수정 가능)
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

        String address = pr.getProjectMode() == ProjectMode.ONLINE
                ? "ONLINE"
                : Optional.ofNullable(pr.getRoadName()).orElse("-");

        return ProjectEditPageResponse.builder()
                .title(pr.getTitle())
                .teamStatus(pr.getTeamStatus().name())
                .bannerImageUrl(pr.getBannerImageUrl())
                .traits(pr.getTraits().stream().map(ProjectRecruitmentTrait::getTraitName).toList())
                .capacity(pr.getCapacity())
                .applicantCount(applicantCount)
                .mode(pr.getProjectMode().name().toLowerCase())
                .address(address)
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

        // 리더 권한
        if (!Objects.equals(pr.getUser().getId(), me.getUser().getId())) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }

        // 시작일 미래 검증 (오늘 이후만 허용)
        if (req.getExpectedStart() != null) {
            java.time.LocalDate today = java.time.LocalDate.now();
            if (!req.getExpectedStart().isAfter(today)) {
                throw new GlobalException(ErrorCode.INVALID_START_DATE);
            }
        }

        // 연령/모드/위치 등 기존 검증 ...
        if (req.getPreferredAges() != null &&
                req.getPreferredAges().getAgeMin() > req.getPreferredAges().getAgeMax()) {
            throw new GlobalException(ErrorCode.INVALID_AGE_RANGE);
        }
        if (req.getPositions() == null || req.getPositions().isEmpty()) {
            throw new GlobalException(ErrorCode.INVALID_POSITIONS);
        }
        if (req.getMode() == ProjectMode.OFFLINE) {
            LocationDto loc = req.getLocation();
            if (loc == null || loc.getLatitude() == null || loc.getLongitude() == null) {
                throw new GlobalException(ErrorCode.INVALID_LOCATION);
            }
        }

        // ✅ [추가] 리더 포지션 검증: 요청 포지션 목록에 반드시 포함되어야 함
        String leaderPosName = (req.getLeaderPosition() == null) ? "" : req.getLeaderPosition().trim();
        java.util.List<String> desiredPositions =
                req.getPositions().stream().filter(Objects::nonNull)
                        .map(String::trim).filter(s -> !s.isBlank())
                        .distinct().collect(Collectors.toList());

        if (leaderPosName.isBlank() || !desiredPositions.contains(leaderPosName)) {
            throw new GlobalException(ErrorCode.INVALID_LEADER_POSITION);
        }

        // 배너 URL 선택
        String bannerUrl = resolveBannerUrl(bannerImage, req.getBannerImageUrl(), pr.getBannerImageUrl(), req.getTitle());

        // 위치 업데이트
        boolean offline = req.getMode() == ProjectMode.OFFLINE;
        pr = offline ? updateOfflineLocation(pr, req.getLocation()) : clearLocation(pr);

        // 기본 필드 업데이트
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

        // 포지션/성향 머지
        mergeTraits(pr, req.getTraits());
        mergePositions(pr, desiredPositions);

        // ✅ [추가] 리더 Participant의 포지션을 요청의 leaderPosition으로 동기화
        // (mergePositions 이후 pr.getPositions() 안에 반드시 존재)
        ProjectRecruitmentPosition leaderPosEntity = pr.getPositions().stream()
                .filter(p -> p.getPositionName().equals(leaderPosName))
                .findFirst()
                .orElseThrow(() -> new GlobalException(ErrorCode.INVALID_LEADER_POSITION));

        participantRepository
                .findFirstByPosition_ProjectRecruitment_IdAndRoleAndDeletedAtIsNull(projectId, ParticipantRole.LEADER)
                .ifPresent(pp -> {
                    // 이미 같지 않으면 변경
                    if (pp.getPosition() == null || !Objects.equals(pp.getPosition().getId(), leaderPosEntity.getId())) {
                        pp.changePosition(leaderPosEntity);
                        participantRepository.save(pp);
                    }
                });

        ProjectRecruitment saved = recruitmentRepository.save(pr);
        return buildUpdateResult(saved, me);
    }

    /* ---------- merge helpers ---------- */

    private void mergeTraits(ProjectRecruitment pr, List<String> incoming) {
        List<String> desired = (incoming == null) ? List.of() : incoming.stream()
                .filter(Objects::nonNull).map(String::trim).filter(s -> !s.isBlank()).distinct().toList();

        // 현재 맵
        Map<String, ProjectRecruitmentTrait> current = pr.getTraits().stream()
                .collect(Collectors.toMap(ProjectRecruitmentTrait::getTraitName, t -> t, (a,b)->a));

        // 추가
        for (String name : desired) {
            if (!current.containsKey(name)) {
                pr.getTraits().add(ProjectRecruitmentTrait.builder()
                        .projectRecruitment(pr)
                        .traitName(name)
                        .build());
            }
        }
        // 제거 (원본에만 있고 신규에는 없는 것)
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
                // 참조가 있으면 그대로 둠
            }
        }
    }

    /* ---------- location helpers ---------- */

    private ProjectRecruitment updateOfflineLocation(ProjectRecruitment pr, LocationDto loc) {
        BigDecimal lat = loc.getLatitude();
        BigDecimal lng = loc.getLongitude();
        String addr = loc.getAddress();

        return pr.toBuilder()
                .region1depthName("서울특별시")   // TODO: 실제 파서로 교체
                .region2depthName("강남구")
                .region3depthName("테헤란로")
                .roadName(addr)
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
        if (requestUrl != null && !requestUrl.isBlank()) return requestUrl.trim();
        if (currentUrl != null && !currentUrl.isBlank()) return currentUrl;
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
                            .mainPosition(null)      // 필요 시 사용자 포지션 연계
                            .temperature(null)       // 필요 시 연계
                            .decidedPosition(pp.getPosition() != null ? pp.getPosition().getPositionName() : null)
                            .isMine(mine)
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
                            .isMine(meId != null && Objects.equals(meId, u.getId()))
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
                        .isApplied(myApplied)
                        .isApproved(myApproved)
                        .isAvailable(pr.getTeamStatus() == TeamStatus.RECRUITING)
                        .build())
                .toList();

        String address = pr.getProjectMode() == ProjectMode.ONLINE
                ? "ONLINE"
                : Optional.ofNullable(pr.getRoadName()).orElse("-");

        boolean isMine = meId != null && Objects.equals(pr.getUser().getId(), meId);

        return ProjectUpdateResultResponse.builder()
                .projectId(projectId)
                .isMine(isMine)
                .title(pr.getTitle())
                .teamStatus(pr.getTeamStatus().name())
                .bannerImageUrl(pr.getBannerImageUrl())
                .traits(pr.getTraits().stream().map(ProjectRecruitmentTrait::getTraitName).toList())
                .capacity(pr.getCapacity())
                .applicantCount(applicantCount)
                .mode(pr.getProjectMode().name().toLowerCase())
                .address(address)
                .preferredAges(new PreferredAgesDto(pr.getAgeMin(), pr.getAgeMax()))
                .expectedMonth(pr.getExpectedMonths())
                .startDate(pr.getStartDate())
                .detail(pr.getContentMd())
                .positions(positionItems)
                .leader(resolveLeader(projectId, me))
                .participants(resolveParticipants(projectId, me))
                .build();
    }
}