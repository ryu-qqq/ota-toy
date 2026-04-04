# 백로그

> 생성일: 2026-04-04
> 기준: 요구사항 필수 6개 + 선택 확장 + 권장 확장
> 현재 상태: Domain 모델 일부 구현 (accommodation, pricing, location, partner), Application/Adapter 전체 미구현

---

## Epic 1: 숙소 등록 및 관리 (Extranet)

> 필수 요구사항 1: "숙소 파트너가 Extranet을 통해 숙소 정보를 등록하고 관리할 수 있어야 한다"

### STORY-101: 숙소(Property) 도메인 모델 완성
- **우선순위**: P0
- **구현 상태**: 부분 완료 (Property, RoomType, RatePlan 등 도메인 모델 존재. Partner 도메인은 PartnerId만 존재)
- **수용기준**:
  - [ ] AC-1: Property.forNew() 호출 시 name이 blank이면 IllegalArgumentException 발생
  - [ ] AC-2: Property.reconstitute()는 검증 없이 모든 필드를 복원
  - [ ] AC-3: Partner 도메인 모델(Partner, PartnerMember)이 forNew/reconstitute 패턴으로 구현
  - [ ] AC-4: ArchUnit 테스트 통과 (setter 금지, 외부 의존 금지, 생성자 비공개)
- **관련 레이어**: Domain
- **의존성**: 없음
- **담당 팀**: domain-team
- **추정 규모**: S

### STORY-102: 숙소 등록 UseCase 구현
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: RegisterPropertyUseCase 호출 시 Property가 저장되고 PropertyId가 반환됨
  - [ ] AC-2: partnerId가 존재하지 않으면 PartnerNotFoundException 발생
  - [ ] AC-3: PropertyCommandPort, PropertyQueryPort 인터페이스가 Application 레이어에 정의
- **관련 레이어**: Domain → Application
- **의존성**: STORY-101
- **담당 팀**: application-team
- **추정 규모**: M

### STORY-103: 숙소 Persistence Adapter 구현
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: PropertyJpaEntity와 Property 도메인 간 매핑이 정확히 동작 (reconstitute 사용)
  - [ ] AC-2: save() 후 DB에서 조회하면 모든 필드가 일치
  - [ ] AC-3: Testcontainers(MySQL) 기반 통합 테스트 통과
- **관련 레이어**: Application → Adapter-out:mysql
- **의존성**: STORY-102
- **담당 팀**: persistence-mysql-team
- **추정 규모**: L

### STORY-104: 숙소 등록 REST API (Extranet)
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: POST /api/v1/extranet/properties 요청 시 201 응답 + JSON body에 propertyId 필드 존재
  - [ ] AC-2: 필수 필드(name, propertyTypeId, partnerId) 누락 시 400 응답 + errorCode 필드 존재
  - [ ] AC-3: Swagger UI에서 API 명세 확인 가능
- **관련 레이어**: Application → Adapter-in:rest-api
- **의존성**: STORY-102
- **담당 팀**: rest-api-team
- **추정 규모**: M

### STORY-105: 객실(RoomType) 등록 API (Extranet)
- **우선순위**: P0
- **구현 상태**: 미구현 (도메인 모델 RoomType은 존재)
- **수용기준**:
  - [ ] AC-1: POST /api/v1/extranet/properties/{propertyId}/rooms 요청 시 201 응답 + roomTypeId 필드 존재
  - [ ] AC-2: propertyId가 존재하지 않으면 404 응답
  - [ ] AC-3: baseOccupancy > maxOccupancy이면 400 응답
- **관련 레이어**: Domain → Application → Adapter-out:mysql → Adapter-in:rest-api
- **의존성**: STORY-104
- **담당 팀**: rest-api-team
- **추정 규모**: M

