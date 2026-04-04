# 구현 로드맵

> 작성일: 2026-04-02
> 기준: 백로그 P0 Story 중심, 핵심 흐름(숙소 등록 → 검색 → 예약 → 취소) 우선
> 원칙: Domain → Application → Adapter-out → Adapter-in 순서 (레이어 의존 방향)

---

## 현재 상태

### 구현 완료
- 도메인 모델: accommodation(Property, RoomType, Brand 등), pricing(RatePlan, RateRule, Rate 등), location(Location, Landmark 등)
- 공통 인프라: ErrorCode 인터페이스, DomainException 기반 클래스
- 빌드 구조: settings.gradle.kts에 domain, application, adapter-in/rest-api, adapter-out/persistence-mysql, adapter-out/persistence-redis 모듈 등록

### 전체 미구현
- Application 레이어 전체 (UseCase, Service, Manager, Factory, Port, DTO)
- Adapter 레이어 전체 (JpaEntity, Mapper, Repository, Controller)
- Infra (Docker Compose, Flyway, SpringDoc)
- 도메인: Partner, Inventory, Reservation, Supplier, Outbox, 공통 VO

---

## Phase 1: 기반 구축

> 목표: 모든 레이어가 동작할 수 있는 최소 기반

### Step 1-1: 프로젝트 기반 세팅

**STORY-701: Gradle 멀티모듈 세팅** (규모: M)
- core 모듈 신규 생성 (TimeProvider, ApiResponse, ErrorCode 인터페이스 이동)
- infra 모듈 신규 생성 (SystemTimeProvider, 설정 클래스)
- adapter-out-supplier 모듈 신규 생성
- 모듈 간 의존성 방향 설정 (domain → core, application → domain, adapter → application)
- `./gradlew build` 성공 확인

**STORY-702: Docker Compose** (규모: S)
- docker-compose.yml (MySQL 8.x + Redis)
- application.yml 프로필 설정 (local, test)

### Step 1-2: 공통 도메인 VO + 예외 처리

**STORY-102: 공통 도메인 VO** (규모: M)
- Domain 레이어:
  - `domain/common/vo/`: DateRange, Money, DeletionStatus
  - `domain/common/query/`: PageRequest, CursorPageRequest, SliceMeta, PageMeta
  - `domain/common/sort/`: SortKey, SortDirection, SearchField, DateField
  - `domain/common/infra/`: CacheKey, LockKey
- 결정 필요: Money를 int 기반으로 할지, BigDecimal 기반으로 할지 (현재 pricing 도메인이 BigDecimal 사용 중)

**STORY-703: 공통 응답 포맷 + 예외 처리** (규모: M)
- core 모듈: ApiResponse record, ErrorDetail record
- adapter-in/rest-api: GlobalExceptionHandler, ErrorMapper
- SliceResponse record

---

## Phase 2: 숙소 등록 E2E (첫 번째 관통 흐름)

> 목표: Domain → Application → Persistence → REST API 전 레이어를 관통하는 첫 번째 흐름 완성

### Step 2-1: 도메인 보완

**STORY-101: 숙소 도메인 완성** (규모: S, 남은 부분)
- Domain 레이어:
  - `domain/partner/`: Partner, PartnerMember, PartnerStatus, PartnerErrorCode, PartnerNotFoundException
  - Partner.forNew() / reconstitute() 패턴 적용
  - PartnerMember.role(OWNER, MANAGER, STAFF), status(ACTIVE, INACTIVE)

### Step 2-2: 숙소 등록 Application

**STORY-103: 숙소 등록 UseCase** (규모: M)
- Application 레이어:
  - Port-In: `RegisterPropertyUseCase` 인터페이스
  - Port-Out: `PropertyCommandPort`, `PropertyQueryPort`, `PartnerQueryPort`
  - DTO: `RegisterPropertyCommand` record
  - Factory: `PropertyFactory` (TimeProvider 주입)
  - Manager: `PropertyCommandManager`(@Transactional), `PropertyReadManager`(@Transactional(readOnly)), `PartnerReadManager`
  - Facade: `PropertyPersistenceFacade`
  - Service: `RegisterPropertyService` (@Transactional 금지)
- 참조 컨벤션: APP-UC-001, APP-SVC-001, APP-MGR-001, APP-FAC-001, APP-PRT-001, APP-BC-001

### Step 2-3: 숙소 Persistence

