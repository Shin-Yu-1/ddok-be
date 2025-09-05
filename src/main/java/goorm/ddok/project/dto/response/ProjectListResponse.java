package goorm.ddok.project.dto.response;

import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.project.domain.ProjectMode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@Schema(name = "ProjectListResponse", description = "프로젝트 리스트 아이템 응답")
public class ProjectListResponse {

    @Schema(description = "프로젝트 ID", example = "1")
    private final Long projectId;

    @Schema(description = "제목", example = "구지라지 프로젝트")
    private final String title;

    @Schema(description = "팀 상태", example = "RECRUITING")
    private final String teamStatus;

    @Schema(description = "배너 이미지 URL", example = "https://cdn.example.com/images/default.png")
    private final String bannerImageUrl;

    @Schema(description = "모집 포지션 목록", example = "[\"백엔드\",\"프론트엔드\"]")
    private final List<String> positions;

    @Schema(description = "모집 정원", example = "4")
    private final Integer capacity;

    @Schema(description = "진행 방식", example = "offline")
    private final ProjectMode mode;

    @Schema(description = "주소(오프라인만: \"서울 강남구\" 형식, 온라인은 \"online\")", example = "서울 마포구")
    private final String address;

    @Schema(description = "선호 연령대", example = "{\"ageMin\":20,\"ageMax\":30}")
    private final PreferredAgesDto preferredAges;

    @Schema(description = "예상 개월 수", example = "3")
    private final Integer expectedMonth;

    @Schema(description = "시작일", example = "2025-09-10")
    private final LocalDate startDate;
}
