# 📖 똑DDOK — 당신 곁의 동료, 딸깍!

> 지도 기반 프로젝트·스터디 매칭 & 팀 협업 플랫폼 

<br />

지도 기반으로 스터디/프로젝트를 빠르게 찾고 참여하고, 팀 협업(채팅·일정·알림)까지 한 곳에서 처리하는 플랫폼입니다.   

- 프로젝트 기간: 2025.08 ~ 2025.09 (기획 및 개발)   
- 시연영상 [YouTube](https://youtu.be/tJxeeBno15E?si=37zNZ9FemquKkHhN)   
- 배포: ~https://www.deepdirect.site/~   

<br />

![Java](https://img.shields.io/badge/Java_17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3-6DB33F?logo=springboot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-59666C?logo=spring&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?logo=springsecurity&logoColor=white)
![STOMP](https://img.shields.io/badge/STOMP_WebSocket-010101?logo=websocket&logoColor=white)

<br />

---

## 내 기여
| 카테고리 | 기여 내용 |
| --- | --- |
| **실시간 채팅** | - 채팅 API 구현 ([#44](https://github.com/DeepDirect/ddok-be/pull/44), [#192](https://github.com/DeepDirect/ddok-be/pull/192))<br>- 안 읽은 메시지 실시간 알림 구현 ([#226](https://github.com/DeepDirect/ddok-be/pull/226)) |
| **채팅 도메인 설계** | - ChatRoom, ChatMessage, ChatRoomMember 도메인/엔티티 설계 ([#63](https://github.com/DeepDirect/ddok-be/pull/63)) |
| **채팅방 관리** | - 팀/스터디 참여에 따른 채팅방 자동 생성 ([#151](https://github.com/DeepDirect/ddok-be/pull/151)) |
| **기타** | - README 개선 ([#243](https://github.com/DeepDirect/ddok-be/pull/243)) |


<br />

## 구현 기능
- **채팅 API & 도메인**
  - 채팅 API 기본 기능 구현, 채팅 도메인/엔티티 설계
- **채팅방 자동 생성**
  - 팀/스터디 참여 이벤트에 따른 채팅방 자동 생성
- **실시간 알림**
  - 안 읽은 메시지 실시간 알림(개인 구독 채널) 구현
- **소셜 로그인 호환**
  - 카카오 로그인 계정의 채팅 목록 조회 실패 → ID 기반 식별로 개선


<br />

## 이슈 해결 사례

1) 안 읽은 메시지 실시간 알림
   - 문제: 새 메시지 도착 시 안 읽음 표시가 즉시 반영되지 않음
   - 원인: 클라이언트 캐시 업데이트만 의존 → 서버 이벤트 누락 시 동기화 실패
   - 해결: 사용자별 알림 채널(`/sub/users/{id}/notifications`) 생성, 메시지 생성 이벤트를 구독자에게 푸시
   - 결과: 다중 탭/기기에서도 안 읽음 표시 실시간 반영   

2) 카카오 로그인 계정에서 채팅방 목록 불러오기 실패
   - 문제: 카카오 로그인 사용자가 사이드바에서 채팅 목록을 확인하려 할 때, 목록이 표시되지 않고 에러가 발생함
   - 원인: 기존 로직이 인증 정보의 email을 기준으로 사용자 식별 및 채팅 목록을 조회하도록 구현되어 있었음. 카카오 로그인은 인증 정보에 이메일을 제공하지 않아 조회에 실패함
   - 해결: 사용자 식별 로직을 email 기반에서 고유 ID 기반으로 변경하여 이메일 유무와 상관없이 계정 식별이 가능하도록 수정함
   - 결과: 카카오 로그인 계정에서도 채팅방 목록이 정상적으로 불러옴.

<br/>

---

## 📁 디렉토리 구조

```bash
src/
└── main/
    └── java/
        └── goorm/ddok/
            ├── badge/          # 배지 발급, 등급 관련 도메인
            ├── cafe/           # 추천 카페/장소 관련
            ├── chat/           # 채팅, 채팅 알림 관련
            ├── evaluation/     # 프로젝트/스터디 종료 평가 로직
            ├── global/         # 전역 설정, 보안, 예외처리, 공통 유틸
            ├── map/            # 지도/좌표 관련 기능
            ├── member/         # 사용자/회원가입/프로필 관리
            ├── notification/   # 알림(Notification) 처리
            ├── player/         # 사용자 카드, 필터링, 플레이어 조회
            ├── project/        # 프로젝트 관련 CRUD
            ├── reputation/     # 신뢰도(온도) 계산/관리
            ├── study/          # 스터디 관련 CRUD
            ├── team/           # 팀 생성/관리/권한
            └── DdokApplication # Spring Boot 메인 클래스
```

<br />

---

## 🏃‍➡️ 실행
```bash
./start-dev.sh
```

<br/>

---

## Credits
원본 저장소: [링크](https://github.com/DeepDirect/ddok-be)   
팀원:
| 이름      | 역할                 | GitHub 링크                                     |
|----------|--------------------|------------------------------------------------|
| 정원용     | 팀장, Full Stack, Infra    | [@projectmiluju](https://github.com/jihun-dev) |
| 권혜진     | Backend            | [@sunsetkk](https://github.com/sunsetkk)       |
| 박건      | Frontend            | [@Jammanb0](https://github.com/Jammanb0)       |
| 박소현     | Frontend           | [@ssoogit](https://github.com/ssoogit)         |
| 박재경     | Full Stack  | [@Shin-Yu-1](https://github.com/Shin-Yu-1) |
| 이은지     | Frontend           | [@ebbll](https://github.com/ebbll)             |
| 최범근     | Backend            | [@vayaconChoi](https://github.com/vayaconChoi) |
