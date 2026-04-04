# 부트스트랩 핸드오프 문서

> 이 문서는 에이전트 하네스 구축 과정의 현재 진행 상태와 다음 단계를 정리한 핸드오프 문서다.
> 새 Claude 세션에서 이 문서를 읽고 바로 이어서 작업할 수 있다.

---

## 현재까지 완료된 것

### 1. 에이전트 조직 구축 완료 (19개 에이전트 + 3개 스킬)

`.claude/agents/` 에 19개 에이전트 파일이 생성되어 있다:

**Strategic (3):** product-owner, project-lead, project-manager
**Convention (2):** convention-guardian, convention-advocate
**Domain Team (4):** domain-builder, domain-code-reviewer, domain-spec-reviewer, domain-test-designer
**Application Team (3):** application-builder, application-reviewer, application-test-designer
**Adapter-in (2):** rest-api-builder, rest-api-test-designer
**Adapter-out (2):** persistence-mysql-builder, persistence-mysql-test-designer
**Cross-cutting (1):** journal-recorder
**Meta (1):** agent-recruiter
**Orchestrator (1):** pipeline-orchestrator

`.claude/skills/` 에 3개 스킬:
- `/journal` — 과정 기록 관리
- `/pipeline` — 파이프라인 실행
- `/ota-research` — OTA 크롤링 리서치

### 2. 설계 문서 완료

- `docs/design/agent-pipeline.md` — 전체 파이프라인 기술 설계서 (조직도, 피드백 루프, 도구 권한 매트릭스, 데이터 흐름)
- `docs/design/agent-harness-guide.md` — 종합 가이드 (왜 만들었는지, 어떻게 쓰는지, 용어 정리)
- `docs/design/domain-convention.md` — 도메인 레이어 컨벤션 (기존)

### 3. 백로그 생성 완료 (Step 1)

`docs/backlog.md` 가 product-owner에 의해 생성됨:
- 11개 Epic, 25개 Story (P0 18개, P1 9개, P2 2개)
- 각 Story에 검증 가능한 수용기준(AC), 관련 레이어, 의존성, 담당 팀, 구현 상태 표기
- 의존성 그래프 포함

### 4. 기존 코드 상태

**구현된 것:**
- `domain/` 모듈 — 41개 Java 파일 (accommodation 27개, pricing 9개, location 4개, partner 1개)
- `domain/src/test/` — DomainLayerArchTest.java 1개
- Gradle 멀티모듈 구조 (domain, application, adapter-in/rest-api, adapter-out/persistence-mysql, adapter-out/persistence-redis)

**미구현:**
- `application/` — 빈 구조 (코드 없음)
- `adapter-in/rest-api/` — 빈 구조 (코드 없음)
- `adapter-out/persistence-mysql/` — 빈 구조 (코드 없음)
- `adapter-out/persistence-redis/` — 빈 구조 (코드 없음)
- inventory, reservation, supplier 도메인 코드 없음

### 5. 기존 설계/리서치 자산

- `docs/erDiagram.md` — ERD v2 (6개 바운디드 컨텍스트)
- `docs/research/` — OTA 리서치 3종 (플랫폼 분석, 크로스 비교, 도메인 검증)
- `docs/seeds/` — 시드 데이터 7개 (의사결정, AI활용, 진행, 동시성 설계, 캐싱 설계, 시간 컨벤션, 도메인 피드백)
- `.claude/skills/ota-research/references/ota-domain-guide.md` — OTA 업계 도메인 지식

---

## 다음에 해야 할 것 (부트스트랩 Step 2~5)

### Step 2: project-lead 실행 (지금 해야 할 것)

project-lead 에이전트를 호출하여 아래 산출물을 생성한다:

1. **Application 컨벤션** — `docs/design/application-convention.md`
   - UseCase 인터페이스 규칙 (단일 메서드, Command/Query 분리)
   - Port 규칙 (Outbound Port는 Domain 객체만)
   - Service 규칙 (@Transactional 경계, 비즈니스 로직 Domain 위임)
   - 규칙 코드 형식: `APP-UC-001`, `APP-PORT-001`, `APP-SVC-001`

2. **Persistence 컨벤션** — `docs/design/persistence-convention.md`
   - Entity 규칙 (*Entity 접미사, 비즈니스 로직 금지)
   - Mapper 규칙 (toDomain/toEntity)
   - Adapter 규칙 (Port 구현체)
   - Flyway 규칙 (V{번호}__{설명}.sql)
   - 규칙 코드 형식: `PER-ENT-001`, `PER-MAP-001`, `PER-ADP-001`

