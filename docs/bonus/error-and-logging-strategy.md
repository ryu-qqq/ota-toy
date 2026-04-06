# 에러 처리 및 로깅 전략

> 시스템 전반의 에러 처리 구조, 로깅 아키텍처, 민감 정보 보호, 운영 환경 대응을 설명한다.

---

## 1. 설계 원칙

| 원칙 | 설명 |
|------|------|
| **에러는 도메인에서 발생, Adapter에서 변환** | Domain은 `DomainException`을 던지고, REST Adapter가 RFC 7807로 변환 |
| **에러 응답은 국제 표준** | RFC 7807 (Problem Details) + `application/problem+json` |
| **Domain은 HTTP를 모른다** | `ErrorCategory`만 정의, HTTP 매핑은 Adapter 책임 |
| **로깅은 관찰 가능성(Observability)의 기반** | 요청 추적, 에러 분류, 성능 측정을 로그로 수행 |
| **민감 정보는 로그에 남기지 않는다** | PII(전화번호, 이메일, 이름)는 마스킹 처리 |

---

## 2. 에러 처리 아키텍처

### 흐름

```
[Client 요청]
     ↓
[RequestResponseLoggingFilter] → traceId 생성 → MDC 설정
     ↓
[Controller] → UseCase 호출
     ↓
[Domain] → 비즈니스 규칙 위반 시 DomainException 발생
     ↓
[GlobalExceptionHandler] → ErrorMapperRegistry → ErrorMapper 구현체
     ↓
[RFC 7807 ProblemDetail 응답] + x-error-code 헤더
     ↓
[RequestResponseLoggingFilter] → 응답 상태 + 처리 시간 로깅 → MDC 정리
```

### 계층별 책임

| 계층 | 에러 처리 | 로깅 |
|------|----------|------|
| **Filter** | - | 요청/응답 로깅, traceId 생성 |
| **Controller** | 예외를 잡지 않음 | - |
| **Application** | Domain 예외를 그대로 전파 | - |
| **Domain** | `DomainException` 발생 | - |
| **GlobalExceptionHandler** | HTTP 응답으로 변환 | 에러 상세 로깅 |

---

## 3. 에러 응답 포맷 (RFC 7807)

### 도메인 에러
```json
HTTP/1.1 404 Not Found
Content-Type: application/problem+json
x-error-code: ACC-001

{
  "type": "about:blank",
  "title": "Accommodation Error",
  "status": 404,
  "detail": "숙소를 찾을 수 없습니다",
  "instance": "/api/v1/extranet/properties/999",
  "timestamp": "2026-04-06T06:30:00.000Z",
  "code": "ACC-001",
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Validation 에러
```json
HTTP/1.1 400 Bad Request
Content-Type: application/problem+json
x-error-code: VALIDATION_FAILED

