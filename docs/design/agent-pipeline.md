# 에이전트 조직 파이프라인 설계

## 개요

실제 개발 조직처럼 **역할별 전문 에이전트**가 계층적으로 협업하는 파이프라인을 구성한다.
각 에이전트는 명확한 책임, 제한된 도구 권한, 표준화된 입출력 형식을 갖는다.
팀은 필요시 확장 가능한 구조로 설계하며, Adapter 팀은 확정된 인터페이스부터 순차적으로 구성한다.

---

## 조직도

```
┌─────────────────── Strategic Layer ───────────────────┐
│  product-owner     project-lead      project-manager  │
│  (WHAT/우선순위)    (HOW/아키텍처)     (WHEN/산출물)    │
└────────┬──────────────┬──────────────────┬────────────┘
         │              │                  │
         │    ┌─────────▼──────────┐       │
         │    │ Convention Layer   │       │
         │    │ convention-guardian│       │
         │    │ convention-advocate│       │
         │    └─────────┬──────────┘       │
         │              │ ArchUnit 강제     │
    ┌────▼──────────────▼──────────────────▼────┐
    │              Implementation Layer          │
    │                                            │
    │  ┌─Domain Team──┐  ┌─Application Team─┐   │
    │  │ builder      │  │ builder          │   │
    │  │ code-reviewer│  │ reviewer         │   │
    │  │ spec-reviewer│  │ test-designer    │   │
    │  │ test-designer│  │                  │   │
    │  └──────────────┘  └─────────────────┘   │
    │                                            │
    │  ┌─Adapter-in────┐  ┌─Adapter-out────┐   │
    │  │ rest-api-     │  │ persistence-   │   │
    │  │  builder      │  │  mysql-builder │   │
    │  │ rest-api-     │  │ persistence-   │   │
    │  │  test-designer│  │  mysql-test-   │   │
    │  │ [reviewer     │  │  designer      │   │
    │  │  — 계획]      │  │ [reviewer      │   │
    │  └───────────────┘  │  — 계획]       │   │
    │                     └────────────────┘   │
    └────────────────────────────────────────────┘
         │
    ┌────▼──────────────────┐
    │ Cross-cutting         │
    │ journal-recorder      │
    └───────────────────────┘
```

---

## 에이전트 목록

### Strategic Layer (3개)

| 에이전트 | 역할 | 페르소나 |
|---------|------|---------|
| product-owner | 요구사항 분석, 백로그 분해, 우선순위/수용기준 정의 | 서비스 기획자 |
| project-lead | 아키텍처 의사결정, 컨벤션 정의, ADR, 구현 가이드 | 시니어 아키텍트 |
| project-manager | 산출물 추적, 완성도 검증, 리스크 식별 | QA/PM |

### Convention Layer (2개)

| 에이전트 | 역할 | 특이사항 |
|---------|------|---------|
| convention-guardian | ArchUnit 소유/수정, 컨벤션 강제, 이의 최종 판정 | **ArchUnit 유일 수정자** |
| convention-advocate | 컨벤션 이의 조사, 타당성 판단, guardian에 보고 | 조사/보고만, 수정 권한 없음 |

### Domain Team (4개)

| 에이전트 | 역할 |
|---------|------|
| domain-builder | Aggregate, VO, Enum, ID 등 도메인 모델 코드 생성 |
| domain-code-reviewer | 코드 컨벤션/구조 검증 (ArchUnit 실행) |
| domain-spec-reviewer | 비즈니스 규칙 완전성 검증 (기획 관점) |
| domain-test-designer | 도메인 비즈니스 규칙 테스트 설계/작성 |

### Application Team (3개)

| 에이전트 | 역할 |
|---------|------|
| application-builder | UseCase 인터페이스, Outbound Port, Application Service 생성 |
| application-reviewer | Application 레이어 컨벤션/구조/의존 방향 검증 |
| application-test-designer | UseCase 조합, Port 호출, 이벤트 발행 테스트 설계/작성 |

