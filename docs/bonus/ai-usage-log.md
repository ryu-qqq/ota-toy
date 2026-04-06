# AI 활용 기록

## 활용 개요

- **사용 도구**: Claude Code (CLI) + Playwright MCP + 커스텀 에이전트/스킬 시스템
- **활용 방식**: 도메인 리서치 → 설계 → 코드 생성 → 리뷰 → 테스트까지 전 과정에서 AI를 활용하되, 매 단계에서 본인의 판단을 더함
- **핵심 원칙**: AI가 생성한 내용을 그대로 사용하지 않고, 설계 의사결정과 코드 품질 판단은 직접 수행

---

## 1. 멀티 에이전트 파이프라인 시스템

이 프로젝트의 가장 핵심적인 AI 활용 방식입니다. **역할별 전문 에이전트 29개**를 설계하고, 이들이 서로 다른 관점에서 협업하는 파이프라인을 구축했습니다.

### 왜 멀티 에이전트인가

단일 AI에게 "코드 짜줘"라고 하면 컨벤션 위반, 설계 불일치, 테스트 누락이 발생합니다. 이를 해결하기 위해 **역할을 분리**했습니다:
- 코드를 **만드는** 에이전트와 **검증하는** 에이전트를 분리
- 검증이 실패하면 **수정 요청(FIX-REQUEST)**을 보내고, 최대 2회 루프 후에도 해결 안 되면 **에스컬레이션**
- 컨벤션 위반 시 **convention-guardian**이 ArchUnit 테스트로 강제

### 에이전트 역할 분류

| 분류 | 에이전트 | 핵심 책임 |
|------|---------|-----------|
| **기획** | product-owner | 요구사항 → 백로그 → 수용기준 정의 |
| **설계** | project-lead | 아키텍처 결정, 컨벤션 정의, ADR 작성 |
| **도메인 개발** | domain-builder | 도메인 모델 코드 생성 (Aggregate, VO, Enum) |
| **도메인 검증** | domain-code-reviewer | 컨벤션/구조 기준 코드 리뷰 (9개 체크리스트) |
| **도메인 스펙 검증** | domain-spec-reviewer | 비즈니스 규칙 완전성 검증 |
| **도메인 테스트** | domain-test-designer | 6개 카테고리 테스트 설계/작성 |
| **Application 개발** | application-builder | UseCase, Port, Manager, Validator, Factory 생성 |
| **Application 검증** | application-reviewer | Application 컨벤션 검증 (10개 체크리스트) |
| **Application 테스트** | application-test-designer | Mock 기반 단위 테스트 설계 |
| **API 개발** | rest-api-builder | Controller, DTO, ApiMapper, Swagger 생성 |
| **API 테스트** | rest-api-test-designer | MockMvc + REST Docs 테스트 설계 |
| **Persistence 개발** | persistence-mysql-builder | JPA Entity, Mapper, Adapter, Flyway 생성 |
| **Persistence 테스트** | persistence-mysql-test-designer | Testcontainers MySQL 통합 테스트 설계 |
| **컨벤션 수호** | convention-guardian | ArchUnit 테스트 유일 수정자, 네이밍/구조 강제 |
| **컨벤션 조사** | convention-advocate | 컨벤션 이의 제기 시 타당성 조사 |
| **의존성 관리** | dependency-guardian | build.gradle.kts / libs.versions.toml 유일 수정자 |
| **QA/PM** | project-manager | 산출물 감사, 완성도 검증, 리스크 식별 |
| **기록** | journal-recorder | 의사결정/진행 시드 데이터 자동 수집 |
| **채용** | agent-recruiter | 새 에이전트 생성, 파이프라인 연결 |
| **오케스트레이터** | pipeline-orchestrator | 파이프라인 실행 엔진 |

### 파이프라인 흐름

각 레이어(Domain, Application, Persistence, REST API)마다 **하네스(harness)**가 있어서 빌드 → 리뷰 → FIX → 테스트를 자동으로 순환합니다.

```
[빌드] builder가 코드 생성
  ↓
[리뷰] reviewer가 컨벤션/구조 검증 → PASS or FIX-REQUEST
  ↓
[수정] FIX-REQUEST 시 builder가 수정 (최대 2회)
  ↓
[테스트] test-designer가 테스트 작성 → 실행
  ↓
[에스컬레이션] FIX 2회 초과 시 → project-lead 분석 → 사용자 의사결정
```

### 피드백 프로토콜

에이전트 간 소통을 위한 프로토콜을 정의했습니다.

