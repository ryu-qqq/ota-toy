# 외부 클라이언트 레이어 테스트 시나리오

> Circuit Breaker + Retry 동작, 예외 분류 체계, 장애 전파 차단을 검증한다.
> 총 **1개 테스트 클래스** (11개 테스트)

---

## 테스트 환경

| 항목 | 설정 |
|------|------|
| 프레임워크 | JUnit 5 + AssertJ |
| Resilience4j | CircuitBreaker, Retry 직접 생성 (테스트용 설정) |
| Mock 방식 | Supplier<T> 람다로 외부 호출 시뮬레이션 |
| 외부 의존 | **없음** (실제 HTTP 호출 없이 예외 시뮬레이션) |

### 테스트용 CB 설정 (프로덕션과 다름)

| 항목 | 프로덕션 | 테스트 | 이유 |
|------|---------|--------|------|
| slidingWindowSize | 20 | 4 | 적은 호출로 CB OPEN 유도 |
| minimumNumberOfCalls | 10 | 2 | 빠른 실패율 판단 |
| waitDuration | 60초 | 60초 | 동일 |
| Retry waitDuration | 지수 백오프 | 0ms | 테스트 속도 |

---

## 검증 카테고리

| 카테고리 | 코드 | 검증 내용 |
|----------|------|----------|
| 정상 흐름 | EC-1 | 정상 호출 시 결과 반환 |
| Retry 동작 | EC-2 | 재시도 횟수, 재시도 제외, 중간 성공 |
| CB 동작 | EC-3 | CB OPEN 전이, 4xx 제외, 임계값 미만 유지 |
| 예외 분류 | EC-4 | ErrorType, retryable, statusCode |

---

## SupplierApiExecutor (11개 테스트)

### 정상 흐름 (1개)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 정상 호출 | CB + Retry 통과 후 결과 반환 |

### Retry 동작 (4개)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | ServerException (5xx) → 3회 재시도 | callCount = 3, 최종 SupplierServerException 전파 |
| 2 | NetworkException → 3회 재시도 | callCount = 3, 최종 SupplierNetworkException 전파 |
| 3 | BadRequestException (4xx) → 재시도 안 함 | callCount = 1, 즉시 SupplierBadRequestException 전파 |
| 4 | 1차 실패 → 2차 성공 | callCount = 2, 정상 결과 반환 |

### Circuit Breaker 동작 (3개)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 실패율 초과 → CB OPEN | 연속 실패 → CB 상태 OPEN → ExternalServiceUnavailableException 발생 |
| 2 | BadRequest는 CB에 미기록 | 4xx 10회 → CB 상태 CLOSED 유지 |
| 3 | 실패율 임계값 미만 → CLOSED 유지 | 성공 3 + 실패 1 (25%) → CB CLOSED |

### 예외 분류 검증 (3개)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | ServerException | ErrorType = SERVER_ERROR, retryable = true, statusCode = 500 |
| 2 | NetworkException | ErrorType = NETWORK, retryable = true, statusCode = 0 |
| 3 | BadRequestException | ErrorType = BAD_REQUEST, retryable = false, statusCode = 400 |

---

## 검증 패턴별 요약

| 패턴 | 검증 위치 | 의미 |
|------|----------|------|
| **Retry 지수 백오프** | Retry 동작 #1~3 | retryable 예외만 재시도, non-retryable은 즉시 전파 |
| **CB 상태 전이** | CB 동작 #1 | CLOSED → OPEN (실패율 초과) |
| **CB 예외 필터링** | CB 동작 #2 | ignoreExceptions로 4xx를 CB 카운트에서 제외 |
| **계층 간 예외 변환** | CB 동작 #1 | CallNotPermittedException → ExternalServiceUnavailableException |
| **ErrorType 분류** | 예외 분류 #1~3 | retryable 여부가 예외 타입에 내장 |