### STORY-106: 재고/요금 설정 API (Extranet)
- **우선순위**: P0
- **구현 상태**: 미구현 (RatePlan, RateRule, Rate 도메인 모델 존재)
- **수용기준**:
  - [ ] AC-1: PUT /api/v1/extranet/inventory 요청 시 RateRule 저장 + 해당 기간의 Rate 스냅샷 자동 생성
  - [ ] AC-2: 기간(startDate~endDate) 내 요일별 가격이 Rate 테이블에 정확히 반영
  - [ ] AC-3: RateOverride가 있으면 해당 날짜의 Rate가 override 가격으로 갱신
  - [ ] AC-4: Inventory가 해당 기간에 대해 base_inventory 값으로 생성
  - [ ] AC-5: Redis 캐시에 rate:{ratePlanId}:{date} 키로 캐싱
- **관련 레이어**: Domain → Application → Adapter-out:mysql → Adapter-out:redis → Adapter-in:rest-api
- **의존성**: STORY-105
- **담당 팀**: application-team
- **추정 규모**: L

---

## Epic 2: 숙소 검색 및 요금 조회

> 필수 요구사항 2: "고객이 조건에 맞는 숙소를 검색하고 요금을 조회할 수 있어야 한다"
> 필수 요구사항 3: "대규모 요금 조회 요청이 동시에 들어오는 상황을 고려한 설계"

### STORY-201: 숙소 검색 API (Customer)
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: GET /api/v1/search/properties?region=서울&checkIn=2026-04-10&checkOut=2026-04-12&guests=2 요청 시 200 응답 + properties 배열 반환
  - [ ] AC-2: 해당 날짜에 재고가 0인 숙소는 결과에 포함되지 않음
  - [ ] AC-3: maxOccupancy < guests인 객실만 있는 숙소는 결과에 포함되지 않음
  - [ ] AC-4: 커서 기반 페이지네이션 지원 (size, cursor 파라미터)
  - [ ] AC-5: 응답에 숙소 기본 정보 + 최저 가격이 포함
- **관련 레이어**: Domain → Application → Adapter-out:mysql → Adapter-out:redis → Adapter-in:rest-api
- **의존성**: STORY-106
- **담당 팀**: application-team
- **추정 규모**: L

### STORY-202: 요금 조회 API + Redis 캐싱 (Customer)
- **우선순위**: P0
- **구현 상태**: 미구현 (캐싱 설계 문서 완료 — seeds/2026-04-02-rate-caching-design.md)
- **수용기준**:
  - [ ] AC-1: GET /api/v1/properties/{id}/rates?checkIn=2026-04-10&checkOut=2026-04-12 요청 시 200 응답 + 날짜별 요금 배열 반환
  - [ ] AC-2: 첫 요청은 DB 조회 후 Redis 캐시에 저장. 두 번째 동일 요청은 Redis에서 응답 (DB 쿼리 0회)
  - [ ] AC-3: 캐시 TTL은 60분 + 랜덤 오프셋(0~10분) 적용 (Jittering)
  - [ ] AC-4: 캐시 미스 시 Redisson 분산 락으로 동일 키에 대해 DB 요청 1회만 발생
  - [ ] AC-5: 100개 동시 요금 조회 요청 시 DB 쿼리 수가 10 이하 (캐시 효과 검증)
- **관련 레이어**: Domain → Application → Adapter-out:mysql → Adapter-out:redis → Adapter-in:rest-api
- **의존성**: STORY-106
- **담당 팀**: application-team
- **추정 규모**: L

---

## Epic 3: 예약 및 취소

> 필수 요구사항 4: "고객이 숙소를 예약하고 취소할 수 있어야 한다"
> 필수 요구사항 5: "동일한 재고에 대해 동시 예약 요청이 발생할 수 있는 상황을 처리"

