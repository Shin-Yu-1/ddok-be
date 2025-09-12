package goorm.ddok.ai.service.prompt;

import goorm.ddok.global.dto.LocationDto;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public final class AiPromptFactory {

    private AiPromptFactory() {}

    private static String joinComma(List<String> list) {
        if (list == null || list.isEmpty()) return "-";
        return String.join(", ", list);
    }

    private static String orDash(String s) {
        return (s == null || s.isBlank()) ? "-" : s.trim();
    }

    /* =========================
     * 프로젝트 상세 프롬프트
     * ========================= */
    public static String buildProjectPrompt(
            String title,
            String expectedStart,   // yyyy-MM-dd
            Integer expectedMonth,
            String mode,            // online | offline
            LocationDto loc,
            Integer cap,
            List<String> traits,
            List<String> positions,
            String leaderPosition,
            String detail // 사용자가 미리 적은 메모(있으면 반영)
    ) {
        String address = (loc == null) ? null : loc.getAddress();
        String scheduleStart = orDash(expectedStart);
        String scheduleMonths = (expectedMonth == null) ? "-" : expectedMonth + "개월";
        String modeKo = "online".equalsIgnoreCase(mode) ? "온라인" : "오프라인";

        // 템플릿 고정: 모델이 “반드시 이 형식으로만” 출력하도록 명령
        return """
            너는 프로젝트 모집글을 한국어로 작성하는 어시스턴트다. 아래의 “출력 형식”을 반드시 그대로 지키고, 불필요한 말은 절대 덧붙이지 마라.
            [형식/개행 규칙]
            - 반드시 마크다운 헤딩과 체크박스 형식을 그대로 유지해라.
            - 모든 헤딩(`#`, `##`)은 **헤딩 줄 단독**으로 출력하고, **그 다음 줄부터 본문**을 쓴다. (예: `## 📋 프로젝트 소개` 다음 줄은 **빈 줄 1개**, 그 다음 줄부터 본문)
            - 섹션과 섹션 사이에는 **빈 줄 1개**만 둔다(과도한 줄바꿈 금지).
            - 목록은 각 항목마다 **새 줄**로 표기한다. 체크박스(`- [ ] 항목`)도 같은 규칙.
            - 콜론(`:`) 뒤에는 **반드시 공백 1개**를 둔다. (예: `- 시작일: 2025-09-29`)
            - 모든 줄바꿈/구분도 그대로 유지하되, 내용만 채워라.
            - 과장되거나 차별적 표현 금지, 존중하는 어조로 구체적으로 작성.
            - 제공된 사실(제목/일정/모집역할/모드/위치/특징 등)을 내용에 자연스럽게 녹여라.
            - 온전히 제공된 사실만 사용해서 내용을 작성하라.
            - 제공되지 않은 정보는 추정하지 말고 비워 두거나 간단히 "-" 로 둬라.
            - 동일한 섹션(제목/소개/목표/기술 스택/모집 역할/일정/연락처)을 두 번 이상 반복 출력하지 마라.

            [입력 데이터]
            - 제목: %s
            - 시작일: %s
            - 예상 기간: %s
            - 진행 방식: %s
            - 위치(주소): %s
            - 모집 정원: %s명
            - 팀 특성: %s
            - 모집 역할: %s
            - 리더 포지션: %s
            - 작성자 메모: %s

            [출력 형식 - 이 틀을 그대로 쓰고, 내용만 채워서 반환하라]

            # %s

            ## 📋 프로젝트 소개  
            (프로젝트에 대한 간단한 소개 3~5문장. 팀 특성과 진행 방식(온라인/오프라인), 위치가 있으면 한 줄 포함)

            ## 🎯 목표
            - (달성하고자 하는 목표 1)
            - (달성하고자 하는 목표 2)

            ## 🛠 기술 스택
            - 예상 기술 A: (예상 기술 또는 비워두기)
            - 예상 기술 B: (예상 기술 또는 비워두기)
            - 예상 기술 C: (예상 기술 또는 비워두기)

            ## 👥 모집 역할
            - [ ] %s
            - [ ] %s
            - [ ] %s

            ## 📅 일정
            - 시작일: %s
            - 예상 기간: %s

            ## 📞 연락처
            -
            """
                .formatted(
                        orDash(title),
                        scheduleStart,
                        scheduleMonths,
                        modeKo,
                        orDash(address),
                        (cap == null ? "-" : String.valueOf(cap)),
                        joinComma(traits),
                        joinComma(positions),
                        orDash(leaderPosition),
                        orDash(detail),

                        // 제목
                        orDash(title),

                        // 체크박스 3줄: 최대 3개만 예쁘게 전개 (부족하면 '-' 로 채움)
                        pick(positions, 0),
                        pick(positions, 1),
                        pick(positions, 2),

                        // 일정
                        scheduleStart,
                        scheduleMonths
                );
    }

    /* =========================
     * 스터디 상세 프롬프트
     * ========================= */
    public static String buildStudyPrompt(
            String title,
            String expectedStart,
            Integer expectedMonth,
            String mode,
            LocationDto loc,
            Integer cap,
            List<String> traits,
            String studyType // 예: 취업/면접, 자소서 등
    ) {
        String address = (loc == null) ? null : loc.getAddress();
        String scheduleStart = orDash(expectedStart);
        String scheduleMonths = (expectedMonth == null) ? "-" : expectedMonth + "개월";
        String modeKo = "online".equalsIgnoreCase(mode) ? "온라인" : "오프라인";

        return """
            너는 스터디 모집글을 한국어로 작성하는 어시스턴트다. 아래 “출력 형식”을 반드시 그대로 지켜라.
            [형식/개행 규칙]
            - 반드시 마크다운 헤딩과 체크박스 형식을 그대로 유지해라.
            - 모든 헤딩(`#`, `##`)은 **헤딩 줄 단독**으로 출력하고, **그 다음 줄부터 본문**을 쓴다. (예: `## 📋 스터디 소개` 다음 줄은 **빈 줄 1개**, 그 다음 줄부터 본문)
            - 섹션과 섹션 사이에는 **빈 줄 1개**만 둔다(과도한 줄바꿈 금지).
            - 목록은 각 항목마다 **새 줄**로 표기한다. 체크박스(`- [ ] 항목`)도 같은 규칙.
            - 콜론(`:`) 뒤에는 **반드시 공백 1개**를 둔다. (예: `- 시작일: 2025-09-29`)
            - 모든 줄바꿈/구분도 그대로 유지하되, 내용만 채워라.
            - 과장되거나 차별적 표현 금지, 존중하는 어조로 구체적으로 작성.
            - 제공된 사실(제목/일정/모집역할/모드/위치/특징 등)을 내용에 자연스럽게 녹여라.
            - 온전히 제공된 사실만 사용해서 내용을 작성하라.
            - 제공되지 않은 정보는 추정하지 말고 비워 두거나 간단히 "-" 로 둬라.
            - 동일한 섹션(제목/소개/목표/기술 스택/모집 역할/일정/연락처)을 두 번 이상 반복 출력하지 마라.

            [입력 데이터]
            - 제목: %s
            - 시작일: %s
            - 예상 기간: %s
            - 진행 방식: %s
            - 위치(주소): %s
            - 모집 정원: %s명
            - 팀 특성: %s
            - 스터디 유형: %s

            [출력 형식]

            # %s

            ## 📋 스터디 소개  
            (스터디에 대한 간단한 소개 3~5문장. 방식/장소가 있으면 한 줄 포함)

            ## 🎯 목표
            - (목표 1)
            - (목표 2)

            ## 🛠 스터디 유형
            - %s

            ## 👥 이런 분을 찾습니다!
            - [ ] %s
            - [ ] %s
            - [ ] %s

            ## 📅 일정
            - 시작일: %s
            - 예상 기간: %s

            ## 📞 연락처
            -
            """
                .formatted(
                        orDash(title),
                        scheduleStart,
                        scheduleMonths,
                        modeKo,
                        orDash(address),
                        (cap == null ? "-" : String.valueOf(cap)),
                        joinComma(traits),
                        orDash(studyType),

                        // 제목
                        orDash(title),

                        // 유형
                        orDash(studyType),

                        // 체크박스 3줄: traits에서 3개 뽑기
                        pick(traits, 0),
                        pick(traits, 1),
                        pick(traits, 2),

                        // 일정
                        scheduleStart,
                        scheduleMonths
                );
    }

    private static String pick(List<String> list, int idx) {
        if (list == null || list.size() <= idx) return "-";
        String v = Objects.toString(list.get(idx), "").trim();
        return v.isBlank() ? "-" : v;
    }
}