# 📖 똑DDOK — 당신 곁의 동료, 딸깍!

> 지도 기반 프로젝트·스터디 매칭 & 팀 협업 플랫폼 

<br />

지도 기반으로 스터디/프로젝트를 빠르게 찾고 참여하고, 팀 협업(채팅·일정·알림)까지 한 곳에서 처리하는 플랫폼입니다.   

프로젝트 기간: 2025.08 ~ 2025.09 (기획 및 개발)   
시연영상 [YouTube](https://www.youtube.com/watch?v=lYVMEXc4BAU)   
Link: [DDOK](http://www.deepdirect.site)
Code: [FE](https://github.com/DeepDirect/ddok-fe), [BE](https://github.com/DeepDirect/ddok-be)   

---

## 🫶 팀원
| 이름      | 역할                 | GitHub 링크                                     |
|----------|--------------------|------------------------------------------------|
| 정원용     | 팀장, Full Stack, Infra    | [@projectmiluju](https://github.com/projectmiluju) |
| 권혜진     | Backend            | [@sunsetkk](https://github.com/sunsetkk)       |
| 박건      | Frontend            | [@Jammanb0](https://github.com/Jammanb0)       |
| 박소현     | Frontend           | [@ssoogit](https://github.com/ssoogit)         |
| 박재경     | Full Stack  | [@Shin-Yu-1](https://github.com/Shin-Yu-1) |
| 이은지     | Frontend           | [@ebbll](https://github.com/ebbll)             |
| 최범근     | Backend            | [@vayaconChoi](https://github.com/vayaconChoi) |

<br />

---

## ✨ 주요 기능

- 지도 기반 탐색: 카카오맵에서 주변 스터디/프로젝트/플레이어를 한눈에 확인 (상태/카테고리 필터)
- 포지션 매칭: 역할/경험/시간대 기반 맞춤 필터와 추천
- 원클릭 참여: 오픈톡/댓글 없이 클릭 한 번으로 신청/취소
- 팀 협업: 팀 생성 시 자동 채팅방, 일정 조율(캘린더), 팀 ReadMe
- 신뢰도 시스템: 온도(완주율/기여도), 배지/랭킹으로 책임감과 지속 참여 유도

<br/>

## 🛠️ 기술 스택

| 분류                | 기술명                                                                                 |
|-------------------|--------------------------------------------------------------------------------------|
| **프레임워크/언어**    | Java 17, Spring Boot 3.x, Gradle                                                    |
| **DB/스토리지**       | PostgreSQL (AWS RDS), H2 (테스트 DB), AWS S3                                         |
| **ORM/데이터 관리**    | Spring Data JPA, Hibernate                                                          |
| **캐싱/브로커**       | Redis (세션 관리, Pub/Sub)                                                          |
| **실시간 통신**       | Spring WebSocket, STOMP                                                             |
| **보안/인증**        | Spring Security, JWT, OAuth2(Kakao), JavaMailSender, CoolSMS                         |
| **API 문서화**       | SpringDoc OpenAPI (Swagger)                                                         |
| **검증/유효성**       | Hibernate Validator                                                                |
| **배치/스케줄링**     | Spring Batch, Spring Scheduler                                                     |
| **로깅/모니터링**     | Logback, Actuator, Sentry                                                          |
| **검색/추천(옵션)**    | Elasticsearch                                                                      |
| **배포/인프라**       | Docker, AWS EC2, Nginx, Route53     

<br />

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