{
  "type": "about:blank",
  "title": "Validation Failed",
  "status": 400,
  "detail": "입력값이 올바르지 않습니다",
  "instance": "/api/v1/extranet/properties",
  "code": "VALIDATION_FAILED",
  "errors": {
    "name": "must not be blank",
    "partnerId": "must not be null"
  }
}
```

### 왜 RFC 7807인가

| 관점 | 자체 포맷 | RFC 7807 |
|------|----------|----------|
| 표준화 | 팀마다 다름 | 국제 표준, 클라이언트 라이브러리 호환 |
| Content-Type | `application/json` 구분 불가 | `application/problem+json`으로 에러 즉시 식별 |
| 확장성 | 필드 추가 시 합의 필요 | `setProperty()`로 자유 확장 |
| Spring 지원 | 직접 구현 | `ProblemDetail` 클래스 내장 (6.0+) |

---

## 4. 에러 코드 체계

### 구조: `{BC 접두사}-{번호}`

에러 코드의 접두사만으로 어떤 Bounded Context에서 발생한 에러인지 즉시 식별할 수 있다.

| 접두사 | BC | 예시 |
|--------|---|------|
| `ACC` | 숙소/객실 | ACC-001: 숙소 없음, ACC-005: 유효하지 않은 객실 |
| `PRC` | 요금 | PRC-001: 요금 정책 없음 |
| `INV` | 재고 | INV-002: 재고 소진, INV-003: 판매 중지 |
| `RSV` | 예약 | RSV-003: 이미 취소, RSV-006: 세션 만료 |
| `PTN` | 파트너 | PTN-001: 파트너 없음 |
| `SUP` | 공급자 | SUP-001: 공급자 없음 |

### ErrorCategory → HTTP 매핑

Domain은 HTTP를 모른다. `ErrorCategory`만 정의하고, Adapter에서 HTTP 상태로 변환한다.

```
ErrorCategory.NOT_FOUND     →  404 Not Found
ErrorCategory.VALIDATION    →  400 Bad Request
ErrorCategory.CONFLICT      →  409 Conflict
ErrorCategory.FORBIDDEN     →  422 Unprocessable Entity
```

> FORBIDDEN이 403이 아닌 422인 이유: "인증/인가 실패"가 아니라 "비즈니스적으로 금지된 행위"(판매 중지된 재고 접근 등)를 의미하므로 422로 매핑한다.

### ErrorMapper 패턴 (OCP 준수)

```java
// 인터페이스
public interface ErrorMapper {
    boolean supports(DomainException ex);
    MappedError map(DomainException ex);
}

// Registry — Spring이 구현체를 자동 수집
@Component
public class ErrorMapperRegistry {
    private final List<ErrorMapper> mappers;
    
    public MappedError resolve(DomainException ex) {
        return mappers.stream()
            .filter(m -> m.supports(ex))
            .findFirst()
            .map(m -> m.map(ex))
            .orElse(defaultMapping(ex));
    }
}
```

새로운 BC가 추가되면 ErrorMapper 구현체만 추가하면 된다 (기존 코드 수정 없음).

### GlobalExceptionHandler 처리 범위

| 예외 | 에러 코드 | HTTP |
|------|----------|------|
| `DomainException` | BC별 코드 | ErrorCategory에 따라 |
| `MethodArgumentNotValidException` | VALIDATION_FAILED | 400 |
| `BindException` | BINDING_FAILED | 400 |
| `MissingServletRequestParameterException` | MISSING_PARAMETER | 400 |
| `HttpMessageNotReadableException` | INVALID_FORMAT | 400 |
| `MethodArgumentTypeMismatchException` | TYPE_MISMATCH | 400 |
| `HttpRequestMethodNotSupportedException` | METHOD_NOT_ALLOWED | 405 |
| `Exception` (최종 안전망) | INTERNAL_ERROR | 500 |

**Controller에서 try-catch를 사용하지 않는다.** 모든 예외는 GlobalExceptionHandler에서 일괄 처리하여 응답 포맷의 일관성을 보장한다.

---

## 5. 로깅 아키텍처

### 무엇을 로깅하는가

| 대상 | 로깅 내용 | 로깅하지 않는 것 |
|------|----------|----------------|
| **요청** | HTTP 메서드, URI, 요청 시각 | 요청 Body (PII 위험) |
| **응답** | HTTP 상태, 처리 시간(ms) | 응답 Body (성능, 데이터 크기) |
| **에러** | 에러 코드, 상세 메시지, traceId | 민감 정보 (마스킹 처리) |
| **비즈니스 이벤트** | 예약 생성/확정/취소, 재고 차감 | - |

### 로그 포맷

```
2026-04-06 15:30:00.123 [http-nio-8080-exec-1] [550e8400-...] INFO  c.r.o.a.c.l.RequestResponseLoggingFilter - [550e8400-...] POST /api/v1/reservations → 201 (45ms)
```

| 필드 | 설명 |
|------|------|
| 타임스탬프 | `yyyy-MM-dd HH:mm:ss.SSS` (KST) |
| 스레드 | 요청을 처리한 스레드명 |
| traceId | MDC에서 추출한 분산 추적 ID |
| 레벨 | INFO / WARN / ERROR / DEBUG |
| 로거 | 클래스명 (축약) |
| 메시지 | 구조화된 로그 메시지 |

### 요청 추적 (traceId)

```
[RequestResponseLoggingFilter]
  ↓ traceId = UUID.randomUUID()
  ↓ MDC.put("traceId", traceId)
  ↓
