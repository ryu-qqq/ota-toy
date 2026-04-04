# AI 에이전트 하네스 가이드

## 이 문서는 무엇인가

이 프로젝트는 단일 AI에게 모든 작업을 맡기는 대신, **역할별 전문 AI 에이전트들이 실제 개발 조직처럼 협업하는 구조**를 채택하고 있다. 이 문서는 그 구조 전체를 설명한다.

---

## 왜 이런 구조를 만들었는가

### 문제: 단일 AI의 한계

AI에게 "숙소 도메인 만들어줘"라고 하면 코드를 생성한다. 하지만:
- **코드 컨벤션을 일관되게 지키는가?** — 생성할 때마다 스타일이 달라질 수 있다
- **비즈니스 규칙이 빠짐없이 반영되었는가?** — 코드를 만든 AI가 스스로 비즈니스를 검증하면 확증 편향이 생긴다
- **테스트가 도메인의 진짜 규칙을 검증하는가?** — 만든 사람이 테스트도 만들면 자기가 만든 대로만 테스트한다
- **아키텍처 레이어 간 의존성이 올바른가?** — 한 번에 전체를 만들면 경계가 무너지기 쉽다

### 해결: 역할 분리 + 상호 검증

실제 개발 조직에서는 **개발자가 만들고 → 리뷰어가 검토하고 → QA가 테스트하고 → PM이 산출물을 확인**한다. 같은 원리를 AI 에이전트에 적용한다.

각 에이전트는:
1. **하나의 역할만** 수행한다 (만드는 놈, 리뷰하는 놈, 테스트하는 놈이 다르다)
2. **제한된 도구만** 사용할 수 있다 (리뷰어는 코드를 수정할 수 없다)
3. **표준화된 형식으로** 산출물을 주고받는다 (에이전트 간 소통이 일관된다)
4. **피드백 루프로** 품질을 수렴시킨다 (리뷰 → 수정 → 재리뷰 반복)

---

## 전체 조직 구조

```
┌─────────────────── 전략 ────────────────────┐
│  product-owner   project-lead   project-manager │
│  "무엇을 만들까"  "어떻게 만들까"  "잘 만들었나"   │
└──────────┬──────────┬──────────────┬──────────┘
           │          │              │
      ┌────▼──────────▼────┐        │
      │   컨벤션 거버넌스    │        │
      │ guardian  advocate │        │
      │ "규칙을 지켜라"     │        │
      └────────┬───────────┘        │
               │                    │
  ┌────────────▼────────────────────▼──────┐
  │              구현 팀들                   │
  │                                        │
  │  도메인팀 → Application팀 → Adapter팀들  │
  │  (4명)      (3명)         (각 2명)      │
  └────────────────────────────────────────┘
               │
  ┌────────────▼──────────┐
  │  횡단 관심사            │
  │  journal-recorder     │
  │  agent-recruiter      │
  │  pipeline-orchestrator│
  └───────────────────────┘
```

총 19개 에이전트 + 3개 스킬으로 구성된다.

---

## 에이전트 상세

### 전략 레이어 — "방향을 잡는 사람들"

이 레이어는 **무엇을 만들 것인가, 어떻게 만들 것인가, 제대로 만들었는가**를 결정한다.

#### product-owner (서비스 기획자)

**왜 필요한가:** 요구사항 문서는 "숙소 검색 및 요금 조회"처럼 추상적이다. 이걸 "POST /api/v1/properties로 숙소 생성 가능하고, 필수 필드 누락 시 400 에러를 반환한다"처럼 **검증 가능한 수용기준**으로 분해해야 구현팀이 정확히 무엇을 만들지 안다.

**하는 일:**
- 요구사항을 Epic → Story로 분해
- 각 Story에 우선순위 부여 (P0 필수, P1 확장-높음, P2 확장-낮음)
- 검증 가능한 수용기준(AC) 정의
- 결과물: `docs/backlog.md`

**도구 권한:** Read, Write, Glob, Grep (Bash 없음 — 코드 실행이 아니라 기획이 역할)

