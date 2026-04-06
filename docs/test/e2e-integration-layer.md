# E2E 통합 테스트

> Testcontainers 기반 전체 서버 기동 후 실제 HTTP 호출로 과제 필수 요구사항을 검증한다.

---

## 개요

MockMvc 기반 슬라이스 테스트와 달리, E2E 테스트는 **실제 서버를 띄우고 HTTP 요청**을 보낸다.
MySQL + Redis Testcontainer가 기동되고, Flyway 마이그레이션이 적용되며, Spring Context 전체가 로드된다.
Controller → UseCase → Domain → Adapter 전 계층이 연결된 상태에서 검증한다.

### 슬라이스 테스트 vs E2E 테스트

| 관점 | 슬라이스 (MockMvc) | E2E (Testcontainers) |
|------|-------------------|---------------------|
| 서버 | 미기동 | 실제 기동 (RANDOM_PORT) |
| DB | Mock | 실제 MySQL 컨테이너 |
| Redis | Mock | 실제 Redis 컨테이너 |
| 검증 범위 | Controller ↔ UseCase | 전 계층 통합 |
| 속도 | 빠름 | 느림 (컨테이너 기동) |
| 용도 | API 포맷 검증 | 비즈니스 흐름 검증 |

---

## 테스트 인프라

### Testcontainers 설정

```
ExtranetE2ETestBase / CustomerE2ETestBase
  ├── MySQL 8.0 싱글톤 컨테이너 (Flyway 자동 적용)
  ├── Redis 7 싱글톤 컨테이너 (Redisson 연동)
  ├── @SpringBootTest(RANDOM_PORT)
  ├── TestRestTemplate (HTTP 클라이언트)
  └── @BeforeEach 데이터 초기화 (FK 역순 삭제)
```

### 데이터 격리

테스트 간 데이터 의존을 방지하기 위해 `@BeforeEach`에서 FK 역순으로 전체 삭제:

```
reservationItem → reservation → reservationSession
inventory → rate → rateOverride → rateRule → ratePlan
roomTypeBed → roomTypeView → roomType
propertyPhoto → propertyAmenity → propertyAttributeValue → property
```

### 사전 데이터 삽입

JPA Entity의 `create()` 팩토리 + EntityManager.persist()로 직접 삽입한다.
domain testFixtures가 아닌 **Entity 레벨 직접 삽입**을 사용하는 이유:
E2E 테스트는 Adapter 계층까지 포함하므로 DB에 실제 레코드가 있어야 한다.

---

## 테스트 파일 구조

```
bootstrap/bootstrap-extranet/src/test/java/com/ryuqq/otatoy/e2e/
├── ExtranetE2ETestBase.java          — 베이스 클래스
└── ExtranetPropertyE2ETest.java      — Extranet 전체 흐름

bootstrap/bootstrap-customer/src/test/java/com/ryuqq/otatoy/e2e/
├── CustomerE2ETestBase.java          — 베이스 클래스
├── CustomerReservationE2ETest.java   — 예약 흐름 + 멱등성 + 에러
├── CustomerSearchE2ETest.java        — 검색 + 요금 조회
└── CustomerConcurrencyE2ETest.java   — 동시성 테스트
```

---

## 과제 필수 요구사항 매핑

| 요구사항 | E2E 테스트 | 시나리오 |
|---------|-----------|---------|
| 숙소 등록/관리 | ExtranetPropertyE2ETest | 등록 → 사진/편의시설/속성 설정 → 상세 조회 |
| 객실/요금 설정 | ExtranetPropertyE2ETest | 객실 등록 → 요금 정책 → 요금/재고 설정 |
| 숙소 검색 | CustomerSearchE2ETest | 지역 검색 → 결과 확인 |
| 요금 조회 | CustomerSearchE2ETest | 날짜/인원 기반 요금 조회 |
| 예약/취소 | CustomerReservationE2ETest | 세션 생성 → 확정 → 취소 + DB 상태 검증 |
| 동시성 제어 | CustomerConcurrencyE2ETest | 10 스레드 동시 요청, 재고 1개 → 1건 성공 |

---

## 시나리오별 상세

### Extranet 전체 흐름 (P0)

```
1. POST /properties          → 201 (propertyId 획득)
2. PUT  /properties/{id}/photos    → 200 (사진 2장 설정)
3. PUT  /properties/{id}/amenities → 200 (편의시설 2개 설정)
4. PUT  /properties/{id}/attributes → 200 (속성값 1개 설정)
5. GET  /properties/{id}           → 200 (사진/편의시설/속성 포함 확인)
6. POST /properties/{id}/rooms     → 201 (roomTypeId 획득)
7. POST /.../rooms/{id}/rate-plans → 201 (ratePlanId 획득)
8. PUT  /rate-plans/{id}/rates     → 200 (요금/재고 설정)
9. DB 검증: rate_rule, rate, inventory 레코드 생성 확인
```

### 예약 전체 흐름 (P0)

```
1. POST /reservation-sessions  → 201 (sessionId, Idempotency-Key 헤더)
   → DB: reservation_session 생성, inventory.availableCount 감소
2. POST /reservations           → 201 (reservationId)
   → DB: reservation 생성, session.status = CONFIRMED
3. PATCH /reservations/{id}/cancel → 200
   → DB: reservation.status = CANCELLED, inventory.availableCount 복구
```

### 동시성 테스트 (P0)

```
사전 조건: inventory.availableCount = 1

ExecutorService(10 스레드) + CountDownLatch
  → 10개 동시 POST /reservation-sessions
  → 각기 다른 Idempotency-Key

검증:
  - 성공: 정확히 1건 (201)
  - 실패: 정확히 9건 (409 INV-002 재고 소진)
  - DB: inventory.availableCount = 0
```

### 에러 케이스 (P1)

| 시나리오 | 기대 응답 |
|---------|----------|
| 존재하지 않는 숙소 조회 | 404 + RFC 7807 ProblemDetail |
| 멱등키 중복 세션 생성 | 201 + 동일 sessionId 반환 |
| 이미 취소된 예약 재취소 | 409 (RSV-003) |
| 없는 지역 검색 | 200 + 빈 content 배열 |

---

## 실행 방법

```bash
# Extranet E2E 테스트
./gradlew :bootstrap:bootstrap-extranet:test --tests "*E2ETest*"

# Customer E2E 테스트
./gradlew :bootstrap:bootstrap-customer:test --tests "*E2ETest*"

# 전체 E2E
./gradlew test --tests "*E2ETest*"
```

> Docker가 실행 중이어야 한다 (Testcontainers가 컨테이너를 자동 기동).
> `@Tag("e2e")`로 분류되어 일반 단위 테스트와 분리 실행 가능.