### STORY-301: 예약/재고 도메인 모델 구현
- **우선순위**: P0
- **구현 상태**: 미구현 (ERD에 Reservation, ReservationItem, Inventory 정의됨. 도메인 코드 없음)
- **수용기준**:
  - [ ] AC-1: Reservation.forNew() 시 status가 PENDING으로 생성
  - [ ] AC-2: Reservation.confirm() 호출 시 status가 CONFIRMED로 변경
  - [ ] AC-3: Reservation.cancel(reason) 호출 시 status가 CANCELLED로 변경 + cancelReason, cancelledAt 설정
  - [ ] AC-4: 이미 CANCELLED인 Reservation에 cancel() 호출 시 예외 발생
  - [ ] AC-5: Inventory 도메인에 available_count 필드와 isStopSell 판단 로직 존재
  - [ ] AC-6: ReservationStatus enum에 상태 전이 규칙이 코드로 표현 (PENDING→CONFIRMED, PENDING→CANCELLED, CONFIRMED→CANCELLED)
- **관련 레이어**: Domain
- **의존성**: 없음
- **담당 팀**: domain-team
- **추정 규모**: M

### STORY-302: 예약 생성 UseCase + Redis 재고 차감 (동시성 제어)
- **우선순위**: P0
- **구현 상태**: 미구현 (동시성 설계 문서 완료 — seeds/2026-04-02-inventory-concurrency-design.md)
- **수용기준**:
  - [ ] AC-1: POST /api/v1/reservations 요청 시 Redis DECR로 재고 차감 후 201 응답 + reservationId 반환
  - [ ] AC-2: 재고가 0인 날짜에 예약 요청 시 409 응답 + INVENTORY_EXHAUSTED 에러코드
  - [ ] AC-3: 2박 예약 시 두 날짜 모두 재고 확인. 하나라도 0이면 전부 INCR 복구 후 실패
  - [ ] AC-4: 임시 홀드(hold:{reservationId}) 키가 Redis에 TTL 600초로 생성
  - [ ] AC-5: 10개 동시 예약 요청 중 재고가 1개일 때 정확히 1개만 성공하고 나머지 9개는 실패
  - [ ] AC-6: DB에 Reservation(CONFIRMED) + ReservationItem 저장
- **관련 레이어**: Domain → Application → Adapter-out:mysql → Adapter-out:redis → Adapter-in:rest-api
- **의존성**: STORY-301, STORY-106
- **담당 팀**: application-team
- **추정 규모**: L

### STORY-303: 예약 취소 API
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: DELETE /api/v1/reservations/{id} 요청 시 200 응답 + status가 CANCELLED
  - [ ] AC-2: 취소 시 Redis INCR로 해당 날짜 재고 복구
  - [ ] AC-3: DB Inventory의 available_count도 복구
  - [ ] AC-4: 존재하지 않는 예약 ID로 요청 시 404 응답
  - [ ] AC-5: 이미 취소된 예약에 대해 재요청 시 400 응답 + ALREADY_CANCELLED 에러코드
- **관련 레이어**: Domain → Application → Adapter-out:mysql → Adapter-out:redis → Adapter-in:rest-api
- **의존성**: STORY-302
- **담당 팀**: application-team
- **추정 규모**: M

### STORY-304: 동시성 제어 통합 테스트
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: Testcontainers(MySQL + Redis) 환경에서 ExecutorService 10개 스레드 동시 예약 실행
  - [ ] AC-2: 재고 1개인 객실에 대해 10개 동시 요청 시 성공 1개 + 실패 9개 (정확히)
  - [ ] AC-3: 테스트 후 Redis 재고와 DB Inventory.available_count가 일치
  - [ ] AC-4: 예약 취소 후 재고가 정확히 복구되는지 검증
- **관련 레이어**: Application → Adapter-out:mysql → Adapter-out:redis
- **의존성**: STORY-302
- **담당 팀**: application-team
- **추정 규모**: M

---

## Epic 4: Supplier 통합

> 필수 요구사항 6: "외부 숙소 공급자(Supplier)의 상품을 자사 플랫폼에 통합할 수 있어야 한다"

### STORY-401: Supplier 도메인 모델 구현
- **우선순위**: P0
- **구현 상태**: 미구현 (ERD에 Supplier, SupplierApiConfig, SupplierProperty, SupplierRoomType, SupplierSyncLog 정의됨)
- **수용기준**:
  - [ ] AC-1: Supplier.forNew()로 공급자 생성 시 status가 ACTIVE
  - [ ] AC-2: SupplierProperty에 supplierId, propertyId, supplierPropertyId 매핑 존재
  - [ ] AC-3: SupplierSyncLog에 동기화 결과(total/created/updated/deleted count) 기록 가능
  - [ ] AC-4: SupplierId record VO 구현