**사용 시점:** 프로젝트 시작 시 가장 먼저 실행. 백로그가 없으면 다른 에이전트가 움직일 수 없다.

---

#### project-lead (시니어 아키텍트)

**왜 필요한가:** "숙소를 등록한다"는 스토리가 있을 때, 이걸 **어떤 레이어에서 어떤 순서로 어떤 컨벤션을 적용하여** 구현할지 결정해야 한다. 또한 "왜 Hexagonal인가, 왜 MySQL인가" 같은 아키텍처 의사결정을 문서로 남겨야 한다.

**하는 일:**
- ADR(Architecture Decision Record) 작성 — 결정, 대안, 트레이드오프 기록
- 레이어별 컨벤션 문서 작성 (Application, Persistence, API 각각)
- 백로그 아이템별 구현 가이드 작성 ("Domain에서 이것 → Application에서 이것 → ...")
- 결과물: `docs/design/{layer}-convention.md`, `docs/design/adr/`

**도구 권한:** 전체 (Read, Write, Glob, Grep, Bash)

**사용 시점:** product-owner가 백로그를 만든 후. 컨벤션이 없으면 builder들이 일관된 코드를 만들 수 없다.

---

#### project-manager (QA/PM)

**왜 필요한가:** 코드를 만들고, 리뷰하고, 테스트해도 **프로젝트가 요구하는 산출물이 실제로 다 갖춰져 있는지** 최종 확인하는 사람이 필요하다. "설계 문서 있나? ERD 있나? 테스트 통과하나? API 문서 자동화 되었나?"를 체크한다.

**하는 일:**
- 백로그 수용기준 대비 현재 상태 검증 (PASS/FAIL/PARTIAL)
- 프로젝트 산출물 체크리스트 확인 (설계 문서, 테스트, 과정 기록서, API 문서 등)
- 위험 사항 식별 및 우선 작업 제안
- 미충족 항목에 대해 해당 builder에게 AUDIT-REQUEST 발행
- 결과물: 산출물 검증 보고서

**도구 권한:** Read, Glob, Grep, Bash (**Write 없음** — 검증만 하고 직접 작성/수정하지 않는다)

**사용 시점:** 각 레이어 완료 후 또는 최종 제출 전.

---

### 컨벤션 레이어 — "규칙을 지키게 하는 사람들"

이 레이어는 **모든 에이전트가 동일한 코드 규칙을 따르도록 강제**한다. AI가 코드를 생성해도 컨벤션을 벗어나면 ArchUnit 테스트가 깨진다.

#### convention-guardian (코드 품질 수호자)

**왜 필요한가:** 컨벤션을 문서로만 정의하면 지켜지지 않는다. ArchUnit 테스트로 **자동 강제**해야 한다. 이 에이전트가 ArchUnit 테스트를 소유하고 수정할 수 있는 **유일한** 존재다.

**하는 일:**
- PL이 작성한 컨벤션 문서를 ArchUnit 테스트로 변환
- 컨벤션 이의에 대한 최종 판정 (ACCEPTED / REJECTED)
- ArchUnit 테스트 유지보수
- 소유 파일: `*ArchTest.java` (Domain, Application, API, Persistence 각각)

**도구 권한:** 전체

**핵심 원칙:** 다른 에이전트는 ArchUnit 테스트 파일을 수정할 수 없다. builder가 ArchUnit에 걸리면 자기 코드를 고치거나, convention-advocate를 통해 이의를 제기해야 한다.

---

#### convention-advocate (공정한 조사관)

**왜 필요한가:** 컨벤션이 항상 옳은 것은 아니다. builder가 "이 규칙이 현실적이지 않다"고 느낄 때, 감정이 아닌 **근거 기반으로** 해당 이의가 타당한지 조사하는 역할이 필요하다.

**하는 일:**
- 구현팀의 CONVENTION-DISPUTE(컨벤션 이의) 수신
- 업계 사례, 현 프로젝트 맥락, 코드 영향 범위 조사
- 타당성 판단 후 guardian에게 보고서 전달

