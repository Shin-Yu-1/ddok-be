package goorm.ddok.project.dto.response;

import goorm.ddok.global.dto.AbandonBadgeDto;
import goorm.ddok.global.dto.BadgeDto;
import goorm.ddok.global.dto.LocationDto;
import goorm.ddok.global.dto.PreferredAgesDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@Schema(
        name = "ProjectUpdateResultResponse",
        description = """
            프로젝트 모집글 수정 결과 응답 DTO.
            - mode 가 online 이면 location 은 null 입니다.
            - location 은 카카오 road_address 매핑 필드로 내려갑니다.
            """,
        example = """
        {
          "projectId": 3,
          "isMine": true,
          "title": "오프라인 프로젝트 - 풀필드",
          "teamStatus": "RECRUITING",
          "bannerImageUrl": "https://cdn.example.com/images/default.png",
          "traits": ["집중력","성실함"],
          "capacity": 3,
          "applicantCount": 8,
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
          "startDate": "2025-10-10",
          "detail": "오프라인으로 진행합니다.",
          "positions": [
            { "position": "PM", "applied": 2, "confirmed": 1, "isApplied": false, "isApproved": false, "isAvailable": true },
            { "position": "프론트엔드", "applied": 3, "confirmed": 0, "isApplied": false, "isApproved": false, "isAvailable": true },
            { "position": "백엔드", "applied": 3, "confirmed": 0, "isApplied": false, "isApproved": false, "isAvailable": true }
          ],
          "leader": {
            "userId": 1,
            "nickname": "개구라",
            "profileImageUrl": "https://cdn.example.com/images/u1.png",
            "mainPosition": "백엔드",
            "mainBadge": null,
            "abandonBadge": null,
            "temperature": 36.5,
            "decidedPosition": "PM",
            "chatRoomId": null,
            "dmRequestPending": false,
            "isMine": true
          },
          "participants": [
            {
              "userId": 4, "nickname": "hong",
              "profileImageUrl": "https://picsum.photos/seed/1/200/200",
              "mainPosition": "프론트엔드",
              "mainBadge": null, "abandonBadge": null,
              "temperature": 36.5,
              "decidedPosition": "프론트엔드",
              "chatRoomId": null, "dmRequestPending": false,
              "isMine": false
            }
          ]
        }
        """
)
public class ProjectUpdateResultResponse {

    @Schema(description = "프로젝트 ID", example = "3")
    private Long projectId;

    @Schema(description = "내가 작성한 글인지 여부", example = "true")
    private boolean IsMine;

    @Schema(description = "프로젝트 제목", example = "오프라인 프로젝트 - 풀필드")
    private String title;

    @Schema(description = "팀 상태", allowableValues = {"RECRUITING","ONGOING","CLOSED"}, example = "RECRUITING")
    private String teamStatus;

    @Schema(description = "배너 이미지 URL", example = "https://cdn.example.com/images/default.png")
    private String bannerImageUrl;

    @Schema(description = "모집 성향 리스트", example = "[\"집중력\",\"성실함\"]")
    private List<String> traits;

    @Schema(description = "모집 정원", example = "3")
    private Integer capacity;

    @Schema(description = "총 지원자 수(프로젝트 전체)", example = "8")
    private Long applicantCount;

    @Schema(description = "진행 방식", allowableValues = {"online","offline"}, example = "offline")
    private String mode;     // "online" | "offline"

    @Schema(
            description = """
                프로젝트 진행 장소 (offline일 때만 존재).
                Kakao road_address 매핑 필드 사용.
                """,
            implementation = LocationDto.class
    )
    private LocationDto location;

    @Schema(description = "선호 연령대 (무관이면 null)", example = "{\"ageMin\":20,\"ageMax\":30}")
    private PreferredAgesDto preferredAges;

    @Schema(description = "예상 진행 개월 수", example = "3")
    private Integer expectedMonth;

    @Schema(description = "프로젝트 시작 예정일", example = "2025-10-10")
    private LocalDate startDate;