### Adapter-in Team (2개 활성 + reviewer 계획)

| 에이전트 | 역할 | 상태 |
|---------|------|------|
| rest-api-builder | Controller, Request/Response DTO, Swagger, ExceptionHandler | 활성 |
| rest-api-test-designer | MockMvc 기반 API 포맷/상태코드/에러 핸들링 테스트 | 활성 |
| rest-api-reviewer | API 컨벤션, 응답 포맷, 보안 규칙 검증 | **계획** |

> **reviewer 미생성 사유:** Adapter-in은 REST API 외에도 Admin API, MQ Consumer, Scheduler 등
> 다양한 유형으로 확장될 수 있다. 현재 REST API만 확정된 상태이므로, Adapter-in 전체에 적용할
> 공통 리뷰 기준이 아직 정립되지 않았다. 각 Adapter 유형이 확정되는 시점에 해당 reviewer를 함께 생성한다.

### Adapter-out Team (2개 활성 + reviewer 계획)

| 에이전트 | 역할 | 상태 |
|---------|------|------|
| persistence-mysql-builder | JPA Entity, Repository, QueryDSL, Flyway, Domain↔Entity 매핑 | 활성 |
| persistence-mysql-test-designer | Testcontainers MySQL 기반 CRUD/동시성 통합 테스트 | 활성 |
| persistence-mysql-reviewer | 쿼리 성능, 인덱스 설계, N+1 문제 검증 | **계획** |

> **reviewer 미생성 사유:** Adapter-out 역시 Redis, 외부 API Client, MQ Producer 등으로 확장된다.
> persistence-mysql에 특화된 리뷰 기준(쿼리 성능, 인덱스)과 범용 Adapter-out 리뷰 기준을
> 분리해야 하므로, 전체 Adapter-out 유형이 윤곽을 잡힌 후 reviewer를 설계한다.

### Cross-cutting (1개)

| 에이전트 | 역할 |
|---------|------|
| journal-recorder | 각 팀 작업 완료 시 의사결정/AI활용/진행상황 수집 및 시드 기록 |

### Meta (1개)

| 에이전트 | 역할 |
|---------|------|
| agent-recruiter | 에이전트 조직 관리 — 생성, 수정, 파이프라인 연결, 문서 갱신 |

---

## 피드백 루프 프로토콜

### 1. FIX-REQUEST / FIX-RESPONSE (구현팀 내부)

reviewer/test-designer가 FAIL을 발견하면 해당 builder에게 수정 요청.

**요청 포맷:**
```
- 요청자: {에이전트명}
- 대상 파일: {파일 경로}
- 심각도: BLOCKER / MAJOR / MINOR
- 규칙 코드: {코드}
- 위반/기대: {내용}
- 수정 방안: {제안}
```

**최대 루프 횟수와 근거:**

| 레이어 | 최대 횟수 | 근거 |
|--------|:--------:|------|
| Domain | 3회 | 비즈니스 규칙이 가장 복잡하고, 리뷰어가 3명(code-reviewer, spec-reviewer, test-designer)으로 피드백 소스가 다양하다. 또한 도메인 오류는 모든 상위 레이어로 전파되므로 여기서 충분히 수정해야 한다. |
| Application | 2회 | 도메인 조합과 포트 정의가 주 작업이며 리뷰어 1명 + test-designer 1명이다. 대부분 구조적/의존성 이슈로 1~2회 내 수렴한다. |
| Adapter | 2회 | DTO 매핑, Controller 라우팅 등 상대적으로 기계적인 작업이다. test-designer 1명의 피드백으로 2회면 충분하다. |

### 2. ESCALATION-REPORT (FIX 루프 초과 시)

**최대 FIX 횟수를 소진해도 해결되지 않으면 자동으로 에스컬레이션된다.**