**도구 권한:** Read, Glob, Grep, Bash (**Write 없음** — ArchUnit을 직접 수정할 수 없다. 보고만 한다)

**피드백 흐름:**
```
builder가 이의 제기 → advocate가 조사 → guardian이 판정
  → ACCEPTED: ArchUnit 수정
  → REJECTED: 사용자가 최종 판단 (기각 유지 vs 컨벤션 수정)
```

---

### 도메인 팀 — "비즈니스를 코드로 표현하는 사람들"

이 팀은 **순수 Java로 비즈니스 규칙을 표현**한다. Spring, JPA 등 외부 프레임워크 의존이 없다.

#### domain-builder (구현자)

**왜 필요한가:** ERD와 컨벤션을 보고 Aggregate, VO, Enum, ID, ErrorCode, Exception을 생성하는 전문가가 필요하다.

**하는 일:**
- ERD에서 엔티티의 필드와 관계를 확인
- 컨벤션에 맞는 도메인 코드 생성 (forNew/reconstitute, ID VO, Value Object 등)
- 컴파일 확인 (`./gradlew :domain:compileJava`)
- 생성 결과 매니페스트 출력 (생성된 파일 목록, 유형, 특이사항)

**도구 권한:** 전체

**핵심:** "만드는 것만" 한다. 검증과 테스트는 다른 에이전트에게 넘긴다.

---

#### domain-code-reviewer (시니어 개발자)

**왜 필요한가:** builder가 만든 코드가 컨벤션을 지키는지 **다른 관점**에서 검증해야 한다. 만든 사람이 스스로 리뷰하면 자기 코드의 문제를 못 본다.

**하는 일:**
- ArchUnit 테스트 실행 (`./gradlew :domain:test`)
- 9개 체크리스트(C-1~C-9)로 수동 검증 (Aggregate 구조, ID VO, VO, Enum, ErrorCode, 시간, 의존성, ERD 일치, 패키지)
- 위반 항목에 심각도 부여 (BLOCKER/MAJOR/MINOR)
- FIX-REQUEST 발행 (builder에게 수정 요청)

**도구 권한:** Read, Glob, Grep, Bash (**Write 없음** — 직접 코드를 고치지 않는다)

---

#### domain-spec-reviewer (기획자/PM 관점)

**왜 필요한가:** code-reviewer는 "코드가 맞게 짜였는가"를 보지만, "**비즈니스 규칙이 빠짐없이 표현되었는가**"는 다른 관점이다. "환불불가 요금의 예약을 취소하면 어떻게 되는가?", "기본 인원이 최대 인원보다 큰 객실이 만들어질 수 있는가?" 같은 질문을 던진다.

**하는 일:**
- OTA 리서치 데이터(`docs/research/`)를 기반으로 비즈니스 규칙 검증
- 5개 관점(S-1~S-5)으로 검증: 규칙 표현, 관계, 누락, 자기 보호, 흐름 가능성
- 규칙 소스 명시 (리서치 / ERD / 업계 공통 / 추론)
- ⚠️/❌ 항목을 test-designer에게 테스트 시나리오로 전달

**도구 권한:** Read, Glob, Grep (**Write도 Bash도 없음** — 순수하게 읽고 판단만)

---

#### domain-test-designer (QA 엔지니어)

**왜 필요한가:** "이 도메인이 이런 걸 보장한다"를 **코드로 증명**해야 한다. 테스트가 실패하면 그건 도메인 코드가 비즈니스 규칙을 표현하지 못하고 있다는 신호다.

**하는 일:**
- 6개 카테고리(T-1~T-6)별 시나리오 설계: 생성, 상태 전이, 불변식, 로직, VO, 관계
- spec-reviewer의 ⚠️/❌ 항목을 반드시 테스트로 변환
- 순수 Java 단위 테스트 (Spring Context, DB, Mock 없음)
- 테스트 실패 시 **테스트를 고치지 않고** builder에게 FIX-REQUEST