- **관련 레이어**: Domain
- **의존성**: 없음
- **담당 팀**: domain-team
- **추정 규모**: M

### STORY-402: Supplier Anti-Corruption Layer 구현
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: SupplierAdapter 인터페이스(Port)가 Application 레이어에 정의
  - [ ] AC-2: 외부 Supplier 모델 → 내부 Property/RoomType/Rate 모델 변환 로직 구현
  - [ ] AC-3: Supplier 응답을 Redis에 TTL 기반 캐싱 (동일 요청 반복 시 외부 API 호출 0회)
  - [ ] AC-4: Supplier API 실패 시 캐싱된 이전 데이터로 응답 (fallback)
- **관련 레이어**: Domain → Application → Adapter-out:supplier → Adapter-out:redis
- **의존성**: STORY-401
- **담당 팀**: application-team
- **추정 규모**: L

### STORY-403: Supplier 동기화 UseCase
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: SyncSupplierUseCase 실행 시 SupplierSyncLog에 결과 기록 (total, created, updated count)
  - [ ] AC-2: 외부 숙소가 이미 매핑된 경우 update, 신규이면 create
  - [ ] AC-3: 동기화된 숙소의 RatePlan.sourceType이 SUPPLIER로 설정
  - [ ] AC-4: 동기화된 숙소가 고객 검색 결과에 자사 숙소와 함께 노출
- **관련 레이어**: Domain → Application → Adapter-out:supplier → Adapter-out:mysql
- **의존성**: STORY-402
- **담당 팀**: application-team
- **추정 규모**: L

---

## Epic 5: 공통 인프라 및 프로젝트 기반

> 모든 Epic의 선행 조건. Adapter 계층 구현의 기반.

### STORY-501: 프로젝트 멀티모듈 Gradle 세팅
- **우선순위**: P0
- **구현 상태**: 부분 완료 (domain 모듈 존재, 나머지 모듈 구조 확인 필요)
- **수용기준**:
  - [ ] AC-1: ./gradlew build 성공 (core, domain, application, adapter-in-web, adapter-out-persistence, adapter-out-redis, adapter-out-supplier, infra 모듈 포함)
  - [ ] AC-2: domain 모듈이 spring, jpa 등 외부 프레임워크에 의존하지 않음 (ArchUnit 검증)
  - [ ] AC-3: application 모듈이 adapter 모듈에 의존하지 않음 (단방향 의존)
- **관련 레이어**: 전체
- **의존성**: 없음
- **담당 팀**: application-team
- **추정 규모**: M

### STORY-502: Docker Compose 환경 구성
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: docker-compose up으로 MySQL 8.x + Redis 컨테이너 기동
  - [ ] AC-2: Spring Boot 애플리케이션이 docker-compose 환경에 연결되어 정상 기동
  - [ ] AC-3: README.md에 실행 방법 기술
- **관련 레이어**: infra
- **의존성**: 없음
- **담당 팀**: application-team
- **추정 규모**: S

### STORY-503: 공통 응답 포맷 및 예외 처리
- **우선순위**: P0
- **구현 상태**: 미구현 (core에 ErrorCode, DomainException 존재)
- **수용기준**:
  - [ ] AC-1: 모든 API 성공 응답이 {"success": true, "data": {...}} 형식
  - [ ] AC-2: DomainException 발생 시 {"success": false, "error": {"code": "ACC-001", "message": "..."}} 형식 + 해당 httpStatus
  - [ ] AC-3: 검증 실패(MethodArgumentNotValidException) 시 400 응답 + 필드별 에러 메시지
  - [ ] AC-4: 예상치 못한 예외(Exception) 시 500 응답 + "INTERNAL_ERROR" 코드
