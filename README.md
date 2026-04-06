# OTA 숙박 플랫폼

가상의 OTA(Online Travel Agency) 숙박 플랫폼 백엔드 시스템입니다.
파트너가 숙소를 등록하고, 고객이 검색·예약하고, 외부 Supplier 상품이 통합되는 구조입니다.

---

## 실행 방법

```bash
# 1. Docker Compose로 MySQL + Redis 실행
docker compose up -d

# 2. 서버 실행
./gradlew :bootstrap:bootstrap-extranet:bootRun    # 파트너 API (8080)
./gradlew :bootstrap:bootstrap-customer:bootRun    # 고객 API (8081)
./gradlew :bootstrap:bootstrap-scheduler:bootRun   # 스케줄러 (Supplier 동기화, 좀비 세션 복구)

# 3. Swagger UI 확인
# http://localhost:8080/swagger-ui.html (Extranet)
# http://localhost:8081/swagger-ui.html (Customer)

# 4. 테스트 실행 (Docker 필요)
./gradlew test
```

---

## 기술 스택

| 항목 | 선택 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.6 |
| Build | Gradle 8.14.3 (Kotlin DSL), 멀티모듈 |
| Database | MySQL 8.4.0 (Testcontainers 기반 통합 테스트) |
| ORM | Spring Data JPA + QueryDSL 5.1.0 |
| Migration | Flyway 10.10.0 |
| Cache / Lock | Redis (Redisson 3.27.2) |
| Test | JUnit 5 + AssertJ + Mockito + Testcontainers 2.0.3 |
| Architecture Test | ArchUnit 1.2.1 |
| API 문서 | SpringDoc OpenAPI 2.8.6 + Spring REST Docs |

---

## 아키텍처

Hexagonal Architecture (Port & Adapter) 기반 멀티모듈 구조입니다.

```
                    ┌──────────────────────────────────────────────────┐
                    │                   Adapter-In                      │
                    │  ┌──────────┐ ┌──────────┐ ┌───────────────────┐  │
  HTTP 요청 ───────▶│  │ Extranet │ │ Customer │ │    Scheduler      │  │
                    │  │  :8080   │ │  :8081   │ │ Supplier 동기화   │  │
                    │  │          │ │          │ │ 좀비 세션 복구    │  │
                    │  └────┬─────┘ └────┬─────┘ └────────┬──────────┘  │
                    └───────┼────────────┼────────────────┼────────────┘
                            │            │                │
                    ┌───────▼────────────▼────────────────▼───┐
                    │             Application                  │
                    │  UseCase ─ Service ─ Manager ─ Factory   │
                    │  Port(in/out) ─ Validator ─ Assembler    │
                    └───────────────────┬─────────────────────┘
                                        │
                    ┌───────────────────▼─────────────────────┐
                    │               Domain                     │
                    │  순수 Java — Spring/JPA 의존 없음         │
                    │  Aggregate ─ VO ─ Enum ─ ErrorCode       │
                    └───────────────────┬─────────────────────┘
                                        │
                    ┌───────────────────▼─────────────────────┐
                    │             Adapter-Out                   │
                    │  ┌──────────────┐  ┌──────────────────┐  │
                    │  │ MySQL (JPA)  │  │ Redis (캐시/재고) │  │
                    │  │ Flyway 16개  │  │ Lua Script 3개   │  │
                    │  └──────────────┘  └──────────────────┘  │
                    └─────────────────────────────────────────┘
```

### 설계 원칙
- **Domain은 순수 Java** — Spring, JPA, jakarta 등 외부 프레임워크 import 금지 (ArchUnit으로 강제)
- **BC 간 ID 참조** — 객체를 직접 참조하지 않고 ID VO(PropertyId, RoomTypeId 등)로만 참조
- **책임 분리** — 정책 판단(Domain) / 흐름 조립(Application) / 외부 통신(Adapter)

---

## 구현 범위

### 핵심 흐름

```
파트너가 숙소 등록 → 객실/요금/재고 설정 → 고객이 검색 → 요금 조회 → 예약 → 취소
                                          ↑
                        외부 Supplier 상품이 통합되어 함께 검색
```

### 필수 요구사항 (6/6 충족)

| # | 요구사항 | 구현 |
|---|---------|------|
| 1 | 숙소 파트너가 숙소 정보를 등록/관리 | Extranet 9개 엔드포인트 |
| 2 | 고객이 숙소를 검색하고 요금 조회 | Customer 검색 + 요금 조회 API |
| 3 | 대규모 요금 조회 동시 처리 | Redis 3단 캐싱 (RateRule → Rate → Redis) |
| 4 | 고객이 숙소를 예약하고 취소 | 2단계 예약 (세션 → 확정) + 취소 API |
| 5 | 동시 예약 동시성 제어 | Redis Lua + DB 원자적 UPDATE 2중 구조 |
| 6 | 외부 Supplier 상품 통합 | ACL + 2단계 동기화 (수집 → 가공) |

### API 엔드포인트