**STORY-104: 숙소 Persistence Adapter** (규모: L)
- Persistence 레이어:
  - `BaseAuditEntity`, `SoftDeletableEntity`
  - `PropertyJpaEntity`, `PartnerJpaEntity`, `PartnerMemberJpaEntity`
  - `PropertyEntityMapper`, `PartnerEntityMapper`
  - `PropertyJpaRepository` (save/saveAll만)
  - `PropertyQueryDslRepository` + `PropertyConditionBuilder`
  - `PropertyCommandAdapter`, `PropertyQueryAdapter`
  - `PartnerQueryAdapter`
  - Flyway: `V202604021000__create_property_table.sql`, `V202604021001__create_partner_table.sql`
  - JPA 설정: open-in-view=false, ddl-auto=validate
- 참조 컨벤션: PER-ENT-001~005, PER-MAP-001, PER-ADP-001~002, PER-REP-001, PER-FLY-001

### Step 2-4: 숙소 등록 REST API

**STORY-105: 숙소 등록 API** (규모: M)
- API 레이어:
  - `ExtranetPropertyController` (@RestController)
  - `RegisterPropertyApiRequest` record (Jakarta Validation)
  - `PropertyApiResponse` record
  - `PropertyApiMapper` (static 메서드)
  - POST /api/v1/extranet/properties → 201 응답
- 참조 컨벤션: API-CTR-001~003, API-DTO-001~002, API-DOC-001

---

## Phase 3: 객실 + 요금/재고 설정 (Extranet 완성)

> 목표: 파트너가 숙소 → 객실 → 요금/재고까지 설정할 수 있는 흐름

### Step 3-1: 객실 등록

**STORY-106: 객실 등록 API** (규모: M)
- Application: `RegisterRoomTypeUseCase`, `RegisterRoomTypeService`, `RoomTypeCommandManager`, `RoomTypeReadManager`
- Persistence: `RoomTypeJpaEntity`, `RoomTypeEntityMapper`, `RoomTypeJpaRepository`, `RoomTypeQueryDslRepository`, Command/Query Adapter
- API: `ExtranetRoomTypeController`, POST /api/v1/extranet/properties/{id}/rooms
- Flyway: `V202604021002__create_room_type_table.sql`, BedType/ViewType 관련 테이블

### Step 3-2: 재고/요금 설정 + Redis 초기화

**STORY-107: 재고/요금 설정 API** (규모: L)
- Application:
  - `SetInventoryUseCase`, `SetInventoryService`
  - `RatePlanCommandManager`, `RateCommandManager`, `InventoryCommandManager`
  - `RateCacheClientManager` (Redis 캐시)
  - `InventoryClientManager` (Redis 재고)
  - Port-Out: `RatePlanCommandPort`, `RateCommandPort`, `InventoryCommandPort`, `RateCachePort`, `InventoryRedisPort`
- Persistence: `RatePlanJpaEntity`, `RateRuleJpaEntity`, `RateJpaEntity`, `InventoryJpaEntity` + 각 Mapper/Repository/Adapter
- Redis: `RateCacheAdapter`, `InventoryRedisAdapter`
- API: `ExtranetInventoryController`, PUT /api/v1/extranet/inventory
- 핵심 로직: RateRule → Rate 스냅샷 자동 계산, Redis 캐시 + 재고 카운터 초기화
- Flyway: rate_plan, rate_rule, rate_override, rate, inventory 테이블

**STORY-601: Redis 재고 초기화 + 홀드 만료 처리** (규모: M)
- STORY-107과 병행. InventoryRedisPort에 initializeStock, getStock, decrementStock, incrementStock 정의
- 홀드 만료 복구 처리 (스케줄러 또는 Redis Keyspace Notification)

---

## Phase 4: 고객 검색 + 요금 조회

> 목표: 고객이 숙소를 검색하고 요금을 확인할 수 있는 흐름

### Step 4-1: 숙소 검색

**STORY-201: 숙소 검색 API** (규모: L)
- Domain: `PropertySliceCriteria` record
- Application: `SearchPropertyUseCase`, `SearchPropertyService`, `SearchPropertyQuery`, `PropertySliceResult`
- Persistence: `PropertyQueryDslRepository.findByCondition()` — 커서 기반 페이지네이션
- Redis: 재고 확인 (inventory:{roomTypeId}:{date} 조회)
- API: `SearchPropertyController`, GET /api/v1/search/properties

### Step 4-2: 요금 조회 + 3단 캐싱