```
[FIX 루프 초과]
  │
  ▼
reviewer/test-designer → ESCALATION-REPORT 작성
  │
  ▼
project-lead → 분석 후 의사결정 문서 작성
  │
  ├─ 문제 요약: 무엇이 해결되지 않았는가
  ├─ 시도 이력: FIX 루프에서 어떤 수정을 시도했는가
  ├─ 선택지 (2~3개):
  │    옵션 A: {방향} — 장점/단점
  │    옵션 B: {방향} — 장점/단점
  │    옵션 C: {방향} — 장점/단점 (선택)
  ├─ 추천안: project-lead의 권고
  └─ 자유 의견란: 사용자가 선택지 외 직접 방향을 제시할 수 있음
  │
  ▼
사용자 의사결정 (AskUserQuestion)
  │
  ▼
결정 사항을 해당 builder에게 전달 → 수정 재개
journal-recorder → 에스컬레이션 이력 기록
```

**ESCALATION-REPORT 포맷:**
```
- 에스컬레이션 출처: {에이전트명}
- 대상 레이어: {Domain / Application / Adapter}
- FIX 시도 횟수: {N}/{최대}
- 미해결 이슈:
  - 파일: {경로}
  - 심각도: BLOCKER
  - 내용: {구체적 문제}
- 시도한 수정:
  - 1회차: {내용} → 결과: {왜 실패했는지}
  - 2회차: {내용} → 결과: {왜 실패했는지}
  - (3회차: Domain만)
```

### 3. CONVENTION-DISPUTE (컨벤션 이의)

```
구현팀 → CONVENTION-DISPUTE → convention-advocate (조사)
  → 조사 보고서 → convention-guardian (판정)
    → ACCEPTED: ArchUnit 수정 → builder에 전달
    → REJECTED: 사용자 에스컬레이션 (아래 절차)
```

**REJECTED 시 사용자 에스컬레이션:**

```
convention-guardian (REJECTED 판정)
  │
  ▼
사용자에게 보고 (AskUserQuestion):
  ├─ 이의 내용: builder가 제기한 것
  ├─ advocate 조사 결과: 근거와 권고
  ├─ guardian 기각 사유: 왜 거부했는지
  └─ 질문: "기각을 유지할까요, 아니면 컨벤션을 수정할까요?"
  │
  ▼
사용자 판정
  ├─ "기각 유지" → builder가 기존 컨벤션대로 수정
  └─ "컨벤션 수정" → convention-guardian이 ArchUnit 수정
                    → project-lead가 컨벤션 문서 갱신
                    → builder에게 수정된 규칙 전달
```

> **설계 원칙:** convention-guardian은 기술적 판단을, 사용자는 최종 의사결정권을 갖는다.
> guardian이 기각해도 사용자가 뒤집을 수 있으며, 그 결정은 컨벤션팀을 통해 정식 반영된다.

### 4. CLARIFY-REQUEST / RESPONSE (수용기준 명확화)

```
구현팀 → CLARIFY-REQUEST → product-owner → CLARIFY-RESPONSE
최대 1회
```

### 5. AUDIT-REQUEST (산출물 보완)

```
project-manager → AUDIT-REQUEST → 해당 builder
```

---

## journal-recorder 상세 설계

### 트리거 조건

각 팀의 작업 단위가 완료될 때마다 개별 기록한다.

| 트리거 시점 | 기록 대상 |
|------------|----------|
| builder 코드 생성 완료 | 생성한 파일 목록, 설계 의사결정, 대안 비교 |
| reviewer/test-designer 검증 완료 | 발견된 이슈, FIX-REQUEST 발행 여부 |
| FIX 루프 1회 완료 | 수정 내용, 수정 전/후 비교 |
| ESCALATION-REPORT 발생 | 에스컬레이션 전문, 사용자 결정 내용 |
| CONVENTION-DISPUTE 완료 | 이의 내용, 판정 결과, 사용자 개입 여부 |
| 팀 전체 작업 완료 | 팀 매니페스트 요약, 소요 라운드 수 |