[Controller → Service → Domain → Adapter]
  ↓ 모든 로그에 traceId 자동 포함
  ↓
[GlobalExceptionHandler]
  ↓ 에러 응답에 traceId 포함
  ↓
[RequestResponseLoggingFilter]
  ↓ MDC.clear()
```

하나의 요청에서 발생한 모든 로그를 `traceId`로 묶어서 추적할 수 있다.

### Swagger/API Docs 경로는 로깅에서 제외

```java
@Override
protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/swagger") 
        || path.startsWith("/api-docs") 
        || path.startsWith("/actuator");
}
```

---

## 6. 로그 레벨 전략

### 요청/응답 로깅

| HTTP 상태 | 로그 레벨 | 이유 |
|----------|----------|------|
| 2xx | INFO | 정상 요청 기록 |
| 4xx | WARN | 클라이언트 오류 추적 |
| 5xx | ERROR | 즉시 조치 필요 |

### 에러 로깅

| 대상 | 로그 레벨 | 이유 |
|------|----------|------|
| 5xx 서버 오류 | ERROR + 스택트레이스 | 즉시 조치. 원인 파악을 위해 스택트레이스 필수 |
| 404 Not Found | DEBUG | 정상 흐름 (검색 엔진 크롤러, 삭제된 링크). WARN으로 남기면 노이즈 |
| 나머지 4xx | WARN | 비정상 요청이지만 서버 장애는 아님 |

> **왜 404를 DEBUG로 하는가**: OTA에서 "존재하지 않는 숙소 조회"는 매우 빈번한 정상 시나리오다 (검색 엔진, 삭제된 숙소 링크 등). WARN으로 기록하면 실제 경고를 놓치게 된다.

### 환경별 로그 레벨

| 패키지 | 개발(local) | 운영(prod) |
|--------|-----------|-----------|
| `com.ryuqq.otatoy` | DEBUG | INFO |
| `org.springframework` | WARN | WARN |
| `org.hibernate` | WARN | WARN |
| Root | INFO | INFO |

---

## 7. 민감 정보 보호 (PII 마스킹)

### 마스킹 대상

| 대상 | 원본 | 마스킹 결과 |
|------|------|-----------|
| 전화번호 | 010-1234-5678 | 010-****-5678 |
| 이메일 | ryu@example.com | r***@example.com |
| 이름 | 홍길동 | 홍*동 |

### 적용 범위

- **로그 메시지**: PII가 포함될 수 있는 예외 메시지, 디버그 로그에서 마스킹
- **에러 응답**: `detail` 필드에 사용자 입력이 포함되는 경우 마스킹
- **요청 Body**: 로깅하지 않음 (마스킹보다 비로깅이 안전)

### 구현

```java
public final class PiiMaskingUtils {
    public static String maskPhone(String phone) {
        // 010-1234-5678 → 010-****-5678
    }
    public static String maskEmail(String email) {
        // ryu@example.com → r***@example.com
    }
    public static String maskName(String name) {
        // 홍길동 → 홍*동
    }
}
```

---

## 8. 운영 환경 대응

### JSON 구조화 로깅 (prod)

운영 환경에서는 ELK Stack이나 CloudWatch 연동을 위해 JSON 형식 로깅으로 전환한다.

```json
{
  "timestamp": "2026-04-06T15:30:00.123+09:00",
  "level": "ERROR",
  "thread": "http-nio-8080-exec-1",
  "logger": "c.r.o.a.c.GlobalExceptionHandler",
  "message": "DomainException (Server Error): code=INV-002",
  "traceId": "550e8400-...",
  "exception": "com.ryuqq.otatoy.domain.inventory.InventoryExhaustedException"
}
```

전환 방법: `logstash-logback-encoder` 의존성 추가 + prod 프로파일에 LogstashEncoder 설정.

### 모니터링 연동 포인트

로그 기반으로 다음 지표를 모니터링할 수 있다:

| 지표 | 추출 방법 | 알림 기준 |
|------|----------|----------|
| API 응답 시간 | 요청 로그의 처리 시간(ms) | P99 > 500ms |
| 에러율 | ERROR 레벨 로그 빈도 | 5분간 5xx 10건 이상 |
| 재고 소진 빈도 | INV-002 코드 발생 빈도 | 특정 객실 1시간 내 50건 이상 |
| 예약 세션 만료율 | RSV-006 코드 발생 빈도 | 10분간 만료 비율 > 30% |
| Redis-DB 불일치 | 복구 스케줄러 로그 | 불일치 건수 > 0 |

### 로그 보존 정책 (권장)

| 환경 | 보존 기간 | 형태 |
|------|----------|------|
| 개발 | 콘솔 출력 (파일 미저장) | 텍스트 |
| 스테이징 | 7일 | JSON (파일) |
| 운영 | 30일 (INFO), 90일 (ERROR) | JSON (중앙 수집) |

---

## 9. 예외 계층 구조

```
RuntimeException
  └── DomainException (abstract)
        ├── PropertyNotFoundException        (ACC-001, NOT_FOUND)
        ├── RoomTypeNotFoundException        (ACC-002, NOT_FOUND)
        ├── InvalidRoomTypeException         (ACC-005, VALIDATION)
        ├── RequiredPropertyAttributeMissingException (ACC-006, VALIDATION)
        ├── RatePlanNotFoundException        (PRC-001, NOT_FOUND)
        ├── InventoryNotFoundException       (INV-001, NOT_FOUND)
        ├── InventoryExhaustedException      (INV-002, CONFLICT)
        ├── InventoryStopSellException       (INV-003, FORBIDDEN)
        ├── ReservationNotFoundException     (RSV-001, NOT_FOUND)
        ├── ReservationAlreadyCancelledException (RSV-003, CONFLICT)
        ├── ReservationSessionExpiredException   (RSV-006, CONFLICT)
        ├── PartnerNotFoundException         (PTN-001, NOT_FOUND)
        └── SupplierNotFoundException        (SUP-001, NOT_FOUND)