**Extranet (파트너용) — localhost:8080**

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/v1/extranet/properties` | 숙소 등록 |
| GET | `/api/v1/extranet/properties` | 숙소 목록 조회 |
| GET | `/api/v1/extranet/properties/{id}` | 숙소 상세 조회 |
| PUT | `/api/v1/extranet/properties/{id}/photos` | 사진 설정 |
| PUT | `/api/v1/extranet/properties/{id}/amenities` | 편의시설 설정 |
| PUT | `/api/v1/extranet/properties/{id}/attributes` | 속성값 설정 |
| POST | `/api/v1/extranet/properties/{id}/rooms` | 객실 등록 |
| POST | `/api/v1/extranet/rooms/{id}/rate-plans` | 요금 정책 등록 |
| PUT | `/api/v1/extranet/rate-plans/{id}/rates` | 요금/재고 설정 |

**Customer (고객용) — localhost:8081**

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/v1/search/properties` | 숙소 검색 |
| GET | `/api/v1/properties/{id}/rates` | 요금 조회 |
| POST | `/api/v1/reservation-sessions` | 예약 세션 생성 (재고 선점) |
| POST | `/api/v1/reservations` | 예약 확정 |
| PATCH | `/api/v1/reservations/{id}/cancel` | 예약 취소 |

**Scheduler (백그라운드)**

| 스케줄러 | 주기 | 역할 |
|---------|------|------|
| SupplierTaskTriggerScheduler | 주기적 | Supplier API 설정 기반 수집 작업 생성 |
| SupplierTaskExecutorScheduler | 주기적 | 수집 작업 실행 (외부 API 호출 → Raw 저장) |
| SupplierRawDataProcessorScheduler | 주기적 | Raw 데이터 → ACL 변환 → Property 반영 |
| ReservationSessionRecoveryScheduler | 주기적 | 만료된 예약 세션 감지 → 재고 복구 (좀비 세션 방지) |

**Admin (관리자용) — 미구현**

Admin API는 설계만 완료했으며, 실제 구현은 진행하지 않았습니다. 핵심 흐름(Extranet → Customer → 예약 → Supplier)에 집중하는 것이 7일 내 더 효과적이라고 판단했습니다.

---

## 권장 가산점 항목

| # | 항목 | 문서 |
|---|------|------|
| 1 | 설계/조사 관련 문서 (도메인 리서치, 아키텍처, ERD) | [design-and-research.md](docs/bonus/design-and-research.md) / [ERD](docs/bonus/erDiagram.md) |
| 2 | 테스트 코드 (단위/통합/동시성) | [test-code.md](docs/bonus/test-code.md) |
| 3 | 과정 기록서 (Progress Journal) | [progress-journal.md](docs/progress-journal.md) |
| 4 | AI 활용 기록 | [ai-usage-log.md](docs/bonus/ai-usage-log.md) |
| 5 | API 문서 자동화 (Swagger + REST Docs) | [docs/api/README.md](docs/api/README.md) |
| 6 | 에러 처리 및 로깅 전략 | [error-and-logging-strategy.md](docs/bonus/error-and-logging-strategy.md) |
| 7 | 이벤트 기반 아키텍처 (Outbox 패턴) | [event-driven-architecture.md](docs/bonus/event-driven-architecture.md) |
| 8 | 외부 클라이언트 장애 대응 (CB, Retry, 유량 제어) | [external-client-resilience.md](docs/bonus/external-client-resilience.md) |
| 9 | 성능 테스트 결과 | [performance-test-report.md](docs/bonus/performance-test-report.md) |
| 10 | 모니터링 설계 (Micrometer + Prometheus + Grafana) | [performance-test-report.md §7~9](docs/bonus/performance-test-report.md) |

---

## 문서 구조

```
docs/
├── bonus/                                — 가산점 항목 문서
│   ├── README.md                         — 가산점 인덱스
│   ├── design-and-research.md            — 설계/조사 (OTA 리서치 → 설계 반영)
│   ├── test-code.md                      — 테스트 전략/현황
│   ├── ai-usage-log.md                   — AI 활용 기록 (멀티 에이전트 시스템)
│   ├── error-and-logging-strategy.md     — 에러 처리 전략
│   ├── event-driven-architecture.md      — 이벤트 기반 아키텍처 (Outbox)
│   ├── external-client-resilience.md     — 외부 클라이언트 장애 대응 (CB, Retry)
│   ├── performance-test-report.md        — 성능 테스트 결과
│   └── erDiagram.md                      — ERD (Mermaid)
├── api/                                  — API 문서 (Swagger + REST Docs)
├── research/                             — OTA 리서치 원본 5개
├── design/                               — 설계 결정 / 컨벤션 / ADR
├── test/                                 — 테스트 시나리오 상세
└── progress-journal.md                   — 과정 기록서
```

---

## 테스트

```bash
./gradlew test                                      # 전체 테스트
./gradlew :domain:test                              # 도메인 단위 테스트 (Docker 불필요)
./gradlew :application:test                         # Application 단위 테스트
./gradlew :adapter-out:persistence-mysql:test        # MySQL 통합 테스트
./gradlew :adapter-out:persistence-redis:test        # Redis 통합 테스트
./gradlew :adapter-in:rest-api-extranet:test         # Extranet API 테스트
./gradlew :adapter-in:rest-api-customer:test         # Customer API 테스트
```

> Testcontainers 기반 테스트는 Docker가 실행 중이어야 합니다.