**도구 권한:** 전체

**핵심 원칙:** 테스트가 실패하면 도메인 코드가 고쳐져야 한다. 테스트를 고치는 것은 금지.

---

### Application 팀 — "유스케이스를 조합하는 사람들"

이 팀은 **Domain 객체를 조합하여 유스케이스를 완성**한다. UseCase 인터페이스, Port, Service를 담당한다.

#### application-builder

**왜 필요한가:** 도메인 객체가 있어도 "숙소를 등록한다"는 **유스케이스 흐름**은 Application 레이어에서 조합해야 한다. Port를 정의하여 Adapter와의 경계를 만든다.

**하는 일:**
- UseCase 인터페이스 (Inbound Port) 정의
- Outbound Port (Repository, Cache 등) 정의
- Application Service (UseCase 구현체) 작성
- `@Transactional` 경계 설정, 이벤트 발행

**도구 권한:** 전체

---

#### application-reviewer

**왜 필요한가:** Application 레이어에 비즈니스 로직이 스며들면 Domain의 의미가 퇴색된다. "Service에서 if문으로 비즈니스 판단을 하고 있지 않은가?", "Adapter를 직접 import하고 있지 않은가?"를 검증한다.

**하는 일:**
- 5개 체크리스트(APP-1~APP-5): UseCase 구조, Service 구조, Port 구조, 의존성 방향, 이벤트

**도구 권한:** Read, Glob, Grep, Bash (Write 없음)

---

#### application-test-designer

**왜 필요한가:** UseCase가 Domain을 올바르게 조합하는지, Port를 올바른 순서로 호출하는지, 이벤트가 발행되는지를 증명해야 한다.

**하는 일:**
- 5개 카테고리(AT-1~AT-5): 정상 흐름, 실패 흐름, Port 호출, 이벤트, 트랜잭션
- Port를 Mock으로 대체한 단위 테스트

**도구 권한:** 전체

---

### Adapter-in 팀 — "외부 요청을 받는 사람들"

이 팀은 **외부에서 들어오는 요청을 처리**한다. 현재는 REST API만 구현되어 있으며, Admin API, MQ Consumer, Scheduler 등으로 확장 가능하다.

#### rest-api-builder

**왜 필요한가:** Controller는 UseCase만 호출하고, 일관된 응답 포맷과 에러 처리를 제공해야 한다. API 문서 자동화(Swagger)도 이 에이전트의 책임이다.

**하는 일:**
- REST Controller + Request/Response DTO(record) 생성
- `ApiResponse<T>` 공통 응답 포맷 적용
- `GlobalExceptionHandler` (DomainException → HTTP 상태 매핑)
- Swagger/SpringDoc 어노테이션

**도구 권한:** 전체

---

#### rest-api-test-designer

**왜 필요한가:** "정상 요청에 200이 오는가?", "필수 필드 누락에 400이 오는가?", "DomainException이 적절한 HTTP 상태로 변환되는가?"를 MockMvc로 검증해야 한다.

**도구 권한:** 전체

---

### Adapter-out 팀 — "외부로 나가는 사람들"

이 팀은 **내부에서 외부로 나가는 통신을 처리**한다. 현재는 MySQL 영속성만 구현되어 있으며, Redis, 외부 API Client, MQ Producer 등으로 확장 가능하다.

#### persistence-mysql-builder

**왜 필요한가:** Application의 Outbound Port를 **JPA로 구현**해야 한다. Domain 객체와 JPA Entity 사이의 매핑, Flyway 마이그레이션, QueryDSL 커스텀 쿼리를 담당한다.

**하는 일:**
- JPA Entity (`@Entity` + 비즈니스 로직 금지)
- Mapper (Domain ↔ Entity 변환)
- Adapter (Port 인터페이스 구현체)
- Flyway 마이그레이션 SQL
- Spring Data JPA Repository + QueryDSL

**도구 권한:** 전체

---

#### persistence-mysql-test-designer