    @Schema(description = "상세 설명 (Markdown)", example = "오프라인으로 진행합니다.")
    private String detail;

    @Schema(description = "포지션별 모집 현황")
    private List<PositionItem> positions;

    @Schema(description = "리더 정보")
    private LeaderBlock leader;

    @Schema(description = "참여자 목록 (리더 제외)")
    private List<ParticipantBlock> participants;

    @Getter
    @Builder
    @Schema(name = "ProjectUpdateResultResponse.PositionItem", description = "포지션별 요약")
    public static class PositionItem {
        @Schema(description = "포지션명", example = "백엔드")
        private String position;

        @Schema(description = "지원자 수(해당 포지션의 ProjectApplication 수)", example = "3")
        private Long applied;

        @Schema(description = "확정자 수(해당 포지션의 리더/멤버 Participant 수 - Soft Delete 제외)", example = "1")
        private Long confirmed;

        @Schema(description = "내 지원 여부(이 프로젝트의 어떤 포지션이든 지원했는지)", example = "false")
        private boolean IsApplied;

        @Schema(description = "내 승인 여부(이 프로젝트에서 내 지원이 승인되어 멤버로 확정됐는지)", example = "false")
        private boolean IsApproved;

        @Schema(description = "지원 가능 여부(팀 상태/정원/중복 지원 등 정책에 따라 산정)", example = "true")
        private boolean IsAvailable;
    }

    @Getter
    @Builder
    @Schema(name = "ProjectUpdateResultResponse.LeaderBlock", description = "리더 정보")
    public static class LeaderBlock {
        @Schema(description = "리더 사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "리더 닉네임", example = "개구라")
        private String nickname;

        @Schema(description = "리더 프로필 이미지 URL", example = "https://cdn.example.com/images/u1.png")
        private String profileImageUrl;

        @Schema(description = "리더의 대표 포지션", example = "백엔드")
        private String mainPosition;

        @Schema(description = "대표 배지(없으면 null)")
        private BadgeDto mainBadge;

        @Schema(description = "이탈 배지(없으면 null)")
        private AbandonBadgeDto abandonBadge;

        @Schema(description = "온도(리더의 평판 지표, 없으면 null)", example = "36.5")
        private Double temperature;

        @Schema(description = "리더가 수행하는 포지션(모집 포지션명)", example = "PM")
        private String decidedPosition;

        @Schema(description = "내가 리더인지 여부", example = "true")
        private boolean IsMine;

        @Schema(description = "리더와의 채팅방 ID(없으면 null)", example = "null")
        private Long chatRoomId;

        @Schema(description = "DM 요청 대기 여부", example = "false")
        private boolean dmRequestPending;
    }

    @Getter
    @Builder
    @Schema(name = "ProjectUpdateResultResponse.ParticipantBlock", description = "참여자 정보(리더 제외)")
    public static class ParticipantBlock {
        @Schema(description = "사용자 ID", example = "4")
        private Long userId;

        @Schema(description = "닉네임", example = "hong")
        private String nickname;

        @Schema(description = "프로필 이미지 URL", example = "https://picsum.photos/seed/1/200/200")
        private String profileImageUrl;

        @Schema(description = "대표 포지션", example = "프론트엔드")
        private String mainPosition;

        @Schema(description = "대표 배지(없으면 null)")
        private BadgeDto mainBadge;

        @Schema(description = "이탈 배지(없으면 null)")
        private AbandonBadgeDto abandonBadge;

        @Schema(description = "온도(없으면 null)", example = "36.5")
        private Double temperature;

        @Schema(description = "확정된 포지션(모집 포지션명)", example = "프론트엔드")
        private String decidedPosition;

        @Schema(description = "내 자신인지 여부", example = "false")
        private boolean IsMine;

        @Schema(description = "채팅방 ID(없으면 null)", example = "null")
        private Long chatRoomId;

        @Schema(description = "DM 요청 대기 여부", example = "false")
        private boolean dmRequestPending;
    }
}