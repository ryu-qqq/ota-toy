# 외부 클라이언트 장애 대응 설계

> 외부 Supplier API 호출 시 장애 전파를 차단하고, 자동 복구하는 Resilience 패턴을 설명한다.

---

## 1. 설계 원칙

| 원칙 | 설명 |
|------|------|
| **장애 전파 차단** | 외부 서비스 장애가 내부 시스템으로 전파되지 않아야 한다 |
| **재시도 가능한 것만 재시도** | 5xx/네트워크는 재시도, 4xx는 재시도하지 않음 |
| **Application은 Adapter 예외를 모른다** | CB OPEN만 Application 레벨 예외로 전달, 나머지는 일반 Exception |
| **설정은 외부화** | 타임아웃, CB 임계값, Retry 횟수 모두 Properties로 관리 |

---

## 2. 아키텍처

### 계층별 장애 대응 흐름

```
[외부 Supplier API]
     ↓ HTTP 응답 / 네트워크 오류
[Adapter — MockSupplierClientAdapter]
     ↓ HTTP 상태코드별 예외 변환
[Support — SupplierApiExecutor]
     ↓ Retry (지수 백오프) → 실패 시 Circuit Breaker 기록
     ↓ CB OPEN → ExternalServiceUnavailableException
[Application — ExecuteSupplierTaskService]
     ↓ 예외 분기
     ├── ExternalServiceUnavailableException → deferRetry (retryCount 유지)
     └── 기타 Exception → markFailed (retryCount 증가)
```

### 모듈 구조

```
adapter-out/client/supplier-client/
├── config/
│   ├── SupplierClientProperties.java       ← 타임아웃 설정 외부화
│   └── SupplierCircuitBreakerConfig.java   ← CB + Retry 빈 설정
├── support/
│   └── SupplierApiExecutor.java            ← CB(바깥) + Retry(안쪽) 래핑 실행기
├── adapter/
│   └── MockSupplierClientAdapter.java      ← SupplierStrategy 구현체 (Mock)
└── exception/
    ├── SupplierClientException.java        ← 기본 클래스 + ErrorType enum
    ├── SupplierServerException.java        ← 5xx (retryable)
    ├── SupplierNetworkException.java       ← 네트워크 오류 (retryable)
    └── SupplierBadRequestException.java    ← 4xx (not retryable)
```

---

## 3. Circuit Breaker 설정

### 목적

외부 API가 지속적으로 실패할 때, 더 이상 호출하지 않고 즉시 실패시켜 시스템 자원을 보호한다.

### 설정값

| 항목 | 값 | 설명 |
|------|:--:|------|
| failureRateThreshold | 50% | 실패율 50% 초과 시 OPEN |
| slowCallRateThreshold | 80% | 느린 호출 80% 초과 시 OPEN |
| slowCallDurationThreshold | 3초 | 3초 이상이면 느린 호출 |
| slidingWindowType | COUNT_BASED | 최근 N건 기준 |
| slidingWindowSize | 20 | 최근 20건 기준 |
| minimumNumberOfCalls | 10 | 최소 10건 이상일 때 판단 시작 |
| permittedNumberOfCallsInHalfOpenState | 5 | Half-Open에서 5건 허용 |
| waitDurationInOpenState | 60초 | Open 상태 60초 유지 후 Half-Open 전환 |
| recordExceptions | ServerException, NetworkException | CB 카운트 대상 |
| ignoreExceptions | BadRequestException | CB 카운트 제외 (4xx는 외부 서비스 장애가 아님) |

### 상태 전이

```
CLOSED ──(실패율 50% 초과)──→ OPEN ──(60초 경과)──→ HALF_OPEN
                                                        │
                                    ├──(5건 중 실패율 정상)──→ CLOSED
                                    └──(5건 중 실패율 초과)──→ OPEN
```

### 왜 4xx를 CB에서 제외하는가

4xx는 요청 자체가 잘못된 것이지 외부 서비스의 장애가 아니다. 잘못된 요청이 CB를 열면 정상 요청까지 차단되는 오탐이 발생한다.

---

## 4. Retry 전략

### 설정값

| 항목 | 값 | 설명 |
|------|:--:|------|
| maxAttempts | 3 | 최대 3회 시도 |
| intervalFunction | 지수 백오프 | 100ms → 200ms → 400ms |
| retryExceptions | ServerException, NetworkException | 재시도 대상 |
| ignoreExceptions | BadRequestException | 재시도 제외 |

### 실행 순서: CB(바깥) + Retry(안쪽)

```java
// SupplierApiExecutor
Supplier<T> retryDecorated = Retry.decorateSupplier(retry, supplier);
return circuitBreaker.executeSupplier(retryDecorated);
```

```
1차 시도 → 실패 (5xx)
  → 100ms 대기 → 2차 시도 → 실패
    → 200ms 대기 → 3차 시도 → 실패
      → CB에 실패 기록 → 예외 전파
```

Retry를 CB 안쪽에 두는 이유: Retry가 모두 실패한 뒤에 CB에 1건만 기록된다. Retry 매 시도가 CB에 기록되면 3배 빠르게 CB가 열린다.