**왜 필요한가:** H2로는 동시성 제어(SELECT FOR UPDATE)를 검증할 수 없다. **Testcontainers MySQL**로 실제 DB에서 CRUD 정합성과 동시성을 테스트해야 한다.

**하는 일:**
- 5개 카테고리(PT-1~PT-5): 매핑 정합성, CRUD, QueryDSL, Flyway, **동시성**(핵심)
- ExecutorService로 N개 스레드 동시 예약 → 재고 정합성 검증

**도구 권한:** 전체

---

### 횡단 관심사 — "모두를 지원하는 사람들"

#### journal-recorder (과정 기록 전문가)

**왜 필요한가:** 이 프로젝트는 결과물뿐 아니라 과정을 중시한다. 각 에이전트가 작업할 때마다 **의사결정, AI 활용, 진행상황을 자동으로 기록**해야 나중에 과정 기록서와 AI 활용 기록을 생성할 수 있다.

**하는 일:**
- 모든 에이전트의 산출물에서 의사결정/AI활용/진행을 자동 추출
- `docs/seeds/` 에 시드 데이터로 저장
- 최종 문서 생성은 journal 스킬(`/journal`)이 담당

**도구 권한:** Read, Write, Glob, Grep (Bash 없음)

**journal 스킬과의 차이:**
| | journal 스킬 | journal-recorder |
|--|-------------|-----------------|
| 트리거 | 사용자가 `/journal` 호출 | 에이전트 작업 완료 시 자동 |
| 입력 | 사용자와의 대화 | 에이전트 산출물 |
| 최종 문서 생성 | O | X (시드만 저장) |

---

#### agent-recruiter (HR/조직설계)

**왜 필요한가:** 프로젝트가 진행되면서 새로운 Adapter 팀이 필요해질 수 있다(Redis, Kafka 등). 기존 조직 구조, 피드백 루프 프로토콜, 네이밍 컨벤션을 **완벽히 이해한 상태로** 새 에이전트를 "채용"하고 기존 팀들과 연결해주는 역할이 필요하다.

**하는 일:**
- 새 에이전트 필요성 판단 (기존 에이전트로 커버 가능한지 먼저 확인)
- 에이전트 파일 생성 + 관련 에이전트 관계 섹션 갱신 + 파이프라인 문서 갱신
- 팀 구성 템플릿: 최소 2명(builder + test-designer), 표준 3명(+ reviewer), 확장 4명(+ spec-reviewer)

**도구 권한:** Read, Write, Glob, Grep (Bash 없음)

**사용 예시:** "Redis 캐시 팀 채용해줘" → `persistence-redis-builder.md` + `persistence-redis-test-designer.md` 생성 + 관련 에이전트 갱신

---

#### pipeline-orchestrator (CI/CD 엔진)

**왜 필요한가:** 19개 에이전트를 일일이 수동으로 호출하는 것은 비현실적이다. **정해진 순서로 에이전트를 호출하고, FIX 루프를 관리하고, 에스컬레이션을 처리**하는 자동화 엔진이 필요하다.

**하는 일:**
- 레이어 순서 제어 (Domain → Application → Adapter)
- 병렬 실행 관리 (code-reviewer + spec-reviewer 동시, adapter-in + adapter-out 동시)
- FIX 루프 카운트 추적 (최대 횟수 초과 시 ESCALATION)
- 매니페스트 수집/저장/전달
- journal-recorder 트리거
- 상태 파일(`docs/pipeline/STORY-{번호}-state.yaml`) 관리

**도구 권한:** 전체 + Agent 도구

**핵심 원칙:** 코드 내용에 개입하지 않는다. 매니페스트/보고서를 있는 그대로 전달만 한다.

---

## 스킬 (사용자 진입점)

스킬은 사용자가 **슬래시 커맨드로 호출**하는 자동화 도구다.

### /pipeline — 파이프라인 실행

