package goorm.ddok.project.dto.response;

import goorm.ddok.global.dto.LocationDto;
import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.project.domain.ProjectMode;
import goorm.ddok.project.domain.TeamStatus;
import goorm.ddok.project.dto.ProjectPositionDto;
import goorm.ddok.project.dto.ProjectUserSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "ProjectDetailResponse",
        description = "프로젝트 모집글 상세 조회 응답 DTO",
        example = """
        {
          "projectId": 2,
          "title": "구라라지 프로젝트",
          "isMine": true,
          "teamId": 5,
          "isTeamMember": true,
          "teamStatus": "RECRUITING",
          "bannerImageUrl": "https://cdn.example.com/images/default.png",
          "traits": ["정리의 신","실행력 갓","내향인"],
          "capacity": 6,
          "applicantCount": 4,
          "mode": "offline",
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
          "preferredAges": { "ageMin": 20, "ageMax": 30 },
          "expectedMonth": 3,
          "startDate": "2025-10-01",
          "detail": "함께 멋진 웹을 개발할 팀원을 찾습니다.",
          "positions": [
            { "position": "PM", "applied": 2, "confirmed": 1, "isApplied": false, "isApproved": false, "isAvailable": true },
            { "position": "UI/UX", "applied": 1, "confirmed": 0, "isApplied": true, "isApproved": true, "isAvailable": true },
            { "position": "백엔드", "applied": 1, "confirmed": 1, "isApplied": false, "isApproved": false, "isAvailable": false }
          ],
          "leader": {
            "userId": 10,
            "nickname": "고라니",
            "profileImageUrl": "https://cdn.example.com/images/leader.png",
            "mainPosition": "백엔드",
            "temperature": 36.5,
            "decidedPosition": "백엔드",
            "isMine": true,
            "chatRoomId": null,
            "dmRequestPending": false
          },
          "participants": [
            {
              "userId": 20,
              "nickname": "프론트왕",
              "profileImageUrl": "https://cdn.example.com/images/user20.png",
              "mainPosition": "프론트엔드",
              "temperature": 37.0,
              "decidedPosition": "프론트엔드",
              "isMine": false,
              "chatRoomId": null,
              "dmRequestPending": false
            }
          ]
        }
        """
)
public class ProjectDetailResponse {

    @Schema(description = "프로젝트 ID", example = "2")
    private Long projectId;

    @Schema(description = "내가 작성한 글인지 여부", example = "true")
    private boolean IsMine;

    @Schema(description = "팀 ID", example = "5")
    private Long teamId;

    @Schema(description = "내가 팀 멤버인지 여부", example = "true")
    private boolean IsTeamMember;

    @Schema(description = "프로젝트 제목", example = "구라라지 프로젝트")
    private String title;

    @Schema(description = "팀 상태 (RECRUITING / ONGOING / CLOSED)", example = "RECRUITING")
    private TeamStatus teamStatus;

    @Schema(description = "배너 이미지 URL", example = "https://cdn.example.com/images/default.png")
    private String bannerImageUrl;

    @Schema(description = "모집 성향 리스트", example = "[\"정리의 신\", \"실행력 갓\", \"내향인\"]")
    private List<String> traits;

    @Schema(description = "모집 정원", example = "4")
    private Integer capacity;

    @Schema(description = "지원자 수", example = "6")
    private Integer applicantCount;

    @Schema(description = "진행 방식 (online / offline)", example = "online")
    private ProjectMode mode;

    @Schema(
            description = """
                프로젝트 진행 장소 (offline일 때만 존재).
                Kakao road_address 매핑 필드 사용.
                """,
            implementation = LocationDto.class
    )
    private LocationDto location;

    @Schema(description = "선호 연령대")
    private PreferredAgesDto preferredAges;

    @Schema(description = "예상 진행 개월 수", example = "3")
    private Integer expectedMonth;

    @Schema(description = "프로젝트 시작 예정일", example = "2025-09-10")
    private LocalDate startDate;

    @Schema(description = "상세 설명", example = "저희 정말 멋진 웹을 만들거에요~ 하고 싶죠?")
    private String detail;

    @Schema(description = "포지션별 모집 현황")
    private List<ProjectPositionDto> positions;

    @Schema(description = "리더 정보")
    private ProjectUserSummaryDto leader;

    @Schema(description = "참여자 목록 (리더 제외)")
    private List<ProjectUserSummaryDto> participants;

}