| 프로토콜 | 발신 → 수신 | 용도 |
|---------|-----------|------|
| FIX-REQUEST / FIX-RESPONSE | 리뷰어 → 빌더 | 코드 수정 요청 (최대 2회) |
| ESCALATION-REPORT | 하네스 → project-lead | FIX 루프 초과 시 분석 요청 |
| CONVENTION-DISPUTE | 빌더 → convention-advocate | 컨벤션 이의 제기 |
| DEP-REQUEST / DEP-RESPONSE | 빌더 → dependency-guardian | 의존성 추가 요청/승인 |
| CLARIFY-REQUEST | 빌더 → product-owner | 수용기준 명확화 요청 |

---

## 2. 커스텀 스킬 시스템

반복적인 작업을 **재실행 가능한 스킬**로 체계화했습니다.

| 스킬 | 커맨드 | 용도 |
|------|--------|------|
| **pipeline** | `/pipeline run STORY-xxx` | 전체 파이프라인 실행 (기획 → 설계 → 구현 → 리뷰 → 테스트) |
| **domain-harness** | `/domain-harness build` | 도메인 레이어 빌드 → 리뷰 → FIX → 테스트 |
| **application-harness** | `/application-harness build` | Application 레이어 하네스 실행 |
| **persistence-harness** | `/persistence-harness build` | Persistence 레이어 하네스 실행 |
| **rest-api-harness** | `/rest-api-harness build` | REST API 레이어 하네스 실행 |
| **test-integration** | `/test-integration` | Testcontainers 기반 E2E 통합 테스트 하네스 |
| **ota-research** | `/ota-research` | Playwright 기반 OTA 사이트 크롤링 리서치 |
| **journal** | `/journal` | 과정 기록 시드 저장 / 최종 문서 생성 |

---

## 3. OTA 도메인 리서치

### 활용 방식
- **도구**: Claude Code + Playwright MCP (browser_navigate, browser_snapshot)
- **AI의 역할**: 실제 OTA 3개 플랫폼에 접속하여 검색 결과/상세 페이지의 데이터 구조를 접근성 트리로 추출하고, 플랫폼 간 비교 분석 수행
- **본인의 판단**:
  - 크롤링 대상을 "해외 글로벌 + 해외 아시아 + 국내" 3개로 선정
  - AI가 추출한 필드 목록에서 구현 우선순위를 직접 판단 (Core / Design Only / Future)
  - AI가 제안한 "배리어프리", "지속가능성 인증" 등은 현재 스코프에서 제외

### 효과
- 도메인 초안에서 놓친 핵심 개념 발견 (성급, 결제방식, 가격 3단 구조, Supplier 법적 정보 등)
- 국내 OTA 실제 API 응답 분석을 통해 필드명과 구조를 1차 자료에서 도출

---

## 4. 코드 생성과 품질 관리

### AI가 담당한 것
- 컨벤션 문서 기반 코드 스캐폴딩 (Aggregate, UseCase, Controller, Entity 등)
- 테스트 시나리오 설계 및 테스트 코드 생성
- Flyway 마이그레이션 SQL 생성
- REST Docs snippet 기반 API 문서 생성

### 본인이 담당한 것
- **설계 의사결정**: BC 분리 기준, 동시성 전략 선택 (Redis Lua + DB 2중), 예약 2단계 프로세스 채택
- **컨벤션 정의**: 각 레이어별 컨벤션 문서 작성 및 ArchUnit 규칙 설정
- **코드 리뷰**: AI가 생성한 코드를 하네스 리뷰어로 검증 후, 최종 판단은 직접 수행
- **트레이드오프 판단**: ADR 문서에서 대안 비교 후 선택 근거 기록

---

## 5. 과정 기록 체계

- **시드(seed) / 최종 문서** 2단계 구조를 설계
- 작업 중 의사결정, AI 활용, 진행상황을 시드 데이터로 자유롭게 축적
- 제출 시 시드를 최종 문서(이 파일, progress-journal.md)로 정제
- AI 활용 기록에서 **"AI 제안"과 "본인 판단"을 반드시 분리**하도록 포맷 설계

---

## 본인 판단이 개입된 주요 의사결정

| 결정 | AI 제안 | 본인 판단 |
|------|--------|----------|
| 동시성 전략 | 5가지 대안 비교표 제시 | Redis Lua + DB 2중 구조 채택 (성능 + 정합성 + 가용성 균형) |
| 예약 프로세스 | 단일 요청 vs 2단계 | 2단계 채택 — OTA API 리서치에서 업계 표준 확인 |
| Outbox vs Spring Event | 두 방식 비교 | Outbox 채택 — 정상 경로 1개로 예측 가능 |
| 편의시설 구조 | 단일 리스트 제안 | 2레벨 분리 — 크롤링에서 숙소/객실 시설 분리 확인 |
| record vs class | record 유지 제안 | Long id 가진 엔티티는 class로 전환 — equals/hashCode 문제 |
| BC 세분화 | accommodation 하나로 | 14개 BC로 분리 — 생명주기/변경빈도/행위자가 다름 |
