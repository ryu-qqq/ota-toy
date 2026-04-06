# 테스트 전략 문서

> OTA 숙박 플랫폼 백엔드의 전체 테스트 전략과 레이어별 검증 시나리오를 정리한다.

---

## 1. 테스트 피라미드 구조

```
              ┌───────────────┐
              │  REST API     │  MockMvc + REST Docs
              │ (21개 클래스)  │  Controller + Mapper + ErrorMapper
              ├───────────────┤
              │  Application  │  Mockito Mock 기반 단위 테스트
              │ (61개 클래스)  │  Service + Factory + Validator + Facade + Manager
              ├───────────────┤
              │  Persistence  │  Testcontainers 통합 테스트
              │ (27개 클래스)  │  MySQL 24 + Redis 3
              ├───────────────┤
              │    Domain     │  순수 Java 단위 테스트
              │ (89개 클래스)  │  Aggregate + VO + Enum + ErrorCode + ArchUnit
              └───────────────┘
```

| 레이어 | 테스트 유형 | 클래스 수 | 환경 |
|--------|-----------|---------|------|
| Domain | 단위 테스트 | 89개 | 순수 Java (외부 의존 없음) |
| Application | 단위 테스트 | 61개 | Mockito Mock 기반 |
| Persistence MySQL | 통합 테스트 | 24개 | Testcontainers MySQL 8.0 + Flyway |
| Persistence Redis | 통합 테스트 | 3개 | Testcontainers Redis 7.2 |
| REST API Extranet | 슬라이스 + 단위 | 13개 | MockMvc + 순수 Java |
| REST API Customer | 슬라이스 + 단위 | 8개 | MockMvc + 순수 Java |

**총 198개 테스트 클래스**

---

## 2. 레이어별 테스트 상세

각 레이어의 상세 시나리오는 별도 문서에 기술한다.

| 문서 | 경로 | 설명 |
|------|------|------|
| Domain 테스트 | [domain-layer.md](domain-layer.md) | 도메인 모델 비즈니스 규칙 검증 (1,019개 테스트) |
| Application 테스트 | [application-layer.md](application-layer.md) | UseCase + Factory + Validator + Facade + Manager 검증 |
| Persistence 테스트 | [persistence-layer.md](persistence-layer.md) | MySQL 168개 + Redis 23개 통합 테스트 |
| REST API 테스트 | [rest-api-layer.md](rest-api-layer.md) | Controller + Mapper + ErrorMapper 검증 (202개) |
| E2E 통합 테스트 | [e2e-integration-layer.md](e2e-integration-layer.md) | Testcontainers 전체 흐름 + 동시성 검증 (11개) |

---

## 3. 테스트 실행 방법

```bash
# 전체 테스트
./gradlew test

# 레이어별 실행
./gradlew :domain:test
./gradlew :application:test
./gradlew :adapter-out:persistence-mysql:test
./gradlew :adapter-out:persistence-redis:test
./gradlew :adapter-in:rest-api-extranet:test
./gradlew :adapter-in:rest-api-customer:test

# 특정 테스트 클래스 실행
./gradlew :adapter-out:persistence-mysql:test --tests "*.ReservationPersistenceAdapterTest"
```

> Testcontainers 기반 테스트(Persistence, Redis)는 Docker가 실행 중이어야 한다.

---

## 4. 테스트 설계 원칙

1. **실제 인프라 검증** — H2 대신 Testcontainers MySQL/Redis로 운영 환경과 동일한 동작 보장
2. **Singleton 컨테이너** — JVM당 1회 기동으로 테스트 속도 최적화
3. **테스트 격리** — `@Transactional` 자동 롤백(MySQL), `@BeforeEach` 키 정리(Redis)
4. **Fixture 패턴** — `testFixtures` 소스셋으로 도메인 객체 생성 헬퍼 공유
5. **동시성 검증** — ExecutorService + CountDownLatch로 실제 멀티스레드 경합 테스트
6. **레이어별 독립 검증** — 각 레이어가 자신의 책임만 검증 (Domain은 순수 로직, Application은 흐름, Persistence는 DB 연동)

---

## 5. E2E 통합 테스트 시나리오 (설계)

> Testcontainers(MySQL + Redis) 기반 실제 서버 기동 후 TestRestTemplate으로 전체 흐름을 검증한다.

### Extranet 시나리오

| ID | 시나리오 | 우선순위 |
|----|---------|---------|
| EXT-E2E-001 | 숙소 등록 → 사진/편의시설/속성값 설정 → 상세 조회 | P0 |
| EXT-E2E-002 | 객실 등록 → 요금 정책 등록 → 요금/재고 설정 | P0 |
| EXT-E2E-003 | 숙소 목록 커서 페이지네이션 (3건, size=2) | P1 |
| EXT-E2E-004 | 존재하지 않는 숙소 조회 → 404 ProblemDetail | P1 |

### Customer 시나리오

| ID | 시나리오 | 우선순위 |
|----|---------|---------|
| CUST-E2E-001 | 예약 세션 생성 → 확정 → 취소 (전체 흐름) | P0 |
| CUST-E2E-002 | 멱등키 중복 → 동일 세션 반환 | P1 |
| CUST-E2E-003 | 이미 취소된 예약 재취소 → 409 Conflict | P1 |
| CUST-E2E-004 | 숙소 검색 (지역 필터, 커서 페이지네이션) | P0 |
| CUST-E2E-005 | 요금 조회 (dailyRates, totalPrice) | P0 |
| CUST-E2E-006 | **동시 10건 예약 (재고 1개)** → 1건만 성공, 9건 409 | P0 |

---

## 6. 에러 핸들링 전략

### GlobalExceptionHandler

| 예외 | HTTP 상태 | 에러 코드 |
|------|----------|----------|
| `DomainException` | ErrorMapper 결정 | 도메인 에러 코드 |
| `MethodArgumentNotValidException` | 400 | VALIDATION_FAILED |
| `BindException` | 400 | BINDING_FAILED |
| `MissingServletRequestParameterException` | 400 | MISSING_PARAMETER |
| `ServletRequestBindingException` | 400 | MISSING_HEADER |
| `HttpMessageNotReadableException` | 400 | INVALID_FORMAT |
| `MethodArgumentTypeMismatchException` | 400 | TYPE_MISMATCH |
| `IllegalArgumentException` | 400 | INVALID_ARGUMENT |
| `DateTimeParseException` | 400 | INVALID_DATETIME_FORMAT |
| `HttpRequestMethodNotSupportedException` | 405 | METHOD_NOT_ALLOWED |
| `Exception` (fallback) | 500 | INTERNAL_ERROR |
