# 부트스트랩 핸드오프 문서

> 새 Claude 세션에서 이 문서를 읽고 바로 이어서 작업할 수 있다.
> 최종 갱신: 2026-04-04 (세션 2 종료 시점)

---

## 현재까지 완료된 것

### 1. 설계 완료
- OTA 3개 플랫폼 크롤링 리서치 (`docs/research/`)
- ERD v2 (`docs/erDiagram.md`) — BC별 Outbox 포함
- 4개 레이어 컨벤션 (`docs/design/domain|application|persistence|api-convention.md`)
- 에이전트 파이프라인 19개 + domain-harness 스킬/오케스트레이터
- 백로그 25+ Story (`docs/backlog.md`) — PO 재검증 완료
- 구현 로드맵 8단계 (`docs/design/implementation-roadmap.md`)
- 정합성 점검 + 산출물 감사 완료

### 2. 도메인 레이어 — 대부분 구현 + 온보딩 리뷰 진행 중

**현재 파일 수**: ~100개 (domain 모듈)

**완료된 BC 온보딩 (accommodation)**:
- `/domain-harness review accommodation` 실행 완료
- Phase 1 ArchUnit ✅ → Phase 2 code+spec 리뷰 → Phase 3 FIX 루프 1회 수렴 → Phase 4 테스트 57개 통과
- 이후 사용자 직접 리뷰에서 **record → class 이슈** 발견 → 13개 전환 완료
- ID VO 14개 신규, AmenityType/PhotoType enum, Money VO 적용
- Brand/PropertyType → Aggregate Root 승격
- ArchUnit 14개 규칙 (DOM-AGG-013 record금지, DOM-AGG-014 of()금지 추가)

**아직 온보딩 안 된 BC**:
- pricing — 코드 있지만 하네스 안 돌림
- inventory — 코드 있지만 하네스 안 돌림
- reservation — 코드 있지만 하네스 안 돌림
- partner — 코드 있지만 하네스 안 돌림
- supplier — 코드 있지만 하네스 안 돌림
- location — 코드 있지만 하네스 안 돌림
- common — 코드 있지만 하네스 안 돌림

### 3. ArchUnit 테스트 14개 통과
domain/src/test/java/com/ryuqq/otatoy/domain/DomainLayerArchTest.java

### 4. 도메인 단위 테스트
- accommodation: 57개 (PropertyTest, RoomTypeTest, PropertyAmenityTest, VoTest 등)
- reservation: 일부 (ReservationTest, ReservationItemTest, GuestInfoTest)

---

## 지금 해야 할 것

### Step 0: 하네스 + 에이전트 보강

**domain-harness-orchestrator.md** — Phase 결과 문서화 규칙 추가:
- Phase 2 결과 → `docs/review/{BC}-code-review.md`, `docs/review/{BC}-spec-review.md`
- Phase 4 결과 → `docs/review/{BC}-test-scenarios.md` (어떤 비즈니스 규칙을 어떤 테스트가 검증하는지)
- Phase 6 결과 → `docs/review/{BC}-harness-result.md` (전체 요약)
- 이 문서들이 있어야 "이 도메인이 어떤 시나리오를 커버하는지" 사람이 볼 수 있다.

**domain-test-designer.md** — 기존 테스트 확인 단계 추가:
- 현재: spec-reviewer 시나리오만 보고 새로 작성 → 기존 테스트와 중복/스타일 불일치 가능
- 수정: 작업 전에 기존 테스트 파일을 먼저 확인하여 이미 커버된 시나리오 파악 → 없는 것만 추가 → 기존 스타일에 맞춰 작성
- 작업 절차를 아래로 변경:
  1. 대상 BC의 기존 테스트 파일 확인 (`domain/src/test/java/.../`)
  2. 어떤 시나리오가 이미 커버되는지 파악
  3. spec-reviewer 보고서 읽기
  4. 기존에 없는 시나리오만 추가 작성
  5. 기존 테스트 스타일(네이밍, 구조, assert 방식)에 맞춰 작성

### Step 0.5: testFixtures 패턴 도입

현재 모든 테스트에서 도메인 객체를 직접 생성하고 있음. testFixtures로 전환 필요.

**구조:**
```
domain/src/testFixtures/java/com/ryuqq/otatoy/domain/
├── accommodation/
│   ├── PropertyFixture.java      — aProperty(), aPropertyWith(name)
│   ├── RoomTypeFixture.java
│   └── ...
├── pricing/
│   ├── RatePlanFixture.java
│   └── ...
├── inventory/
│   └── InventoryFixture.java
└── reservation/
    └── ReservationFixture.java
```