---

## 5. 예외 분류 체계

### HTTP 상태코드 → 예외 변환

```java
// Adapter에서 변환
try {
    // RestClient 호출
} catch (HttpServerErrorException e) {        // 5xx
    throw new SupplierServerException(e.getStatusCode().value(), e.getMessage(), e);
} catch (HttpClientErrorException e) {        // 4xx
    throw new SupplierBadRequestException(e.getStatusCode().value(), e.getMessage());
} catch (ResourceAccessException e) {         // 타임아웃, 연결 실패
    throw new SupplierNetworkException(e.getMessage(), e);
}
```

### ErrorType 분류

```java
public enum ErrorType {
    SERVER_ERROR(true),    // 5xx — 재시도 O, CB 기록 O
    NETWORK(true),         // 네트워크 — 재시도 O, CB 기록 O
    BAD_REQUEST(false),    // 4xx — 재시도 X, CB 기록 X
    CIRCUIT_OPEN(false),   // CB OPEN — 즉시 실패
    UNKNOWN(false);        // 미분류
}
```

### 예외 전달 경로

| 상황 | Adapter 예외 | Retry | CB 기록 | Application 수신 | Task 처리 |
|------|-------------|:-----:|:------:|-----------------|----------|
| 5xx 서버 오류 | SupplierServerException | 3회 | O | Exception (일반) | markFailed + retryCount++ |
| 네트워크 타임아웃 | SupplierNetworkException | 3회 | O | Exception (일반) | markFailed + retryCount++ |
| 4xx 잘못된 요청 | SupplierBadRequestException | X | X | Exception (일반) | markFailed + retryCount++ |
| CB OPEN | — | X | X | ExternalServiceUnavailableException | deferRetry (retryCount 유지) |

### 왜 Application은 ExternalServiceUnavailableException만 구분하는가

Application 레이어가 Adapter의 세부 예외(Server/Network/BadRequest)를 알면 계층 경계가 깨진다. Application이 알아야 하는 것은 딱 하나: **"외부 서비스 자체가 불가한 상태인가?"** 이것만 `ExternalServiceUnavailableException`으로 전달하고, 나머지는 일반 `Exception`으로 처리한다.

---

## 6. 실패 사유 기록

### SupplierTaskFailureReason

외부 호출 실패 시 구조화된 실패 사유를 JSON으로 Task에 기록한다.

```java
public record SupplierTaskFailureReason(
    Integer httpStatus,     // 500, 429, null(네트워크)
    String errorCode,       // 공급자 에러 코드 (있으면)
    String errorMessage,    // 상세 메시지
    Instant occurredAt      // 실패 시각
)
```

기록 예시:
```json
{"httpStatus":500,"errorCode":null,"errorMessage":"Connection refused","occurredAt":"2026-04-06T12:00:00Z"}
{"httpStatus":429,"errorCode":"RATE_LIMITED","errorMessage":"Too many requests","occurredAt":"2026-04-06T12:01:00Z"}
{"httpStatus":null,"errorCode":null,"errorMessage":"Read timed out","occurredAt":"2026-04-06T12:02:00Z"}
```

운영 시 이 데이터로 공급자별 장애 패턴을 분석할 수 있다.

---

## 7. 설정 외부화

### SupplierClientProperties

```yaml
supplier-client:
  connect-timeout: 5s
  read-timeout: 10s
```

### SupplierCircuitBreakerConfig

CB와 Retry 설정은 `@Configuration`에서 빈으로 등록한다. 값을 Properties로 분리하면 환경별 튜닝이 가능하다.

```java
@Bean
public CircuitBreaker supplierCircuitBreaker() {
    CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .slidingWindowSize(20)
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .recordExceptions(SupplierServerException.class, SupplierNetworkException.class)
            .ignoreExceptions(SupplierBadRequestException.class)
            .build();
    return CircuitBreaker.of("supplier", config);
}

@Bean
public Retry supplierRetry() {
    RetryConfig config = RetryConfig.custom()
            .maxAttempts(3)
            .intervalFunction(attempt -> 100L * (long) Math.pow(2, attempt - 1))
            .retryExceptions(SupplierServerException.class, SupplierNetworkException.class)
            .ignoreExceptions(SupplierBadRequestException.class)
            .build();
    return Retry.of("supplier", config);
}
```

---

## 8. 코드 위치

| 구성 요소 | 위치 |
|----------|------|
| CB + Retry 설정 | `adapter-out/client/supplier-client/config/SupplierCircuitBreakerConfig.java` |
| 타임아웃 설정 | `adapter-out/client/supplier-client/config/SupplierClientProperties.java` |
| CB + Retry 실행기 | `adapter-out/client/supplier-client/support/SupplierApiExecutor.java` |
| Mock Adapter | `adapter-out/client/supplier-client/adapter/MockSupplierClientAdapter.java` |
| 예외 계층 | `adapter-out/client/supplier-client/exception/` |
| Application 예외 | `application/common/exception/ExternalServiceUnavailableException.java` |
| 실패 사유 VO | `domain/supplier/SupplierTaskFailureReason.java` |
