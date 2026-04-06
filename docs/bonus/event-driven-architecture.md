# 이벤트 기반 아키텍처

> Transactional Outbox 패턴 기반의 비동기 이벤트 처리 구조를 설명한다.
> Spring ApplicationEvent 대신 DB Outbox 테이블로 이벤트를 발행하여, 메시지 유실 없이 최종 일관성을 보장한다.

---

## 1. 설계 원칙

| 원칙 | 설명 |
|------|------|
| **Spring Event 금지** | 인메모리 이벤트는 장애 시 유실된다. Outbox 테이블로 원자성을 보장 |
| **정상 경로는 하나** | 이벤트 + 보정 스케줄러 2개가 아닌, Outbox 스케줄러 1개로 흐름을 단순화 |
| **실패 복구가 내장** | retryCount, maxRetries, deferRetry로 재시도가 도메인 모델에 내장 |
| **단계 간 결합 제거** | 수집(fetch) → 저장(persist) → 가공(process)이 Outbox로 비동기 분리 |

### 왜 Spring ApplicationEvent를 쓰지 않는가

```
Spring Event 방식의 문제:
  1. 메모리 기반 → 서버 재시작 시 유실
  2. @TransactionalEventListener → 트랜잭션 커밋 후 실행, 실패 시 재시도 불가
  3. 정상 경로(이벤트) + 보정 경로(스케줄러) 2개가 공존 → 디버깅 복잡

Outbox 방식:
  1. DB에 저장 → 유실 없음
  2. 트랜잭션 내에서 비즈니스 데이터 + Outbox를 원자적 저장
  3. 정상 경로 = 스케줄러 1개 → 흐름이 단순
```

---

## 2. 아키텍처 개요

### Transactional Outbox 패턴

```
[비즈니스 트랜잭션]
  ├── 도메인 데이터 저장 (Property, Rate, Inventory 등)
  └── Outbox 메시지 저장 (SupplierTask — 같은 트랜잭션)
         ↓
[스케줄러가 Outbox 폴링]
  → PENDING 조회 → PROCESSING → 실행 → COMPLETED / FAILED
         ↓
[후속 이벤트 체이닝]
  → 1단계 완료 시 2단계 Outbox 자동 생성 (FollowUpTaskCreator)
```

### 적용 영역

| 영역 | Outbox 역할 | 이벤트 소비자 |
|------|-----------|-------------|
| **외부 공급자 데이터 수집** | SupplierTask | SupplierTaskExecutorScheduler |
| **수집 데이터 가공** | SupplierRawData (FETCHED 상태) | SupplierRawDataProcessorScheduler |
| **Rate 캐시 갱신** | Rate 스냅샷 Write-Through | Outbox 스케줄러가 Redis SET |

---

## 3. 구현 상세

### 3.1 SupplierTask — Outbox 도메인 모델

```java
public class SupplierTask {
    private SupplierTaskId id;
    private SupplierId supplierId;
    private SupplierTaskType taskType;    // PROPERTY_CONTENT / RATE_AVAILABILITY
    private SupplierTaskStatus status;    // PENDING → PROCESSING → COMPLETED / FAILED
    private int retryCount;
    private int maxRetries;               // 기본 3회
    private String failureReason;         // 구조화된 실패 사유 (JSON)

    // 상태 전이 메서드
    void markProcessing();                // PENDING → PROCESSING
    void markCompleted(Instant now);      // PROCESSING → COMPLETED
    void markFailed(String reason, now);  // PROCESSING → FAILED (retryCount++)
    void deferRetry();                    // PROCESSING → PENDING (CB OPEN 시, retryCount 안 깎음)
    void resetToPending();               // FAILED → PENDING (재시도)
}
```

**상태 전이 테이블:**

```
PENDING ──→ PROCESSING ──→ COMPLETED
  ↑              │
  │              ├──→ FAILED ──→ PENDING (resetToPending, retryCount < maxRetries)
  │              │
  └──────────────┘ (deferRetry, CB OPEN — retryCount 미증가)
```

### 3.2 3단계 스케줄러 구조

```
[1단계] SupplierTaskTriggerScheduler (60초 주기)
  → 수집 주기 도래한 공급자 판별
  → SupplierTask(PENDING) 생성
  → 기존 진행 중 Task와 중복 제거 (SupplierTasks 일급 컬렉션)

[2단계] SupplierTaskExecutorScheduler (5초 주기)
  → PENDING Task 배치 조회 (50건)
  → markProcessing → 외부 API 호출 (Circuit Breaker + Retry 보호)
  → 성공: RawData 저장 + SyncLog + Task 완료 + 후속 Task 생성 (원자적 트랜잭션)
  → 실패: failureReason 기록 + retryCount 증가
  → CB OPEN: deferRetry (retryCount 미증가, PENDING 복귀)

[3단계] SupplierRawDataProcessorScheduler (10초 주기)
  → FETCHED RawData 배치 조회 (50건)
  → TaskType별 전략 패턴으로 가공
    - PROPERTY_CONTENT → 숙소/객실 파싱 → Diff 계산 → Property 생성/갱신
    - RATE_AVAILABILITY → 요금/재고 파싱 → Rate/Inventory 갱신 → Redis 캐시 SET
  → 성공: markSynced / 실패: markFailed
```

