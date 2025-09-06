package goorm.ddok.study.dto.response;

import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.study.domain.StudyMode;
import goorm.ddok.study.domain.StudyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@Schema(name = "StudyListResponse", description = "스터디 리스트 아이템")
public class StudyListResponse {

    @Schema(description = "스터디 ID", example = "1")
    private final Long studyId;

    @Schema(description = "제목", example = "구지라지 프로젝트")
    private final String title;

    @Schema(description = "팀 상태", example = "RECRUITING")
    private final String teamStatus;

    @Schema(description = "배너 이미지 URL", example = "https://cdn.example.com/images/default.png")
    private final String bannerImageUrl;

    @Schema(description = "정원", example = "4")
    private final Integer capacity;

    @Schema(description = "모드(online|offline)", example = "offline")
    private final StudyMode mode;

    @Schema(description = "주소(오프라인만)", example = "서울 강남구")
    private final String address;

    @Schema(description = "스터디 유형", example = "자소서")
    private final StudyType studyType;

    @Schema(description = "선호 연령대")
    private final PreferredAgesDto preferredAges;

    @Schema(description = "예상 개월 수", example = "3")
    private final Integer expectedMonth;

    @Schema(description = "시작일", example = "2025-09-10")
    private final LocalDate startDate;
}
