# 테스트 코드

> 단위 테스트, 통합 테스트, 동시성 테스트 등 테스트 전략과 현황을 정리한 문서입니다.

---

## 1. 테스트 전략 요약

### H2 대신 Testcontainers를 선택한 이유

과제 요구사항 중 "동일한 재고에 대해 동시 예약 요청이 발생할 수 있는 상황을 처리해야 한다"가 있습니다. H2는 `SELECT FOR UPDATE`, 원자적 `UPDATE ... WHERE available_count >= 1` 같은 동시성 제어 구문의 동작이 MySQL과 다릅니다. 동시성 검증을 정확히 하려면 실제 MySQL/Redis에서 테스트해야 한다고 판단했습니다.

| 항목 | H2 | Testcontainers MySQL |
|------|-----|---------------------|
| 동시성 제어 검증 | 동작이 다름 | 운영 환경과 동일 |
| Flyway 호환성 | MySQL 문법 불일치 가능 | 동일한 마이그레이션 사용 |
| 테스트 신뢰도 | 낮음 | 높음 |
| 속도 | 빠름 | Singleton 컨테이너로 최적화 |

### 테스트 피라미드

```
              ┌───────────────┐
              │  E2E 통합     │  Testcontainers (MySQL + Redis) 전체 흐름
              ├───────────────┤
              │  REST API     │  MockMvc + REST Docs
              ├───────────────┤
              │  Application  │  Mockito Mock 기반 단위 테스트
              ├───────────────┤
              │  Persistence  │  Testcontainers 통합 테스트
              ├───────────────┤
              │    Domain     │  순수 Java 단위 테스트
              └───────────────┘
```

---

## 2. 테스트 현황

| 레이어 | 테스트 유형 | 테스트 수 | 환경 |
|--------|-----------|:--------:|------|
| Domain | 단위 테스트 | 1,019 | 순수 Java (외부 의존 없음) |
| Application | 단위 테스트 | - | Mockito Mock 기반 |
| Persistence MySQL | 통합 테스트 | 190 | Testcontainers MySQL 8.0 + Flyway |
| Persistence Redis | 통합 테스트 | 23 | Testcontainers Redis 7.2 |
| REST API | 슬라이스 테스트 | 202 | MockMvc + REST Docs |
| E2E 통합 | 전체 흐름 | 11 | Testcontainers (MySQL + Redis) |
| 성능 테스트 | 부하/동시성 | 7 | Testcontainers + Virtual Thread |
| ArchUnit | 아키텍처 | 14+ | 레이어 경계/컨벤션 강제 |

---

## 3. 핵심 테스트 시나리오

### 동시성 테스트 — 과제 필수 요구사항

"동일한 재고에 대해 동시 예약 요청이 발생할 수 있는 상황"을 Redis Lua 스크립트 + DB 원자적 UPDATE 2중 구조로 해결했고, 이를 실제 Testcontainers 환경에서 검증합니다.

| 시나리오 | 조건 | 기대 결과 |
|---------|------|----------|
| 재고 1개 + 10 동시 요청 | ExecutorService 10 스레드 | 정확히 1건 성공, 9건 실패, 최종 재고 0 |
| 재고 5개 + 10 동시 요청 | ExecutorService 10 스레드 | 정확히 5건 성공, 5건 실패, 최종 재고 0 |
| 차감 + 복구 혼합 | 5번 차감 + 3번 복구 동시 | 최종 재고 정합성 (10 - 5 + 3 = 8) |

### 멱등키 테스트 — 이중 예약 방지

| 시나리오 | 기대 결과 |
|---------|----------|
| 동일 멱등키로 세션 생성 재요청 | 기존 세션 반환 (새로 생성하지 않음) |
| 동일 멱등키 + DB unique 제약 | 중복 저장 차단 |

### 2단계 예약 프로세스 테스트

| 시나리오 | 기대 결과 |
|---------|----------|
| 세션 생성 → 확정 | PENDING → CONFIRMED, 재고 차감 |
| 세션 만료 후 확정 시도 | 예외 발생, 재고 복구 |
| 확정 실패 시 보상 | Redis INCRBY로 재고 자동 복구 |
| 이미 확정된 세션 재확정 | 기존 reservationId 반환 (멱등) |

### Redis 폴백 테스트

| 시나리오 | 기대 결과 |
|---------|----------|
| Redis 정상 | Redis Lua로 재고 차감 |
| Redis 장애 | DB WHERE available_count >= 1로 폴백 |

---

## 4. 테스트 설계 원칙