| 커맨드 | 설명 |
|--------|------|
| `/pipeline run STORY-001` | 전체 파이프라인 실행 (Domain → Application → Adapter → 감사) |
| `/pipeline layer domain STORY-001` | 도메인 팀만 실행 |
| `/pipeline step build domain STORY-001` | 도메인 빌드 단계만 실행 |
| `/pipeline audit STORY-001` | PM 산출물 검증만 실행 |
| `/pipeline status` | 현재 상태 확인 |
| `/pipeline resume STORY-001` | 중단된 파이프라인 재개 |

### /journal — 과정 기록 관리

| 모드 | 설명 |
|------|------|
| 기록 모드 | "이거 기록해줘" → 의사결정/AI활용/진행을 시드로 저장 |
| 생성 모드 | "과정 기록서 생성해줘" → 모든 시드를 최종 문서로 변환 |

### /ota-research — OTA 플랫폼 리서치

실제 OTA 플랫폼을 Playwright로 크롤링하여 도메인 모델의 근거를 수집한다.

---

## 피드백 루프 — "에이전트끼리 소통하는 방법"

### FIX-REQUEST / FIX-RESPONSE (구현팀 내부)

리뷰어나 테스터가 문제를 발견하면 builder에게 **표준 형식**으로 수정을 요청한다.

```
┌────────────┐     FIX-REQUEST     ┌────────────┐
│  reviewer  │ ──────────────────→ │  builder   │
│ or tester  │ ←────────────────── │            │
└────────────┘     FIX-RESPONSE    └────────────┘
```

최대 루프 횟수: Domain 3회, Application 2회, Adapter 2회. 초과 시 자동 ESCALATION.

### ESCALATION (FIX 루프 초과)

```
FIX 루프 초과 → project-lead 분석 → 선택지 제시 → 사용자 의사결정 → 재개
```

사용자가 최종 방향을 결정한다. 결정 내용은 journal-recorder가 자동 기록.

### CONVENTION-DISPUTE (컨벤션 이의)

```
builder 이의 제기 → advocate 조사 → guardian 판정
  → ACCEPTED: ArchUnit 수정
  → REJECTED: 사용자가 최종 판단
```

### CLARIFY-REQUEST (수용기준 명확화)

구현팀이 "이 수용기준이 뭔 뜻이야?"라고 product-owner에게 질문. 최대 1회.

### AUDIT-REQUEST (산출물 보완)

project-manager가 "이 산출물이 부족하다"고 해당 builder에게 보완 요청.

---

## 실행 순서 — "어떤 순서로 돌아가는가"

```
1. product-owner → 백로그 생성
2. project-lead → 컨벤션 + ADR + 구현 가이드
3. convention-guardian → ArchUnit 테스트 작성

4. Domain Team
   domain-builder → [병렬] code-reviewer + spec-reviewer → test-designer
   ↑ FIX 루프 (최대 3회)

5. Application Team (Domain 완료 후)
   application-builder → reviewer → test-designer
   ↑ FIX 루프 (최대 2회)

6. Adapter Teams [병렬] (Application 완료 후)
   persistence-mysql: builder → test-designer ↑ FIX (최대 2회)
   rest-api: builder → test-designer ↑ FIX (최대 2회)

7. project-manager → 산출물 감사

전 과정에서 journal-recorder가 자동 기록
```

---

## 도구 권한 설계 — "왜 이 에이전트는 이 도구를 못 쓰는가"

| 제한 | 대상 에이전트 | 이유 |
|------|-------------|------|
| **Write 없음** | PM, advocate, code-reviewer, spec-reviewer, app-reviewer | 리뷰/검증 전용. 직접 코드를 수정하면 builder의 책임과 겹기고, "만든 사람 ≠ 검증하는 사람" 원칙이 무너진다 |
| **Bash 없음** | PO, spec-reviewer, journal-recorder, agent-recruiter | 코드 실행이 불필요한 역할. 기획, 비즈니스 판단, 기록, 조직 관리는 코드를 돌릴 필요가 없다 |
| **ArchUnit 수정** | convention-guardian만 | ArchUnit은 프로젝트 전체 품질의 마지막 방어선. 여러 에이전트가 수정하면 규칙이 느슨해진다 |