**STORY-202: 요금 조회 API + Redis 3단 캐싱** (규모: L)
- Application: `FetchRateUseCase`, `FetchRateService`, `FetchRateQuery`, `RateDateResult`
- Redis: `RateCacheClientManager` — Redis 조회 → DB 조회 → 캐시 갱신
- 분산 락: Redisson 기반 캐시 스탬피드 방어
- TTL Jittering: 60분 + random(0~10분)
- API: `RateController`, GET /api/v1/properties/{id}/rates

---

## Phase 5: 예약 생성 + 취소 (핵심 동시성)

> 목표: 예약 전체 생명주기 + 동시성 제어 검증

### Step 5-1: 예약/재고 도메인

**STORY-301: 예약/재고 도메인 모델** (규모: M)
- Domain 레이어:
  - `domain/reservation/`: Reservation, ReservationId, ReservationItem, ReservationStatus, ReservationErrorCode, 각종 예외
  - `domain/inventory/`: Inventory, InventoryId, InventoryErrorCode
  - Reservation.forNew() → PENDING, confirm(), cancel(reason, cancelledAt)
  - ReservationStatus 상태 전이 규칙 코드화
  - bookingSnapshot JSON 필드

### Step 5-2: 예약 생성 (Redis 동시성 제어)

**STORY-302: 예약 생성 UseCase + Redis 원자적 카운터** (규모: L)
- Application:
  - `CreateReservationUseCase`, `CreateReservationService`
  - `ReservationFactory` (TimeProvider)
  - `ReservationPersistenceFacade` (Reservation + ReservationItem 원자적 저장)
  - `InventoryClientManager` — Redis DECR/INCR
  - 흐름: Redis DECR → 임시 홀드 생성 → DB 저장 → 실패 시 INCR 복구
- Persistence: `ReservationJpaEntity`, `ReservationItemJpaEntity`, `InventoryJpaEntity` + Mapper/Repository/Adapter
- API: `ReservationController`, POST /api/v1/reservations → 201

### Step 5-3: 예약 취소

**STORY-303: 예약 취소 API** (규모: M)
- Application: `CancelReservationUseCase`, `CancelReservationService`
- 흐름: Reservation.cancel() → Redis INCR 재고 복구 → DB 저장 → Outbox 저장
- API: PATCH /api/v1/reservations/{id}/cancel → 200

### Step 5-4: 동시성 통합 테스트

**STORY-304: 동시성 제어 통합 테스트** (규모: M)
- Testcontainers (MySQL + Redis) 환경
- ExecutorService 10개 스레드 동시 예약
- 재고 1개 → 성공 1개 + 실패 9개 검증
- Redis-DB 재고 정합성 검증

---

## Phase 6: Supplier 통합

> 목표: 외부 공급자 숙소를 내부 플랫폼에 통합

### Step 6-1: Supplier 도메인

**STORY-401: Supplier 도메인 모델** (규모: M)
- Domain 레이어:
  - `domain/supplier/`: Supplier, SupplierId, SupplierApiConfig, SupplierProperty, SupplierRoomType, SupplierSyncLog, SupplierStatus, SupplierErrorCode

### Step 6-2: Supplier ACL + 동기화

**STORY-402: Supplier Anti-Corruption Layer** (규모: L)
- Application: `SupplierClient` Port, `SupplierClientManager`, Supplier 응답 Redis 캐싱
- adapter-out-supplier: `SupplierClientAdapter` (외부 API Mock 또는 Stub)

**STORY-403: Supplier Diff 동기화** (규모: L)
- Application: `SyncSupplierUseCase`, `SyncSupplierService`
- Diff 패턴: 기존 매핑 대비 INSERT/UPDATE/soft DELETE
- SupplierSyncLog 기록

---

## Phase 7: Outbox + 비동기 처리

> 목표: Spring Event 대신 Outbox 패턴으로 크로스 도메인 비동기 처리

### Step 7-1: Outbox 기반 구현

**STORY-501: Outbox 도메인 + 스케줄러** (규모: L)
- Domain: `domain/common/outbox/` — Outbox, OutboxStatus
- Application: OutboxCommandPort/QueryPort, OutboxCommandManager/ReadManager, OutboxFactory, OutboxMainScheduler, OutboxZombieScheduler, OutboxProcessor
- Persistence: OutboxJpaEntity, OutboxEntityMapper, OutboxJpaRepository, OutboxQueryDslRepository