- **관련 레이어**: Adapter-in:rest-api (GlobalExceptionHandler)
- **의존성**: 없음
- **담당 팀**: rest-api-team
- **추정 규모**: M

---

## Epic 6: Admin 관리 기능

> 선택 확장: "운영팀을 위한 Admin 관리 기능"

### STORY-601: Admin 숙소 목록/상세 조회 API
- **우선순위**: P1
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: GET /api/v1/admin/properties 요청 시 200 응답 + 페이지네이션된 숙소 목록
  - [ ] AC-2: status 필터 지원 (ACTIVE, INACTIVE, PENDING)
  - [ ] AC-3: GET /api/v1/admin/properties/{id} 요청 시 200 응답 + 숙소 상세 (객실, 요금 포함)
- **관련 레이어**: Application → Adapter-in:rest-api
- **의존성**: STORY-103
- **담당 팀**: rest-api-team
- **추정 규모**: M

### STORY-602: Admin 예약 모니터링 API
- **우선순위**: P1
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: GET /api/v1/admin/reservations 요청 시 200 응답 + 예약 목록 (status 필터, 날짜 범위 필터)
  - [ ] AC-2: 응답에 게스트 정보, 숙소명, 요금, 상태 포함
- **관련 레이어**: Application → Adapter-in:rest-api
- **의존성**: STORY-302
- **담당 팀**: rest-api-team
- **추정 규모**: S

---

## Epic 7: 이벤트 기반 아키텍처

> 선택 확장: "이벤트 기반 아키텍처 (설계 또는 간단한 구현)"

### STORY-701: Spring ApplicationEvent 기반 도메인 이벤트 구현
- **우선순위**: P1
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: 예약 확정 시 ReservationConfirmedEvent 발행
  - [ ] AC-2: 예약 취소 시 ReservationCancelledEvent 발행
  - [ ] AC-3: RateRule 변경 시 RateRuleChangedEvent 발행 → Rate 스냅샷 재계산 트리거
  - [ ] AC-4: 이벤트 리스너가 @TransactionalEventListener로 트랜잭션 완료 후 실행
  - [ ] AC-5: 이벤트 흐름이 설계 문서에 시퀀스 다이어그램으로 기록
- **관련 레이어**: Domain → Application → infra
- **의존성**: STORY-302
- **담당 팀**: application-team
- **추정 규모**: M

---

## Epic 8: 인증/인가

> 선택 확장: "보안: 인증/인가 설계"

### STORY-801: API 인증/인가 설계 문서
- **우선순위**: P2
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: docs/design/auth-design.md에 Extranet/Customer/Admin 각 API의 인증 방식 기술
  - [ ] AC-2: 파트너는 자신의 숙소만 관리 가능한 인가 규칙 설계
  - [ ] AC-3: JWT 기반 인증 흐름 다이어그램 포함
- **관련 레이어**: 설계 문서
- **의존성**: 없음
- **담당 팀**: application-team
- **추정 규모**: S

---

## Epic 9: 테스트 코드

> 권장 확장: "테스트 코드 (단위 테스트, 통합 테스트 등)"

### STORY-901: 도메인 단위 테스트
- **우선순위**: P1
- **구현 상태**: 부분 완료 (ArchUnit 테스트 1개 존재)
- **수용기준**:
  - [ ] AC-1: Property 도메인 모델 비즈니스 로직 테스트 (생성, 상태 변경, 검증 실패 케이스)
  - [ ] AC-2: Reservation 상태 전이 테스트 (PENDING→CONFIRMED, PENDING→CANCELLED, 불가능한 전이 시 예외)
  - [ ] AC-3: Rate 계산 로직 테스트 (요일별 가격, Override 적용)
  - [ ] AC-4: 순수 Java 테스트 (Spring Context 없이 실행)
  - [ ] AC-5: 테스트 커버리지: 도메인 모델 핵심 로직 80% 이상
- **관련 레이어**: Domain
- **의존성**: STORY-101, STORY-301
- **담당 팀**: domain-team
- **추정 규모**: M

