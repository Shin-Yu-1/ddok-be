package goorm.ddok.project.dto.response;

import goorm.ddok.global.dto.PreferredAgesDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
@Schema(name = "ProjectEditPageResponse")
public record ProjectEditPageResponse(
        String title,
        String teamStatus,
        String bannerImageUrl,
        List<String> traits,
        Integer capacity,
        Long applicantCount,
        String mode,
        String address,
        PreferredAgesDto preferredAges,
        Integer expectedMonth,
        LocalDate startDate,
        String detail,
        String leaderPosition,
        List<PositionItem> positions
) {
    @Builder
    public record PositionItem(
            String positionName,
            Long applied,
            Long confirmed
    ) {}
}