### 수집 항목

각 트리거에서 다음 5가지를 추출한다:

1. **의사결정 기록** — 왜 이 방식을 선택했는지, 검토한 대안은 무엇이었는지
2. **AI 활용 기록** — 어떤 에이전트가 어떤 작업을 수행했는지, 프롬프트 의도
3. **진행 상황** — 완료된 항목, 남은 항목, 예상 대비 차이
4. **FIX/에스컬레이션 이력** — 수정 횟수, 어떤 종류의 이슈였는지, 해결 방법
5. **교훈/특이사항** — 예상과 달랐던 점, 향후 주의할 점

### 저장 구조

```
docs/seeds/
├── {날짜}-{팀}-{단계}.yaml          # 개별 시드 파일
│   예: 2026-04-04-domain-builder-complete.yaml
│   예: 2026-04-04-domain-fix-round-2.yaml
│   예: 2026-04-04-escalation-domain.yaml
└── manifest.yaml                    # 시드 인덱스 (시간순)
```

**시드 파일 포맷:**
```yaml
timestamp: 2026-04-04T14:30:00
team: domain
agent: domain-builder
stage: builder-complete  # builder-complete | review-complete | fix-round-N | escalation | dispute | team-complete
summary: "숙소 도메인 Aggregate 생성 완료"
decisions:
  - what: "Property를 Aggregate Root로 설정"
    why: "객실(RoomType)은 숙소 없이 존재할 수 없으므로"
    alternatives: ["RoomType을 독립 Aggregate로 분리"]
ai_usage:
  - agent: domain-builder
    task: "Aggregate, VO, Enum 코드 생성"
    input_context: "ERD + 컨벤션 문서"
progress:
  completed: ["Property", "RoomType", "Amenity"]
  remaining: ["Inventory", "Reservation"]
issues: []
lessons: []
```

### 최종 산출물 생성

시드 데이터가 축적된 후, 사용자 요청 시 다음 문서를 생성한다:

- `docs/progress-journal.md` — 과정 기록서 (시드 종합)
- `docs/ai-usage-log.md` — AI 활용 기록 (에이전트별 작업 이력)

---

## 전체 데이터 흐름

```
product-owner (백로그 + 수용기준)
  │
  ▼
project-lead (구현 가이드 + ADR + 레이어별 컨벤션)
  │
  ├─→ convention-guardian (ArchUnit 작성)
  │
  ├─→ Domain Team [순차]
  │     domain-builder → (병렬) code-reviewer + spec-reviewer → test-designer
  │       ↑ FIX 루프 (최대 3회)
  │       ↑ CONVENTION-DISPUTE → advocate → guardian → (REJECTED 시) 사용자
  │       ↑ FIX 초과 시 → ESCALATION-REPORT → project-lead → 사용자
  │     ◆ journal-recorder: 각 단계 완료 시 시드 기록
  │
  ├─→ Application Team [Domain 완료 후]
  │     application-builder → application-reviewer → application-test-designer
  │       ↑ FIX 루프 (최대 2회)
  │       ↑ FIX 초과 시 → ESCALATION-REPORT → project-lead → 사용자
  │     ◆ journal-recorder: 각 단계 완료 시 시드 기록
  │
  ├─→ Adapter-out Team ─┐
  │     persistence-     │ [Application 완료 후, 병렬 실행]
  │     mysql-builder    │
  │     → test-designer  │
  │       ↑ FIX (최대 2회)│
  │                      │
  └─→ Adapter-in Team ──┘
        rest-api-builder
        → test-designer
          ↑ FIX (최대 2회)

  ◆ journal-recorder: Adapter 각 팀 완료 시 시드 기록

project-manager ← 모든 팀의 매니페스트
  → AUDIT-REQUEST (미충족 시)
  ◆ journal-recorder: 최종 감사 결과 기록
```

---

## 레이어 간 의존 방향