### STORY-902: Repository 통합 테스트
- **우선순위**: P1
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: Testcontainers(MySQL) 환경에서 Property CRUD 테스트 통과
  - [ ] AC-2: Reservation 저장/조회 테스트 (ReservationItem 포함)
  - [ ] AC-3: Inventory 조회 시 날짜 범위 필터링 정상 동작
- **관련 레이어**: Adapter-out:mysql
- **의존성**: STORY-103
- **담당 팀**: persistence-mysql-team
- **추정 규모**: M

### STORY-903: API 통합 테스트 (MockMvc)
- **우선순위**: P1
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: 숙소 등록 → 객실 등록 → 요금 설정 → 검색 → 예약 → 취소 E2E 흐름 테스트
  - [ ] AC-2: 에러 응답 포맷 검증 (errorCode, message 필드 존재)
  - [ ] AC-3: MockMvc + Testcontainers 기반
- **관련 레이어**: Adapter-in:rest-api → Application → Adapter-out:mysql
- **의존성**: STORY-303
- **담당 팀**: rest-api-team
- **추정 규모**: L

---

## Epic 10: 문서화 및 기록

> 권장 확장: "과정 기록서, AI 활용 기록, API 문서 자동화"

### STORY-1001: API 문서 자동화 (SpringDoc/Swagger)
- **우선순위**: P1
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: /swagger-ui.html 접속 시 API 문서 페이지 표시
  - [ ] AC-2: 모든 API에 요청/응답 예시, 파라미터 설명 포함
  - [ ] AC-3: Extranet / Customer / Admin API가 태그로 그룹핑
- **관련 레이어**: Adapter-in:rest-api
- **의존성**: STORY-104
- **담당 팀**: rest-api-team
- **추정 규모**: S

### STORY-1002: 설계 문서 작성 (아키텍처, ERD, API 명세)
- **우선순위**: P1
- **구현 상태**: 부분 완료 (ERD v2, 도메인 컨벤션, 의사결정 시드 존재)
- **수용기준**:
  - [ ] AC-1: docs/design/architecture.md에 전체 아키텍처 다이어그램 + 모듈 간 의존성 설명
  - [ ] AC-2: docs/design/api-spec.md에 주요 API 명세 (요청/응답 예시 포함)
  - [ ] AC-3: docs/design/concurrency-design.md에 동시성 제어 전략 + 시퀀스 다이어그램
  - [ ] AC-4: 설계 문서에 적은 내용이 실제 코드에 반영되어 있음
- **관련 레이어**: 설계 문서
- **의존성**: 없음
- **담당 팀**: application-team
- **추정 규모**: M

### STORY-1003: 과정 기록서 (Progress Journal) 최종 정리
- **우선순위**: P1
- **구현 상태**: 부분 완료 (seeds/ 시드 데이터 존재)
- **수용기준**:
  - [ ] AC-1: docs/progress-journal.md에 일자별 작업 내용 + 의사결정 + AI 활용 기록 통합
  - [ ] AC-2: 각 의사결정에 "왜 이 방식인지, 대안은 무엇이었는지" 포함
  - [ ] AC-3: AI 활용 기록에 프롬프트 의도, 결과 활용 방식, 본인 판단 추가분 기술
- **관련 레이어**: 문서
- **의존성**: 없음
- **담당 팀**: application-team
- **추정 규모**: S

---

## Epic 11: 모니터링 설계

> 선택 확장: "모니터링 설계 (구현 불필요, 설계만으로도 가능)"

### STORY-1101: 모니터링 설계 문서
- **우선순위**: P2
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: docs/design/monitoring-design.md에 모니터링 대상 메트릭 정의 (예약 성공/실패율, 재고 정합성, Supplier 동기화 상태, 응답 시간)
  - [ ] AC-2: 알림 조건 정의 (예: 재고 불일치 > 5건, Supplier 동기화 실패 3회 연속)
  - [ ] AC-3: 로깅 전략 (구조화 로깅, 추적 ID, 로그 레벨 기준) 기술