| 원칙 | 설명 |
|------|------|
| **실제 인프라 검증** | H2 대신 Testcontainers MySQL/Redis로 운영 환경과 동일한 동작 보장 |
| **Singleton 컨테이너** | JVM당 1회 기동으로 테스트 속도 최적화 |
| **테스트 격리** | `@Transactional` 자동 롤백(MySQL), `@BeforeEach` 키 정리(Redis) |
| **Fixture 패턴** | `testFixtures` 소스셋으로 도메인 객체 생성 헬퍼를 모듈 간 공유 |
| **레이어별 독립 검증** | Domain은 순수 로직, Application은 흐름, Persistence는 DB 연동만 각각 검증 |

---

## 5. 도메인 패턴별 검증

테스트에서 검증하는 프로젝트 고유 패턴들입니다.

| 패턴 | 검증 내용 |
|------|----------|
| **diff 패턴** | 편의시설/속성값의 기존 조회 → added 저장 + removed soft delete |
| **번들 패턴** | RoomType + Bed/View를 forPending → withRoomTypeId → 일괄 저장 |
| **원자적 저장** | Reservation + Line + Item 연쇄 persist |
| **Lua 원자적 차감** | DECRBY → 음수 체크 → 전체 INCRBY 롤백 |
| **좀비 세션** | findPendingBefore(cutoff)로 만료 세션 조회 + 재고 복구 |
| **크로스 BC 검색** | 5개 BC JOIN + 커서 페이지네이션 |

---

## 6. 성능 테스트

Testcontainers + Java 21 Virtual Thread 기반으로 극한 동시성 부하를 검증합니다.

| 시나리오 | 조건 | 결과 |
|---------|------|------|
| 캐시 콜드→웜 | 100건 조회 2회 | 372 RPS → 509 RPS (37% 향상) |
| 동시 500건 요금 조회 | 캐시 웜 상태 | 469 RPS, 전부 200 OK |
| 캐시 스탬피드 | 캐시 삭제 직후 300건 동시 | 488 RPS, 에러 0건 |
| 재고 100개 / 200건 | 동시 세션 생성 | 성공 100, 실패 100 정확 (837ms) |
| 재고 10개 / 500건 | 극한 동시성 | 성공 10, 실패 490 정확 (559ms) |
| 재고 5개 / 1000건 | 초극한 | 성공 5, 실패 995 정확 |
| 혼합 부하 | 요금 300건 + 예약 100건 동시 | 전부 정상 |
| 30초 지속 부하 | 초당 20건 연속 | 캐시 히트율 99.4% 안정 |
| 가격 변경 + 캐시 무효화 | 100,000→150,000 | 즉시 반영, 불일치 0건 |

상세 결과: [performance-test-report.md](performance-test-report.md)

---

## 7. 테스트 실행 방법

```bash
# 전체 테스트 (Docker 실행 필요)
./gradlew test

# 레이어별 실행
./gradlew :domain:test                            # 순수 Java, Docker 불필요
./gradlew :application:test                        # Mockito, Docker 불필요
./gradlew :adapter-out:persistence-mysql:test       # Testcontainers MySQL
./gradlew :adapter-out:persistence-redis:test       # Testcontainers Redis
./gradlew :adapter-in:rest-api-extranet:test        # MockMvc + REST Docs
./gradlew :adapter-in:rest-api-customer:test        # MockMvc + REST Docs

# E2E 통합 테스트
./gradlew :bootstrap:bootstrap-extranet:test --tests "*E2ETest*"
./gradlew :bootstrap:bootstrap-customer:test --tests "*E2ETest*"

# 성능 테스트
./gradlew :bootstrap:bootstrap-customer:test --tests "*PerformanceTest*"
./gradlew :bootstrap:bootstrap-customer:test --tests "*InvalidationTest*"

# 극한 부하 스크립트 (서버 기동 상태에서)
bash infra/local-dev/load-test.sh
```

> Testcontainers 기반 테스트(Persistence, Redis, E2E, 성능)는 Docker가 실행 중이어야 합니다.

---

## 8. 상세 문서

| 문서 | 내용 |
|------|------|
| [테스트 전략](../test/test-strategy.md) | 테스트 피라미드, E2E 시나리오, 에러 핸들링 |
| [Domain 테스트](../test/domain-layer.md) | 도메인 모델 비즈니스 규칙 검증 상세 |
| [Application 테스트](../test/application-layer.md) | UseCase + Factory + Validator 검증 상세 |
| [Persistence 테스트](../test/persistence-layer.md) | MySQL 190개 + Redis 23개 시나리오 상세 |
| [REST API 테스트](../test/rest-api-layer.md) | Controller + Mapper + ErrorMapper 검증 상세 |
| [E2E 통합 테스트](../test/e2e-integration-layer.md) | 전체 흐름 + 동시성 검증 상세 |
| [성능 테스트 결과](performance-test-report.md) | 캐싱/동시성/스탬피드 + Grafana 모니터링 |
| [E2E 시나리오](../test-scenarios/) | Extranet + Customer 시나리오 문서 |