```
Domain Team (1단계 — 먼저)
  ↓ 도메인 코드 완료
Application Team (2단계)
  ↓ Port 인터페이스 + UseCase 정의
  ├→ Adapter-out Team (3단계 — Port 구현)   ← 병렬 실행 가능
  └→ Adapter-in Team  (3단계 — UseCase 호출) ← 병렬 실행 가능
```

- Application builder는 Domain 코드가 있어야 작업 가능.
- Adapter builder들은 Application의 Port/UseCase가 있어야 작업 가능.
- **Adapter-out과 Adapter-in은 서로 의존하지 않으므로 병렬 실행한다.**

---

## 핵심 산출물 흐름

각 레이어가 다음 레이어에 넘기는 핵심 산출물 요약. 상세 포맷은 각 에이전트 설정 파일 참조.

```
product-owner
  ──→ 백로그 아이템 + 수용기준 (AC)
       │
project-lead
  ──→ 구현 가이드 (레이어별 지시사항)
  ──→ ADR (아키텍처 의사결정 기록)
  ──→ 레이어별 컨벤션 문서
       │
convention-guardian
  ──→ ArchUnit 테스트 코드
       │
Domain Team
  ──→ 도메인 모델 코드 (Aggregate, VO, Enum, ID, Domain Service)
  ──→ 도메인 테스트 코드
  ──→ 리뷰 리포트 (code-reviewer, spec-reviewer)
       │
Application Team
  ──→ UseCase 인터페이스 (Inbound Port)
  ──→ Outbound Port 인터페이스
  ──→ Application Service 구현체
  ──→ Application 테스트 코드
       │
Adapter-out Team
  ──→ JPA Entity + Repository + Flyway 마이그레이션
  ──→ Domain↔Entity Mapper
  ──→ 영속성 통합 테스트 코드
       │
Adapter-in Team
  ──→ REST Controller + Request/Response DTO
  ──→ Swagger 설정
  ──→ GlobalExceptionHandler
  ──→ API 테스트 코드
       │
project-manager
  ──→ 산출물 감사 리포트 (수용기준 충족 여부)
  ──→ AUDIT-REQUEST (미충족 항목)
       │
journal-recorder
  ──→ 시드 데이터 (docs/seeds/*.yaml)
  ──→ 과정 기록서, AI 활용 기록 (최종 문서)
```

---

## 팀 확장 가이드

새로운 Adapter가 필요할 때:

1. `.claude/agents/{adapter-type}-builder.md` 생성 (기존 builder를 템플릿으로)
2. `.claude/agents/{adapter-type}-test-designer.md` 생성
3. `.claude/agents/{adapter-type}-reviewer.md` 생성 (해당 adapter 유형에 특화된 리뷰 기준 정의)
4. project-lead가 `docs/design/{adapter-type}-convention.md` 작성
5. convention-guardian이 해당 ArchUnit 테스트 추가
6. 기존 팀과 동일한 FIX-REQUEST / ESCALATION / 매니페스트 프로토콜 적용

### 확장 후보

**Adapter-in (외부 → 내부):**
- `admin-api-builder` — 백오피스 전용 API
- `open-api-builder` — 외부 공개 API
- `mq-consumer-builder` — 메시지 큐 컨슈머 (SQS, Kafka)
- `scheduler-builder` — 스케줄러 (배치)

**Adapter-out (내부 → 외부):**
- `persistence-redis-builder` — Redis 캐시, 분산 락
- `external-api-client-builder` — 외부 API 클라이언트
- `sqs-client-builder` — AWS SQS 메시지 발행
- `kafka-client-builder` — Kafka 프로듀서

---

## 도구 권한 매트릭스

