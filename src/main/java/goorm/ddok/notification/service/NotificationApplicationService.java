package goorm.ddok.notification.service;

import goorm.ddok.global.exception.ErrorCode;
import goorm.ddok.global.exception.GlobalException;
import goorm.ddok.notification.dto.NotificationPayload;
import goorm.ddok.notification.event.DmRequestDecisionEvent;
import goorm.ddok.notification.event.ProjectJoinRequestDecisionEvent;
import goorm.ddok.notification.event.StudyJoinRequestDecisionEvent;
import goorm.ddok.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationApplicationService {

    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationPushService pushService;

    // 액션 가능한 타입만 허용
    private static final Set<String> ACTIONABLE = Set.of(
            "PROJECT_JOIN_REQUEST",
            "STUDY_JOIN_REQUEST",
            "DM_REQUEST"
    );

    /**
     * @param meUserIdStr  CONNECT Principal.getName() (문자열 userId)
     * @param notificationIdStr STOMP dest 변수 (문자열 id)
     * @param action "accept" | "reject"
     */
    @Transactional
    public void handleAction(String meUserIdStr, String notificationIdStr, String action) {
        Long me = parseLong(meUserIdStr, "meUserId");
        Long notiId = parseLong(notificationIdStr, "notificationId");

        var noti = notificationRepository.findById(notiId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND));

        // 소유자 검증
        if (!noti.getReceiver().getId().equals(me)) {
            throw new GlobalException(ErrorCode.FORBIDDEN);
        }

        // 중복 처리 방지 (processed 플래그/상태 컬럼이 없다면, 임시로 isRead+serverNote 등 활용)
        if (Boolean.TRUE.equals(noti.getProcessed())) {
            // 멱등 처리: 그냥 리턴 (또는 에러)
            return;
        }

        String type = noti.getType().name(); // enum → 문자열
        if (!ACTIONABLE.contains(type)) {
            throw new GlobalException(ErrorCode.NOT_ACTIONABLE); // 액션 불가 타입
        }

        // 도메인별 이벤트 발행
        switch (type) {
            case "PROJECT_JOIN_REQUEST" -> publishProjectDecision(noti, me, action);
            case "STUDY_JOIN_REQUEST"   -> publishStudyDecision(noti, me, action);
            case "DM_REQUEST"           -> publishDmDecision(noti, me, action);
            default -> throw new GlobalException(ErrorCode.NOT_ACTIONABLE);
        }

        // 처리 상태 업데이트
        noti.setProcessed(true);
        noti.setProcessedAt(Instant.now());
        noti.setRead(true); // 액션과 동시에 읽음 처리하는 정책
        notificationRepository.save(noti);

        // (선택) 나 자신에게 “처리 완료” 토스트성 알림 푸시
        pushService.pushToUser(me, NotificationPayload.builder()
                .id(String.valueOf(noti.getId()))
                .type("ACTION_RESULT")
                .message(action.equals("accept") ? "요청을 수락했습니다." : "요청을 거절했습니다.")
                .isRead(true)
                .createdAt(Instant.now())
                .userId(String.valueOf(me))
                .build());
    }

    private void publishProjectDecision(goorm.ddok.notification.domain.Notification noti,
                                        Long approverUserId, String decision) {
        // 🔧 TODO: 엔티티에서 필요한 값 매핑
        Long projectId = noti.getProjectId();        // 예: Long
        Long applicantUserId = noti.getApplicantUserId(); // 신청자(피처리자)

        if (projectId == null || applicantUserId == null) {
            throw new GlobalException(ErrorCode.NOT_ACTIONABLE);
        }

        eventPublisher.publishEvent(ProjectJoinRequestDecisionEvent.builder()
                .approverUserId(approverUserId)
                .applicantUserId(applicantUserId)
                .projectId(projectId)
                .decision(normalizeDecision(decision))
                .notificationId(noti.getId())
                .build());
    }

    private void publishStudyDecision(goorm.ddok.notification.domain.Notification noti,
                                      Long approverUserId, String decision) {
        Long studyId = noti.getStudyId();
        Long applicantUserId = noti.getApplicantUserId();

        if (studyId == null || applicantUserId == null) {
            throw new GlobalException(ErrorCode.NOT_ACTIONABLE);
        }

        eventPublisher.publishEvent(StudyJoinRequestDecisionEvent.builder()
                .approverUserId(approverUserId)
                .applicantUserId(applicantUserId)
                .studyId(studyId)
                .decision(normalizeDecision(decision))
                .notificationId(noti.getId())
                .build());
    }

    private void publishDmDecision(goorm.ddok.notification.domain.Notification noti,
                                   Long approverUserId, String decision) {
        Long requesterUserId = noti.getRequesterUserId(); // DM을 보낸 사람

        if (requesterUserId == null) {
            throw new GlobalException(ErrorCode.NOT_ACTIONABLE);
        }

        eventPublisher.publishEvent(DmRequestDecisionEvent.builder()
                .approverUserId(approverUserId)
                .requesterUserId(requesterUserId)
                .decision(normalizeDecision(decision))
                .notificationId(noti.getId())
                .build());
    }

    private String normalizeDecision(String action) {
        if ("accept".equalsIgnoreCase(action)) return "accept";
        if ("reject".equalsIgnoreCase(action)) return "reject";
        throw new GlobalException(ErrorCode.NOT_ACTIONABLE);
    }

    private Long parseLong(String s, String name) {
        try { return Long.parseLong(s); }
        catch (Exception e) { throw new GlobalException(ErrorCode.NOT_ACTIONABLE); }
    }
}