**Fixture 패턴:**
```java
public class PropertyFixture {
    public static Property aProperty() {
        return Property.forNew(
            PartnerId.of(1L), BrandId.of(2L), PropertyTypeId.of(3L),
            PropertyName.of("테스트 호텔"), PropertyDescription.of("설명"),
            Location.of("서울시", 37.5, 127.0, "강남", "서울"),
            PromotionText.of("홍보 문구"), Instant.now()
        );
    }
    
    public static Property aPropertyWith(PropertyName name) { ... }
}
```

**의존 방향:**
- domain testFixtures → domain main (당연)
- application test → domain testFixtures (Fixture 재사용)
- persistence test → domain testFixtures (Fixture 재사용)
- api test → domain testFixtures + application testFixtures (재사용)

**Gradle:** `java-test-fixtures` 플러그인이 이미 domain/build.gradle.kts에 있음. 다른 모듈에서 `testImplementation(testFixtures(project(":domain")))` 추가.

**Fixture 카탈로그 문서** (`docs/review/fixture-catalog.md`):
test-designer가 Fixture를 만들 때마다 이 문서를 갱신한다. "어떤 Fixture가 어떤 상태의 객체를 만드는지" 한눈에 볼 수 있어야 한다.

```markdown
## accommodation

| Fixture 클래스 | 메서드 | 반환 객체 상태 | 용도 |
|---------------|--------|-------------|------|
| PropertyFixture | aProperty() | ACTIVE, 브랜드 있음, 서울 강남 | 기본 테스트용 |
| PropertyFixture | aPropertyWithoutBrand() | ACTIVE, 브랜드 없음 | 모텔/펜션 테스트 |
| PropertyFixture | anInactiveProperty() | INACTIVE | 비활성 숙소 테스트 |
| RoomTypeFixture | aRoomType() | ACTIVE, 2인, 더블베드 | 기본 객실 |
```

**test-designer 에이전트 수정사항:**
- Fixture 관리 책임을 test-designer에 통합 (별도 에이전트 만들지 않음)
- 작업 절차:
  1. 기존 테스트 파일 확인
  2. 기존 Fixture 확인 (`testFixtures/`)
  3. Fixture 카탈로그 확인 (`docs/review/fixture-catalog.md`)
  4. 필요한 Fixture가 없으면 생성 + 카탈로그 갱신
  5. Fixture 사용하여 테스트 작성
  6. 기존 스타일에 맞춤

**작업:** 기존 테스트 코드에서 직접 생성하는 부분을 Fixture로 추출. test-designer 에이전트도 Fixture 사용하도록 수정.

### Step 1: 래핑 객체 도입 (PropertyAmenities 등)

사용자가 결정한 설계:
- sortOrder 중복 검증, 컬렉션 수준 불변식은 래핑 객체가 담당
- PropertyAmenities, PropertyPhotos, RoomAmenities, RoomPhotos, RoomTypeBeds, RoomTypeViews 등
- `of()` — 검증 수행, `reconstitute()` — 검증 없이 복원
- persistence에서 도메인 변환 시 자동으로 컬렉션 검증

참고할 설계 의도 (사용자 원문):
> "PropertyAmenity라는건 하나만으론 안쓰이고 여러개가 같이 딸려서 나온다. PropertyAmenities라는 래핑 객체를 도메인에 두면, persistence 레이어에서 도메인 객체로 변환할 때 자동으로 검증된다."

### Step 2: 나머지 BC 하네스 리뷰

accommodation과 동일하게 `/domain-harness review {BC}` 실행:
- pricing, inventory, reservation, partner, supplier, location

각 BC에서 accommodation과 같은 이슈(record→class 등)가 있을 수 있음.
accommodation 온보딩에서 확립한 패턴을 적용.

### Step 3: name 필드 VO화

사용자 결정: "String은 유지하지마. 각 객체만의 VO가 있어야 한다."
- 각 하위 엔티티의 name, originUrl 등을 전용 VO로 래핑
- 예: AmenityName, PhotoOriginUrl 등

### Step 4: Phase 1 — 기반 구축
- Docker Compose (MySQL, Redis)
- Flyway 마이그레이션
- Application 공통 (TimeProvider, Factory 베이스)
- Persistence 공통 (BaseAuditEntity, SoftDeletableEntity, JPA Config)

---

## 핵심 컨벤션 (새 세션 필수 숙지)

### Domain 패턴 (accommodation에서 확립)