```

설계 원칙:
- **예외 클래스 1개 = ErrorCode 1개** — 예외명만으로 에러를 식별
- **DomainException은 abstract** — 반드시 구체 예외 클래스를 만들어야 함
- **ErrorCode는 enum** — 타입 안전성 + IDE 자동완성
- **args로 컨텍스트 전달** — `Map<String, Object>`로 디버깅 정보 첨부

---

## 10. 관련 코드 위치

| 항목 | 경로 |
|------|------|
| GlobalExceptionHandler | `adapter-in/rest-api-core/.../api/core/GlobalExceptionHandler.java` |
| ErrorMapper 인터페이스 | `adapter-in/rest-api-core/.../api/core/ErrorMapper.java` |
| ErrorMapperRegistry | `adapter-in/rest-api-core/.../api/core/ErrorMapperRegistry.java` |
| 요청/응답 로깅 필터 | `adapter-in/rest-api-core/.../api/core/logging/RequestResponseLoggingFilter.java` |
| PII 마스킹 유틸 | `adapter-in/rest-api-core/.../api/core/logging/PiiMaskingUtils.java` |
| Logback 설정 | `adapter-in/rest-api-core/src/main/resources/logback-spring.xml` |
| DomainException | `domain/.../domain/common/DomainException.java` |
| ErrorCode 인터페이스 | `domain/.../domain/common/ErrorCode.java` |
| ErrorCategory | `domain/.../domain/common/ErrorCategory.java` |