### 3.3 이벤트 체이닝 — FollowUpTaskCreator

```
PROPERTY_CONTENT Task 완료
  → FollowUpTaskCreator.createFollowUpIfNeeded()
    → RATE_AVAILABILITY Task 자동 생성 (같은 트랜잭션)
```

숙소 컨텐츠를 먼저 수집한 뒤 요금/재고를 수집하는 2단계 연쇄 패턴입니다.
Expedia Rapid API가 Content API → Shopping API 순서로 분리된 것과 동일한 구조입니다.

### 3.4 실패 복구 메커니즘

**SupplierTaskFailureReason — 구조화된 실패 사유:**

```java
public record SupplierTaskFailureReason(
    Integer httpStatus,     // 500, 429 등
    String errorCode,       // 공급자 에러 코드
    String errorMessage,    // 상세 메시지
    Instant occurredAt      // 실패 시각
) {
    String toJson();                    // 직렬화 (순수 Java, Jackson 의존 없음)
    static SupplierTaskFailureReason fromJson(String json); // 역직렬화
}
```

**예외 분류 체계 (Circuit Breaker):**

| 예외 | 분류 | CB 기록 | Retry | Task 처리 |
|------|------|:------:|:-----:|----------|
| SupplierServerException (5xx) | Retryable | O | O | markFailed (retryCount++) |
| SupplierNetworkException | Retryable | O | O | markFailed (retryCount++) |
| SupplierBadRequestException (4xx) | Non-Retryable | X | X | markFailed (retryCount++) |
| ExternalServiceUnavailableException (CB OPEN) | — | X | X | deferRetry (retryCount 유지) |

**CB OPEN 시 deferRetry의 의미:**
외부 서비스 자체가 불가한 상황에서 우리 재시도 횟수를 깎으면 불공정합니다.
PENDING으로 복귀하고 retryCount를 증가시키지 않아, 서비스가 복구되면 정상 재시도됩니다.
CrawlingHub 프로젝트에서 검증된 패턴입니다.

### 3.5 Rate 캐시 Write-Through — Outbox 기반 갱신

```
파트너 가격 변경:
  RateRule 수정 → Rate 스냅샷 재생성 → DB 저장 + Outbox 저장 (같은 트랜잭션)
    → 스케줄러: Outbox 소비 → Redis SET (새 가격으로 덮어쓰기)

고객 요금 조회:
  Redis MGET → 캐시 히트 (99.9%) → 바로 응답
  캐시 미스 (Redis 재시작 등 예외) → DB 조회 → Redis SET → 응답
```

**DEL이 아닌 SET:** 캐시를 지우는 게 아니라 새 값으로 덮어씁니다.
- Thundering Herd 자체가 발생하지 않음 (캐시가 만료되지 않으므로)
- 분산 락 불필요
- TTL 24시간은 안전장치 (정상 흐름에서는 SET으로 항상 갱신)

---

## 4. 설계 결정 근거

### 왜 Outbox인가 (vs 다른 방식)

| 방식 | 장점 | 단점 | 판단 |
|------|------|------|------|
| Spring ApplicationEvent | 구현 간단 | 유실 위험, 복구 불가 | **채택 안 함** |
| 외부 MQ (Kafka, SQS) | 높은 처리량 | 인프라 복잡도 증가, 과제 범위 초과 | **채택 안 함** |
| **Transactional Outbox + 스케줄러** | 유실 없음, 트랜잭션 보장, 인프라 단순 | 폴링 지연 (스케줄러 주기만큼) | **채택** |

### 왜 2단계 수집인가

실제 OTA Supplier API를 리서치한 결과:
- **Expedia Rapid API**: Content API(숙소/객실) + Shopping API(요금/재고)로 분리
- **Booking.com Connectivity API**: Content 관리 + Rates & Availability로 분리

컨텐츠(1일 1회)와 요금/재고(짧은 주기)의 수집 주기가 다르므로 분리가 필수입니다.

### 현재 구조와 한계 — 왜 이렇게 했는가

**현재 구조: 스케줄러가 Worker 역할을 겸함**

```
스케줄러(단일 인스턴스)
  → Outbox 폴링 → PROCESSING → 직접 실행 → COMPLETED/FAILED
```