**STORY-502: 예약 Outbox 연동** (규모: M, P1)
- ReservationPersistenceFacade에서 Outbox 동시 저장
- OutboxProcessor에서 RESERVATION_CREATED/CANCELLED 처리

---

## Phase 8: 확장 + 품질 (P1)

> 목표: 테스트 커버리지, 문서화, Admin API

### 테스트
- **STORY-1001**: 도메인 단위 테스트 (순수 Java)
- **STORY-1002**: Repository 통합 테스트 (Testcontainers)
- **STORY-1003**: API E2E 통합 테스트

### Admin + 모니터링
- **STORY-801**: Admin 숙소 목록/상세 조회 API
- **STORY-802**: Admin 예약 모니터링 API
- **STORY-602**: Redis-DB 재고 정합성 배치

### 문서
- **STORY-1101**: SpringDoc/Swagger API 문서 자동화
- **STORY-1102**: 설계 문서 (아키텍처, API 명세)
- **STORY-1103**: 과정 기록서 최종 정리

---

## 전체 의존성 + 순서 요약

```
Phase 1 (기반)
  STORY-701 → STORY-702 → STORY-102 → STORY-703

Phase 2 (숙소 등록 E2E) — Phase 1 완료 후
  STORY-101 → STORY-103 → STORY-104 → STORY-105

Phase 3 (객실 + 요금/재고) — Phase 2 완료 후
  STORY-106 → STORY-107 + STORY-601

Phase 4 (검색 + 요금조회) — Phase 3 완료 후
  STORY-201
  STORY-202

Phase 5 (예약) — Phase 3, Phase 4-1 완료 후 (STORY-301은 Phase 1 이후 병행 가능)
  STORY-301 → STORY-302 → STORY-303 → STORY-304

Phase 6 (Supplier) — Phase 2 완료 후 (독립 진행 가능)
  STORY-401 → STORY-402 → STORY-403

Phase 7 (Outbox) — Phase 1 완료 후 (독립 진행 가능, Phase 5와 합류)
  STORY-501 → STORY-502

Phase 8 (확장) — Phase 5 완료 후
  STORY-1001, 1002, 1003, 801, 802, 602, 1101, 1102, 1103
```

---

## 병렬화 가능 구간

Phase 1 완료 후 다음을 병렬로 진행 가능:
- **Track A**: Phase 2 → Phase 3 → Phase 4 → Phase 5 (핵심 흐름)
- **Track B**: STORY-301 도메인 모델 (Phase 5-1) — Phase 1 직후 시작 가능
- **Track C**: STORY-401 도메인 모델 (Phase 6-1) — Phase 1 직후 시작 가능
- **Track D**: STORY-501 Outbox (Phase 7) — Phase 1 직후 시작 가능

단, 단독 개발 시 Track A를 우선 진행하고, 도메인 모델(B, C)은 틈틈이 병행하는 것이 효율적.

---

## Phase별 추정 Story 수 및 규모

| Phase | Story 수 | 추정 규모 | 핵심 산출물 |
|-------|:--------:|----------|------------|
| Phase 1 | 4 | M+S+M+M | 빌드 성공, Docker 환경, 공통 VO, 예외 처리 |
| Phase 2 | 4 | S+M+L+M | 숙소 등록 전 레이어 관통 |
| Phase 3 | 3 | M+L+M | 객실/요금/재고 설정, Redis 초기화 |
| Phase 4 | 2 | L+L | 검색, 요금 캐싱 |
| Phase 5 | 4 | M+L+M+M | 예약 전체 생명주기, 동시성 검증 |
| Phase 6 | 3 | M+L+L | Supplier 통합 |
| Phase 7 | 2 | L+M | Outbox 비동기 처리 |
| Phase 8 | 9+ | 혼합 | 테스트, Admin, 문서 |

---

## 구현 전 해결해야 할 사항 (정합성 보고서 참조)

1. **ERD에 Outbox 테이블 추가** — Phase 7 시작 전
2. **Money VO 방향 결정** — Phase 1 STORY-102에서 결정
   - 옵션 A: Money(int) 유지, pricing 도메인의 BigDecimal을 Money로 전환
   - 옵션 B: Money(BigDecimal) 변경
   - 옵션 C: Money VO는 공통으로 두되, pricing은 BigDecimal 직접 사용 허용
3. **domain-convention 패키지 구조 갱신** — pricing, location BC 분리를 문서에 반영