**Aggregate Root (Property 패턴)**:
- private 생성자, forNew() (검증 VO에 위임), reconstitute() (검증 없음)
- ID VO (자기 ID: null 허용 + isNew, 참조 ID: nullable이면 null 허용)
- String 필드 → 전용 VO (PropertyName, BrandName 등)
- equals/hashCode ID 기반, 접근자 xxx() 스타일

**하위 엔티티 (PropertyAmenity 패턴)**:
- class (record 아님!), private 생성자
- forNew() (검증), reconstitute() (검증 없음)
- 전용 ID VO (PropertyAmenityId)
- BigDecimal → Money VO
- String 타입 → enum (AmenityType, PhotoType)
- equals/hashCode ID 기반

**절대 하면 안 되는 것**:
- Long id를 가진 record (ArchUnit DOM-AGG-013이 잡음)
- 엔티티 class에 of() 메서드 (ArchUnit DOM-AGG-014가 잡음)
- record의 compact constructor로 검증하면 reconstitute()에서도 검증 실행됨 — 치명적

### Application/Persistence/API
docs/design/ 의 각 컨벤션 문서 참조

---

## 핵심 참조 파일

| 파일 | 용도 |
|------|------|
| `.claude/CLAUDE.md` | 프로젝트 전체 설정 (최신) |
| `docs/design/domain-convention.md` | 도메인 컨벤션 |
| `docs/design/application-convention.md` | Application 컨벤션 |
| `docs/design/persistence-convention.md` | Persistence 컨벤션 |
| `docs/design/api-convention.md` | API 컨벤션 |
| `docs/design/implementation-roadmap.md` | 구현 로드맵 8단계 |
| `docs/erDiagram.md` | ERD v2 |
| `docs/backlog.md` | 백로그 |
| `.claude/agents/*.md` | 에이전트 정의 20개 |
| `.claude/skills/domain-harness/SKILL.md` | 도메인 하네스 스킬 |
| `docs/seeds/2026-04-04-decisions.md` | 오늘의 설계 판단 15개 |
| `domain/src/test/java/.../DomainLayerArchTest.java` | ArchUnit 14개 규칙 |

---

## Phase 2 구현 가이드 수정 필요 (사용자 피드백)

`docs/design/phase2-implementation-guide.md`를 PL이 작성했으나 **핵심 구조가 잘못됨.**

### 문제
PL이 "숙소 등록 시 기본정보 + 편의시설 + 사진 + 속성값을 한번에 저장"하는 구조로 작성함.

### 실제 (OTA 리서치 근거)
`docs/research/ota-extranet-registration-flow.md` 참조:
- Booking.com, 야놀자 모두 **단계별 별도 저장**
- 기본정보 → 객실 → 사진 → 편의시설 → 요금 각각 독립 API
- 등록 후에도 각각 독립 수정 (사진 업로드가 숙소 기본정보와 무관)
- 중간 저장 가능

### 수정 방향
STORY-103을 쪼개야 함:
```
STORY-103: 숙소 기본정보 등록 UseCase (Property만)
  → RegisterPropertyUseCase — Property 기본정보만 저장
  → PersistenceFacade 불필요 (단일 Aggregate만 저장)

별도 Story:
  → AddPropertyPhotosUseCase — 사진 독립 업로드
  → SetPropertyAmenitiesUseCase — 편의시설 독립 설정
  → SetPropertyAttributesUseCase — 속성값 독립 설정
  → RegisterRoomTypeUseCase — 객실 독립 등록
```

### PersistenceFacade 사용 시점
여러 Aggregate를 한 트랜잭션에 묶는 게 아니라, **단일 Aggregate 저장이면 CommandManager로 충분.**
PersistenceFacade는 "예약 생성 + Outbox 저장" 같은 진짜 여러 Aggregate가 원자적이어야 할 때만 사용.

### PL에게 시킬 것
1. phase2-implementation-guide.md 재작성
2. 위 리서치 근거를 반영하여 UseCase를 단일 책임으로 분리
3. 백로그(STORY-103)도 분할 필요 → PO에게 요청

---

## 즉시 실행

```
이 문서(docs/bootstrap-handoff.md)를 읽었으면:

1. Step 1: 래핑 객체 도입 (PropertyAmenities 패턴 설계 + 구현)
2. Step 2: /domain-harness review pricing → inventory → reservation → partner → supplier → location
3. Step 3: name 필드 VO화
4. Step 4: Phase 1 기반 구축 (Docker, Flyway, Application/Persistence 공통)
```