**이렇게 한 이유:**
- 과제 범위 (7일)에서 SQS/Kafka 인프라를 구축하는 것은 비효율적
- Docker Compose 환경에서 MySQL + Redis만으로 동작해야 함
- 핵심은 "이벤트 기반 사고"이지 "인프라 복잡도"가 아님
- Outbox 도메인 모델(SupplierTask)이 잘 설계되면, 인프라 교체 시 Application 레이어 변경 없음

**현재 구조의 한계:**
- 단일 인스턴스에서만 동작 → 수평 확장 불가
- 스케줄러 주기(5초)만큼 지연 → 실시간성 부족
- 외부 API 호출이 스케줄러 스레드를 점유 → 처리량 제한

---

### 프로덕션 확장 — 어떻게 바꿀 수 있는가

**Phase 1: SQS 기반 Worker 분리**

```
현재:
  스케줄러 → Outbox 폴링 → 직접 실행

변경:
  [스케줄러] → Outbox 폴링 → SQS 발행 (Idempotency Key로 중복 방지)
  [Worker N대] → SQS 소비 → 실행 → 결과 DB 반영
```

변경 범위:
- `SupplierTaskExecutorScheduler` → SQS Publisher로 교체
- `SupplierTaskWorker` 신규 (SQS Consumer, 별도 모듈)
- **Application 레이어(UseCase, Service, Domain) 변경 없음**

이것이 헥사고날의 장점입니다. Adapter-In만 교체하면 됩니다.

**Phase 2: 수평 확장 + 고가용성**

```
[스케줄러] (1대, Leader Election)
  → Outbox 폴링 → SQS 발행

[Worker] (N대, Auto Scaling)
  → SQS 소비 → 외부 API 호출 → 결과 저장
  → 처리량에 따라 수평 확장
  → CB OPEN 시 DLQ로 이동 → 별도 복구 프로세스

[SQS]
  → Visibility Timeout: Worker가 처리 중 다른 Worker가 못 가져감
  → Dead Letter Queue: 최대 재시도 초과 메시지 격리
  → Idempotency Key: SupplierTask.id 기반 중복 소비 방지
```

**Phase 3: 이벤트 소싱 (필요 시)**

```
SupplierTask 상태 변경을 이벤트 로그로 관리:
  TaskCreated → TaskProcessing → TaskCompleted/TaskFailed → TaskRetried

장점:
  - 전체 상태 변경 이력 추적
  - 특정 시점으로 상태 복원 가능
  - 감사(Audit) 요구사항 충족
```

---

### 확장 시에도 변경되지 않는 것

| 레이어 | 변경 여부 | 이유 |
|--------|:--------:|------|
| Domain (SupplierTask, Status, FailureReason) | **불변** | 비즈니스 규칙은 인프라에 무관 |
| Application (UseCase, Service, Manager) | **불변** | Port 인터페이스 기반, Adapter에 무관 |
| Adapter-In (스케줄러) | **교체** | SQS Publisher로 변경 |
| Adapter-Out (Persistence) | **불변** | DB 저장 로직 동일 |
| 신규: Worker 모듈 | **추가** | SQS Consumer + UseCase 호출 |

이것이 **"과제에서는 스케줄러로 간단히 구현하되, 설계는 확장을 고려했다"**는 의미입니다.

---

## 5. 코드 위치

| 구성 요소 | 위치 |
|----------|------|
| SupplierTask 도메인 | `domain/supplier/SupplierTask.java` |
| SupplierTaskStatus (전이 테이블) | `domain/supplier/SupplierTaskStatus.java` |
| SupplierTaskType (PROPERTY_CONTENT, RATE_AVAILABILITY) | `domain/supplier/SupplierTaskType.java` |
| SupplierTaskFailureReason (실패 사유 VO) | `domain/supplier/SupplierTaskFailureReason.java` |
| CreateSupplierTaskService (1단계) | `application/supplier/service/CreateSupplierTaskService.java` |
| ExecuteSupplierTaskService (2단계) | `application/supplier/service/ExecuteSupplierTaskService.java` |
| ProcessSupplierRawDataService (3단계) | `application/supplier/service/ProcessSupplierRawDataService.java` |
| SupplierFollowUpTaskCreator (이벤트 체이닝) | `application/supplier/SupplierFollowUpTaskCreator.java` |
| SupplierFetchPersistenceFacade (원자적 저장) | `application/supplier/facade/SupplierFetchPersistenceFacade.java` |
| 스케줄러 3개 (Adapter-In) | `adapter-in/scheduler/supplier/` |
| Circuit Breaker + Retry | `adapter-out/client/supplier-client/` |
| ExternalServiceUnavailableException | `application/common/exception/` |
| Rate 캐시 Write-Through | `application/pricing/manager/RateCacheManager.java` |
