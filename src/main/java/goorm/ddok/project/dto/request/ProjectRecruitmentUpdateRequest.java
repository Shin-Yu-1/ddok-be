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

    @NotBlank @Size(min = 2, max = 30)
    private String title;

    @NotNull
    private LocalDate expectedStart;

    @NotNull @Min(1) @Max(64)
    private Integer expectedMonth;

    @NotNull
    private ProjectMode mode;

    /** OFFLINE일 때만 사용 */
    private LocationDto location;

    /** 없으면 null */
    private PreferredAgesDto preferredAges;

    @NotNull @Min(1) @Max(7)
    private Integer capacity;

    /** 성향 목록 (없으면 빈 목록/유지) */
    private List<String> traits;

    /** 모집 포지션(최소 1개) */
    @NotEmpty(message = "모집 포지션은 최소 1개 이상이어야 합니다.")
    private List<String> positions;

    /** 리더 포지션명(선택) – 정책에 따라 사용/검증 */
    @NotBlank(message = "리더 포지션은 필수 입력값입니다.")
    private String leaderPosition;

    @NotBlank @Size(min = 10, max = 2000)
    private String detail;

    /** 배너 이미지 URL (파일 미첨부 시 변경/유지 용) */
    private String bannerImageUrl;

    @NotNull
    private TeamStatus teamStatus;
}