| 에이전트 | Read | Write | Glob | Grep | Bash |
|---------|:----:|:-----:|:----:|:----:|:----:|
| product-owner | O | O | O | O | **X** |
| project-lead | O | O | O | O | O |
| project-manager | O | **X** | O | O | O |
| convention-guardian | O | O | O | O | O |
| convention-advocate | O | **X** | O | O | O |
| domain-builder | O | O | O | O | O |
| domain-code-reviewer | O | **X** | O | O | O |
| domain-spec-reviewer | O | **X** | O | O | **X** |
| domain-test-designer | O | O | O | O | O |
| application-builder | O | O | O | O | O |
| application-reviewer | O | **X** | O | O | O |
| application-test-designer | O | O | O | O | O |
| rest-api-builder | O | O | O | O | O |
| rest-api-test-designer | O | O | O | O | O |
| persistence-mysql-builder | O | O | O | O | O |
| persistence-mysql-test-designer | O | O | O | O | O |
| journal-recorder | O | O | O | O | **X** |
| agent-recruiter | O | O | O | O | **X** |
| pipeline-orchestrator | O | O | O | O | O |

**설계 원칙:**
- **Write 없는 에이전트** (5개): PM, advocate, code-reviewer, spec-reviewer, app-reviewer — 검증/리뷰 전용, 코드 수정 불가
- **Bash 없는 에이전트** (4개): PO, spec-reviewer, journal-recorder, agent-recruiter — 코드 빌드/실행 불필요
- **ArchUnit 수정 권한**: convention-guardian만 보유
- **계획된 reviewer** (2개): rest-api-reviewer, persistence-mysql-reviewer — Write 없음, Bash 있음 (기존 reviewer와 동일 패턴)
- **pipeline-orchestrator**: pipeline 스킬에서만 호출. Agent 도구 사용 가능

---

## 오케스트레이터

### 구조: Skill + Agent 조합

| 구성 요소 | 파일 | 역할 |
|----------|------|------|
| **pipeline 스킬** | `.claude/skills/pipeline/SKILL.md` | 사용자 진입점. 커맨드 파싱, 사용자 인터랙션 중계 |
| **pipeline-orchestrator 에이전트** | `.claude/agents/pipeline-orchestrator.md` | 실행 엔진. 에이전트 호출 순서, FIX 루프, 상태 관리 |

### 지원 커맨드

| 커맨드 | 설명 |
|--------|------|
| `/pipeline run STORY-001` | 전체 파이프라인 실행 |
| `/pipeline layer domain STORY-001` | 특정 레이어만 실행 |
| `/pipeline step build domain STORY-001` | 특정 단계만 실행 |
| `/pipeline audit STORY-001` | PM 산출물 검증 |
| `/pipeline status` | 현재 상태 확인 |
| `/pipeline resume STORY-001` | 중단된 파이프라인 재개 |

### 실행 순서

```
Phase 0: 전제조건 확인 (백로그, ERD, 컨벤션)
Phase 1: Domain Team (builder → [병렬] reviewers → test-designer → [FIX 루프])
Phase 2: Application Team (builder → reviewer → test-designer → [FIX 루프])
Phase 3: Adapter Teams [병렬] (adapter-out + adapter-in)
Phase 4: 산출물 감사 (project-manager)
Phase 5: 완료 보고

Phase E: ESCALATION (FIX 초과 → PL 분석 → 사용자 결정 → 재개)
Phase D: CONVENTION-DISPUTE (advocate → guardian → 필요 시 사용자 결정)
```

### 상태 관리

`docs/pipeline/STORY-{번호}-state.yaml`에 파이프라인 상태를 파일로 관리한다.
세션이 끊어져도 `/pipeline resume`로 재개 가능.

### Skill ↔ Agent 책임 경계

| 책임 | Skill | Agent |
|------|:-----:|:-----:|
| 커맨드 파싱 | O | |
| 사용자 확인/질문 | O | |
| 에이전트 호출 순서 | | O |
| FIX 루프 추적 | | O |
| 매니페스트 수집/전달 | | O |
| state.yaml 관리 | | O |
| journal-recorder 트리거 | | O |
| ESCALATION 사용자 전달 | O | |
| 완료 요약 출력 | O | |
