package goorm.ddok.study.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import goorm.ddok.global.dto.LocationDto;
import goorm.ddok.global.dto.PreferredAgesDto;
import goorm.ddok.study.domain.StudyMode;
import goorm.ddok.study.domain.StudyType;
import goorm.ddok.study.domain.TeamStatus;
import goorm.ddok.study.dto.UserSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "StudyRecruitmentDetailResponse",
        description = "스터디 모집글 상세/수정페이지 조회/수정저장 공통 응답 DTO",
        example = """
                {
                    "status": 200,
                    "message": "스터디 수정페이지 조회가 성공했습니다.",
                    "data": {
                        "studyId": 10,
                        "title": "구지라지",
                        "teamId": 5,
                        "isTeamMember": true,
                        "studyType": "취업/면접",
                        "teamStatus": "RECRUITING",
                        "bannerImageUrl": "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMjAwIiBoZWlnaHQ9IjYwMCI+CiAgPHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iI0ZGREFCOSIvPgogIDx0ZXh0IHg9IjUwJSIgeT0iNTUlIiBmb250LXNpemU9IjgwIiBmaWxsPSJibGFjayIgZm9udC13ZWlnaHQ9ImJvbGQiCiAgICAgICAgdGV4dC1hbmNob3I9Im1pZGRsZSIgZG9taW5hbnQtYmFzZWxpbmU9Im1pZGRsZSIgZm9udC1mYW1pbHk9IkludGVyIj7qtazsp4Drnbzsp4A8L3RleHQ+Cjwvc3ZnPgo=",
                        "traits": [
                            "정리의 신",
                            "실행력 갓",
                            "내향인"
                        ],
                        "capacity": 6,
                        "applicantCount": 1,
                        "mode": "offline",
                        "location": {
                            "address": "서울 강남구 역삼동 테헤란로 123-45",
                            "region1depthName": "서울",
                            "region2depthName": "강남구",
                            "region3depthName": "역삼동",
                            "roadName": "테헤란로",
                            "mainBuildingNo": "123",
                            "subBuildingNo": "45",
                            "zoneNo": "06236",
                            "latitude": 37.566500,
                            "longitude": 126.978000
                        },
                        "preferredAges": {
                            "ageMin": 20,
                            "ageMax": 30
                        },
                        "expectedMonth": 3,
                        "startDate": "2025-09-16",
                        "detail": "저희 정말 멋진 영어공부를 할거예요~ 하고 싶죠?",
                        "leader": {
                            "userId": 1,
                            "nickname": null,
                            "profileImageUrl": "http://k.kakaocdn.net/dn/YwWOR/btsf5eC521B/jh2H7E9hYKPOK7Y8O7EPsk/img_640x640.jpg",
                            "mainPosition": null,
                            "mainBadge": null,
                            "abandonBadge": null,
                            "temperature": null,
                            "chatRoomId": null,
                            "isMine": true,
                            "dmRequestPending": false
                        },
                        "participants": [
                            {
                                "userId": 2,
                                "nickname": null,
                                "profileImageUrl": null,
                                "mainPosition": null,
                                "mainBadge": null,
                                "abandonBadge": null,
                                "temperature": null,
                                "chatRoomId": null,
                                "isMine": false,
                                "dmRequestPending": false
                            }
                        ],
                        "participantsCount": 1,
                        "isMine": true
                    }
                }
        """
)
public class StudyRecruitmentDetailResponse {

    @Schema(description = "스터디 ID", example = "2")
    private Long studyId;

    @Schema(description = "제목", example = "프리토킹 스터디")
    private String title;

    @Schema(description = "팀 ID", example = "5")
    private Long teamId;

    @Schema(description = "내가 팀 멤버인지 여부", example = "true")
    private boolean IsTeamMember;

    @Schema(description = "스터디 유형", example = "JOB_INTERVIEW")
    private StudyType studyType;

    @JsonProperty("isMine")
    @Schema(description = "내가 작성자인지 여부", example = "true")
    private boolean IsMine;

    @Schema(description = "팀 상태", example = "RECRUITING")
    private TeamStatus teamStatus;

    @Schema(description = "배너 이미지 URL")
    private String bannerImageUrl;

    @Schema(description = "모집 성향 리스트")
    private List<String> traits;

    @Schema(description = "모집 정원", example = "4")
    private Integer capacity;

    @Schema(description = "지원자 수", example = "6")
    private Integer applicantCount;

    @Schema(description = "진행 방식", example = "online")
    private StudyMode mode;

    @Schema(
            description = """
                스터디 진행 장소 (offline일 때만 존재).
                Kakao road_address 매핑 필드 사용.
                """,
            implementation = LocationDto.class
    )
    private LocationDto location;

    @Schema(description = "선호 연령대 (무관 시 null)")
    private PreferredAgesDto preferredAges;

    @Schema(description = "예상 진행 개월 수", example = "3")
    private Integer expectedMonth;

    @Schema(description = "시작 예정일", example = "2025-09-10")
    private LocalDate startDate;

    @Schema(description = "상세 설명")
    private String detail;

    @Schema(description = "리더 정보")
    private UserSummaryDto leader;

    @Schema(description = "참여자 리스트(리더 제외)")
    private List<UserSummaryDto> participants;

    @Schema(description = "참여자 수(리더 제외)", example = "3")
    private Integer participantsCount;

    @Schema(description = "현재 사용자가 이 스터디에 지원했는지 여부", example = "false")
    private boolean IsApplied;

    @Schema(description = "현재 사용자가 이 스터디에 확정되었는지 여부", example = "false")
    private boolean IsApproved;
}