package goorm.ddok.player.dto.response;

import goorm.ddok.study.domain.StudyParticipant;
import goorm.ddok.study.domain.StudyRecruitment;
import goorm.ddok.study.domain.TeamStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "StudyParticipationResponse",
        description = "참여 스터디 조회 응답 DTO",
        example = """
        {
          "studyId": 1,
          "teamId": 8,
          "title": "면접 스터디",
          "teamStatus": "CLOSED",
          "location": {
              "address": "전북 익산시 부송동 망산길 11-17",
              "region1depthName": "전북",
              "region2depthName": "익산시",
              "region3depthName": "부송동",
              "roadName": "망산길",
              "mainBuildingNo": "11",
              "subBuildingNo": "17",
              "zoneNo": "54547",
              "latitude": 35.976749396987046,
              "longitude": 126.99599512792346
          },
          "period": {
            "start": "2025-08-08",
            "end": "2025-09-09"
          }
        }
        """
)
public class StudyParticipationResponse {

    @Schema(description = "스터디 ID", example = "1")
    private Long studyId;

    @Schema(description = "팀 ID", example = "2")
    private Long teamId;

    @Schema(description = "스터디 제목", example = "면접 스터디")
    private String title;

    @Schema(description = "팀 상태 (RECRUITING / ONGOING / CLOSED)", example = "CLOSED")
    private TeamStatus teamStatus;

    @Schema(description = "스터디 장소 정보 (offline일 경우만 존재)")
    private StudyLocationResponse location;

    @Schema(description = "진행 기간 (시작일 ~ 종료일)")
    private PeriodResponse period;

    public static StudyParticipationResponse from(StudyParticipant participant, Long teamId) {
        StudyRecruitment study = participant.getStudyRecruitment();

        return StudyParticipationResponse.builder()
                .studyId(study.getId())
                .teamId(teamId)
                .title(study.getTitle())
                .teamStatus(study.getTeamStatus())
                .location(StudyLocationResponse.from(study))
                .period(PeriodResponse.from(
                        study.getStartDate(),
                        study.getExpectedMonths(),
                        study.getTeamStatus()
                ))
                .build();
    }
}