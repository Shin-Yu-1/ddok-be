package goorm.ddok.project.dto.request;

import goorm.ddok.global.dto.LocationDto;
import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.project.domain.ProjectMode;
import goorm.ddok.project.domain.TeamStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(name = "ProjectRecruitmentUpdateRequest", description = "프로젝트 수정 요청 DTO")
public class ProjectRecruitmentUpdateRequest {

    @NotBlank
    @Size(min = 2, max = 30)
    @Schema(example = "구지라지")
    private String title;

    @NotNull
    @Schema(example = "2025-09-15", description = "프로젝트 시작 예정일(오늘 또는 미래)")
    private LocalDate expectedStart;

    @NotNull
    @Min(1) @Max(64)
    @Schema(example = "3")
    private Integer expectedMonth;

    @NotNull
    @Schema(example = "OFFLINE", allowableValues = {"ONLINE","OFFLINE"})
    private ProjectMode mode;

    @Schema(description = "OFFLINE 필수. 카카오 road_address 매핑값 사용")
    private LocationDto location;

    @Schema(description = "없으면 null")
    private PreferredAgesDto preferredAges;

    @NotNull
    @Min(1) @Max(7)
    @Schema(example = "6")
    private Integer capacity;

    @Schema(example = "[\"정리의 신\",\"실행력 갓\",\"내향인\"]")
    private List<String> traits;

    @NotEmpty(message = "모집 포지션은 1개 이상이어야 합니다.")
    @Schema(example = "[\"PM\",\"UI/UX\",\"백엔드\"]")
    private List<String> positions;

    @NotBlank(message = "리더 포지션은 필수입니다.")
    @Schema(example = "PM")
    private String leaderPosition;

    @NotBlank
    @Size(min = 10, max = 2000)
    @Schema(example = "멋진 웹을 함께 만들 사람을 찾습니다.")
    private String detail;

    @Schema(description = "파일 미첨부 시 변경/유지 위해 사용 가능",
            example = "https://cdn.example.com/images/default.png")
    private String bannerImageUrl;

    @NotNull
    @Schema(example = "RECRUITING", allowableValues = {"RECRUITING","ONGOING","CLOSED"})
    private TeamStatus teamStatus;
}