---

## 팀 확장 — "새로운 사람이 필요할 때"

새 Adapter가 필요하면 **agent-recruiter에게 요청**하면 된다.

**"Redis 캐시 팀 채용해줘"** 하면:
1. `persistence-redis-builder.md` 생성
2. `persistence-redis-test-designer.md` 생성
3. PL이 `persistence-redis-convention.md` 작성
4. convention-guardian이 Redis ArchUnit 테스트 추가
5. 기존 팀과 동일한 FIX/매니페스트 프로토콜 적용

**팀 크기 템플릿:**
| 크기 | 구성 | 용도 |
|------|------|------|
| 최소 (2명) | builder + test-designer | Adapter 등 구현 중심 |
| 표준 (3명) | + reviewer | Application 등 중요 레이어 |
| 확장 (4명) | + spec-reviewer | Domain 등 비즈니스 핵심 |

---

## 파일 구조

```
.claude/
├── agents/                     # 에이전트 정의 (19개)
│   ├── product-owner.md
│   ├── project-lead.md
│   ├── project-manager.md
│   ├── convention-guardian.md
│   ├── convention-advocate.md
│   ├── domain-builder.md
│   ├── domain-code-reviewer.md
│   ├── domain-spec-reviewer.md
│   ├── domain-test-designer.md
│   ├── application-builder.md
│   ├── application-reviewer.md
│   ├── application-test-designer.md
│   ├── rest-api-builder.md
│   ├── rest-api-test-designer.md
│   ├── persistence-mysql-builder.md
│   ├── persistence-mysql-test-designer.md
│   ├── journal-recorder.md
│   ├── agent-recruiter.md
│   └── pipeline-orchestrator.md
│
├── skills/                     # 스킬 (3개)
│   ├── journal/SKILL.md
│   ├── ota-research/SKILL.md
│   └── pipeline/SKILL.md
│
└── CLAUDE.md                   # 프로젝트 전체 설정

docs/
├── design/
│   ├── agent-pipeline.md       # 파이프라인 기술 설계서
│   ├── agent-harness-guide.md  # 이 문서 (종합 가이드)
│   ├── domain-convention.md    # 도메인 컨벤션
│   └── adr/                    # 아키텍처 의사결정 기록
│
├── pipeline/                   # 파이프라인 실행 상태
│   ├── STORY-{번호}-state.yaml
│   └── STORY-{번호}-manifests/
│
├── seeds/                      # journal 시드 데이터
│
├── backlog.md                  # 백로그 (PO 산출물)
├── progress-journal.md         # 과정 기록서 (최종)
└── ai-usage-log.md            # AI 활용 기록 (최종)
```

---

## 용어 정리

| 용어 | 의미 |
|------|------|
| **에이전트** | 특정 역할을 수행하는 AI 서브프로세스. `.claude/agents/`에 정의 |
| **스킬** | 사용자가 슬래시 커맨드로 호출하는 자동화 도구. `.claude/skills/`에 정의 |
| **매니페스트** | 에이전트가 작업 완료 후 출력하는 결과 보고서. 다음 에이전트의 입력이 된다 |
| **FIX-REQUEST** | 리뷰어/테스터가 builder에게 보내는 수정 요청 |
| **ESCALATION** | FIX 루프가 최대 횟수를 초과했을 때의 상위 보고 절차 |
| **CONVENTION-DISPUTE** | builder가 컨벤션 규칙에 이의를 제기하는 절차 |
| **ArchUnit** | Java 아키텍처 규칙을 테스트로 강제하는 라이브러리 |
| **시드** | journal-recorder가 저장하는 원본 기록 데이터 |
| **백로그** | product-owner가 만든 작업 목록 (Epic → Story → AC) |
| **ADR** | Architecture Decision Record. 아키텍처 의사결정 기록 |
| **하네스** | AI 에이전트들을 조직하고 연결하는 전체 구조 |