3. **API 컨벤션** — `docs/design/api-convention.md`
   - Controller 규칙 (UseCase만 호출)
   - DTO 규칙 (record, toCommand(), from())
   - 응답 포맷 (ApiResponse<T>)
   - 예외 처리 (GlobalExceptionHandler)
   - 규칙 코드 형식: `API-CTL-001`, `API-DTO-001`, `API-RES-001`

4. **구현 가이드** — 백로그 P0 스토리별 "어떤 레이어에서 어떤 순서로" 안내

**호출 방법:**
```
project-lead 에이전트를 호출하세요.
에이전트 정의: .claude/agents/project-lead.md

작업:
1. docs/backlog.md 를 읽고 전체 구현 범위를 파악
2. docs/design/domain-convention.md 형식을 참조하여 동일한 구조로 나머지 3개 컨벤션 작성
3. P0 스토리들의 구현 가이드 작성

참조 문서:
- .claude/CLAUDE.md
- docs/backlog.md
- docs/design/domain-convention.md (형식 참조)
- docs/erDiagram.md
- docs/seeds/ (기존 설계 결정)
- build.gradle.kts, settings.gradle.kts (모듈 구조)
```

### Step 3: convention-guardian 실행

project-lead가 컨벤션을 만든 후, convention-guardian을 호출하여 ArchUnit 테스트를 작성한다:

```
convention-guardian 에이전트를 호출하세요.
에이전트 정의: .claude/agents/convention-guardian.md

작업:
1. docs/design/application-convention.md 의 규칙을 ArchUnit 테스트로 변환
2. docs/design/persistence-convention.md 의 규칙을 ArchUnit 테스트로 변환
3. docs/design/api-convention.md 의 규칙을 ArchUnit 테스트로 변환
4. 기존 DomainLayerArchTest.java 확인 후 누락된 규칙 보강

생성 파일:
- application/src/test/java/.../ApplicationLayerArchTest.java
- adapter-out/persistence-mysql/src/test/java/.../PersistenceArchTest.java
- adapter-in/rest-api/src/test/java/.../ApiArchTest.java
```

### Step 4: 기존 도메인 코드 온보딩

기존 도메인 코드(41개 파일)가 파이프라인 밖에서 만들어졌으므로, 리뷰와 테스트를 실행한다:

```
순서:
1. domain-code-reviewer → 기존 accommodation, pricing, location, partner 코드 리뷰
2. domain-spec-reviewer → 비즈니스 규칙 검증
3. domain-test-designer → 기존 도메인 모델의 테스트 작성
4. FIX-REQUEST가 나오면 domain-builder로 수정

대상 경로:
- domain/src/main/java/com/ryuqq/otatoy/domain/accommodation/
- domain/src/main/java/com/ryuqq/otatoy/domain/pricing/
- domain/src/main/java/com/ryuqq/otatoy/domain/location/
- domain/src/main/java/com/ryuqq/otatoy/domain/partner/
- domain/src/main/java/com/ryuqq/otatoy/domain/common/
```

### Step 5: 정상 파이프라인 가동

Step 2~4가 완료되면 `/pipeline` 스킬로 정상 파이프라인을 실행할 수 있다:

```
/pipeline run STORY-101    → 숙소 도메인 모델 완성 (부분 완료 → 완료)
/pipeline run STORY-301    → 예약/재고 도메인 모델 (신규)
/pipeline run STORY-401    → Supplier 도메인 모델 (신규)
/pipeline run STORY-102    → 숙소 등록 UseCase (Application 레이어 첫 진입)
...
```

---

## 핵심 참조 파일

| 파일 | 용도 |
|------|------|
| `.claude/CLAUDE.md` | 프로젝트 전체 설정, 기술 스택, 아키텍처 원칙 |
| `docs/design/agent-pipeline.md` | 에이전트 파이프라인 기술 설계서 |
| `docs/design/agent-harness-guide.md` | 에이전트 하네스 종합 가이드 |
| `docs/design/domain-convention.md` | 도메인 레이어 컨벤션 (형식 참조용) |
| `docs/backlog.md` | 백로그 (P0/P1/P2, 수용기준, 의존성) |
| `docs/erDiagram.md` | ERD v2 |
| `.claude/agents/*.md` | 각 에이전트 정의 파일 |
| `.claude/skills/pipeline/SKILL.md` | 파이프라인 스킬 |

---

## 즉시 실행 가능한 명령

새 세션에서 이 문서를 읽은 후 바로 실행:

```
이 문서(docs/bootstrap-handoff.md)를 읽었으면, Step 2부터 시작하세요.
project-lead 에이전트(.claude/agents/project-lead.md)를 호출하여
Application/Persistence/API 컨벤션 문서를 생성하세요.
```