- **관련 레이어**: 설계 문서
- **의존성**: 없음
- **담당 팀**: application-team
- **추정 규모**: S

---

## 구현 우선순위 요약

### P0 (필수 — 미구현 시 탈락 위험)
| 순서 | 스토리 | 핵심 | 규모 |
|:----:|--------|------|:----:|
| 1 | STORY-501 | Gradle 멀티모듈 세팅 | M |
| 2 | STORY-502 | Docker Compose (MySQL + Redis) | S |
| 3 | STORY-503 | 공통 응답 포맷 + 예외 처리 | M |
| 4 | STORY-101 | 숙소 도메인 모델 완성 | S |
| 5 | STORY-301 | 예약/재고 도메인 모델 | M |
| 6 | STORY-401 | Supplier 도메인 모델 | M |
| 7 | STORY-102 | 숙소 등록 UseCase | M |
| 8 | STORY-103 | 숙소 Persistence Adapter | L |
| 9 | STORY-104 | 숙소 등록 API (Extranet) | M |
| 10 | STORY-105 | 객실 등록 API (Extranet) | M |
| 11 | STORY-106 | 재고/요금 설정 API + Redis 캐시 | L |
| 12 | STORY-201 | 숙소 검색 API (Customer) | L |
| 13 | STORY-202 | 요금 조회 API + 캐싱 | L |
| 14 | STORY-302 | 예약 생성 + 동시성 제어 | L |
| 15 | STORY-303 | 예약 취소 API | M |
| 16 | STORY-304 | 동시성 제어 통합 테스트 | M |
| 17 | STORY-402 | Supplier ACL 구현 | L |
| 18 | STORY-403 | Supplier 동기화 UseCase | L |

### P1 (확장-높음 — 차별화 요소)
| 스토리 | 핵심 | 규모 |
|--------|------|:----:|
| STORY-601 | Admin 숙소 관리 API | M |
| STORY-602 | Admin 예약 모니터링 API | S |
| STORY-701 | 이벤트 기반 아키텍처 구현 | M |
| STORY-901 | 도메인 단위 테스트 | M |
| STORY-902 | Repository 통합 테스트 | M |
| STORY-903 | API E2E 통합 테스트 | L |
| STORY-1001 | Swagger API 문서 자동화 | S |
| STORY-1002 | 설계 문서 작성 | M |
| STORY-1003 | 과정 기록서 최종 정리 | S |

### P2 (확장-낮음 — 여유 시 진행)
| 스토리 | 핵심 | 규모 |
|--------|------|:----:|
| STORY-801 | 인증/인가 설계 문서 | S |
| STORY-1101 | 모니터링 설계 문서 | S |

---

## 의존성 그래프 (핵심 흐름)

```
STORY-501 (Gradle 세팅)
    └── STORY-502 (Docker Compose)
    └── STORY-503 (공통 응답/예외)
    └── STORY-101 (숙소 도메인) ─── STORY-301 (예약 도메인) ─── STORY-401 (Supplier 도메인)
            │                              │                            │
            ▼                              │                            │
        STORY-102 (숙소 UseCase)           │                            │
            │                              │                            │
            ▼                              │                            │
        STORY-103 (Persistence)            │                            │
            │                              │                            │
            ▼                              │                            │
        STORY-104 (숙소 등록 API)          │                            │
            │                              │                            │
            ▼                              │                            │
        STORY-105 (객실 등록 API)          │                            │
            │                              │                            │
            ▼                              │                            │
        STORY-106 (재고/요금 설정)         │                            │
            │                              │                            │
            ├──────────────────────────────┤                            │
            ▼                              ▼                            ▼
        STORY-201 (검색 API)          STORY-302 (예약 생성)        STORY-402 (Supplier ACL)
        STORY-202 (요금 조회)              │                            │
                                           ▼                            ▼
                                      STORY-303 (예약 취소)        STORY-403 (Supplier 동기화)
                                      STORY-304 (동시성 테스트)
```
