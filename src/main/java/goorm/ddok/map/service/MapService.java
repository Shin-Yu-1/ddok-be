package goorm.ddok.map.service;

import goorm.ddok.global.dto.LocationDto;
import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.map.dto.response.ProjectMapItemResponse;
import goorm.ddok.map.dto.response.StudyMapItemResponse;
import goorm.ddok.project.domain.TeamStatus;
import goorm.ddok.project.repository.ProjectRecruitmentRepository;
import goorm.ddok.study.repository.StudyRecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MapService {

    private final ProjectRecruitmentRepository projectRecruitmentRepository;
    private final StudyRecruitmentRepository studyRecruitmentRepository;

    @Transactional(readOnly = true)
    public List<ProjectMapItemResponse> getProjectsInBounds(
            BigDecimal swLat, BigDecimal swLng,
            BigDecimal neLat, BigDecimal neLng,
            BigDecimal centerLat, BigDecimal centerLng
    ) {
        validateBounds(swLat, swLng, neLat, neLng);

        var rows = projectRecruitmentRepository.findAllInBounds(swLat, neLat, swLng, neLng);

        if (rows == null || rows.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        var items = rows.stream()
                .map(r -> ProjectMapItemResponse.builder()
                        .category("project")
                        .projectId(r.getId())
                        .title(r.getTitle())
                        .teamStatus(normalizeTeamStatus(r.getTeamStatus()))
                        .location(LocationDto.builder()
                                .address(composeRoadAddress(
                                        r.getRegion1depthName(),
                                        r.getRegion2depthName(),
                                        r.getRegion3depthName(),
                                        r.getRoadName(),
                                        r.getMainBuildingNo(),
                                        r.getSubBuildingNo()))
                                .region1depthName(r.getRegion1depthName())
                                .region2depthName(r.getRegion2depthName())
                                .region3depthName(r.getRegion3depthName())
                                .roadName(r.getRoadName())
                                .mainBuildingNo(r.getMainBuildingNo())
                                .subBuildingNo(r.getSubBuildingNo())
                                .zoneNo(r.getZoneNo())
                                .latitude(r.getLatitude())
                                .longitude(r.getLongitude())
                                .build())
                        .build())
                .toList();

        // 중심점이 있으면 거리순 정렬
        if (centerLat != null && centerLng != null && !items.isEmpty()) {
            final double cLat = centerLat.doubleValue();
            final double cLng = centerLng.doubleValue();

            items = items.stream()
                    .sorted(Comparator.comparingDouble(p ->
                            haversineKm(
                                    cLat, cLng,
                                    p.getLocation().getLatitude().doubleValue(),
                                    p.getLocation().getLongitude().doubleValue()
                            )))
                    .toList();
        }

        return items;
    }

    @Transactional(readOnly = true)
    public List<StudyMapItemResponse> getStudiesInBounds(
            BigDecimal swLat, BigDecimal swLng,
            BigDecimal neLat, BigDecimal neLng,
            BigDecimal centerLat, BigDecimal centerLng
    ) {
        validateBounds(swLat, swLng, neLat, neLng);

        var rows = studyRecruitmentRepository.findAllInBounds(swLat, neLat, swLng, neLng);

        if (rows == null || rows.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        var items = rows.stream()
                .map(r -> StudyMapItemResponse.builder()
                        .category("study")
                        .studyId(r.getId())
                        .title(r.getTitle())
                        .teamStatus(normalizeTeamStatus(r.getTeamStatus()))
                        .location(LocationDto.builder()
                                .address(composeRoadAddress(
                                        r.getRegion1depthName(),
                                        r.getRegion2depthName(),
                                        r.getRegion3depthName(),
                                        r.getRoadName(),
                                        r.getMainBuildingNo(),
                                        r.getSubBuildingNo()))
                                .region1depthName(r.getRegion1depthName())
                                .region2depthName(r.getRegion2depthName())
                                .region3depthName(r.getRegion3depthName())
                                .roadName(r.getRoadName())
                                .mainBuildingNo(r.getMainBuildingNo())
                                .subBuildingNo(r.getSubBuildingNo())
                                .zoneNo(r.getZoneNo())
                                .latitude(r.getLatitude())
                                .longitude(r.getLongitude())
                                .build())
                        .build())
                .toList();

        // 중심점이 있으면 거리순 정렬
        if (centerLat != null && centerLng != null && !items.isEmpty()) {
            final double cLat = centerLat.doubleValue();
            final double cLng = centerLng.doubleValue();

            items = items.stream()
                    .sorted(Comparator.comparingDouble(p ->
                            haversineKm(
                                    cLat, cLng,
                                    p.getLocation().getLatitude().doubleValue(),
                                    p.getLocation().getLongitude().doubleValue()
                            )))
                    .toList();
        }

        return items;
    }

    private void validateBounds(BigDecimal swLat, BigDecimal swLng, BigDecimal neLat, BigDecimal neLng) {
        if (swLat == null || swLng == null || neLat == null || neLng == null) {
            throw new GlobalException(ErrorCode.REQUIRED_PARAMETER_MISSING);
        }
        if (swLat.compareTo(neLat) > 0 || swLng.compareTo(neLng) > 0) {
            throw new GlobalException(ErrorCode.INVALID_MAP_BOUNDS);
        }
    }

    private String composeRoadAddress(String r1, String r2, String r3,
                                      String road, String mainNo, String subNo) {
        if (road == null && r1 == null && r2 == null) return "-";
        var sb = new StringBuilder();
        if (r1 != null && !r1.isBlank()) sb.append(r1).append(" ");
        if (r2 != null && !r2.isBlank()) sb.append(r2).append(" ");
        if (r3 != null && !r3.isBlank()) sb.append(r3).append(" ");
        if (road != null && !road.isBlank()) sb.append(road).append(" ");

        String main = mainNo == null ? "" : mainNo.trim();
        String sub  = subNo == null ? "" : subNo.trim();
        if (!main.isEmpty() && !sub.isEmpty()) sb.append(main).append("-").append(sub);
        else if (!main.isEmpty()) sb.append(main);

        return sb.toString().trim();
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return R * c;
    }

    private String normalizeTeamStatus(TeamStatus status) {
        if (status == null) return "ONGOING";
        return (status == TeamStatus.CLOSED) ? "ONGOING" : status.name();
    }
}
