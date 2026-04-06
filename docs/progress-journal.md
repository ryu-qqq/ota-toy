# 과정 기록서 (Progress Journal)

---

## Day 1 — 2026-04-01 [도메인 리서치 & 프로젝트 설계]

### 수행 내용
- 요구사항 분석 및 구조화 (필수 6개 / 선택 가산점 / 기록 요건 분류)
- 도메인 초안 작성 — 6개 Bounded Context 식별 (숙소, 재고/요금, 예약, 파트너, Supplier, 검색)
- 실제 OTA 플랫폼 3개를 Playwright로 크롤링하여 도메인 모델 도출
- 크로스 플랫폼 비교 분석 및 도메인 초안 검증
- 프로젝트 초기 세팅 (Spring Boot 3.4+, Gradle Kotlin DSL, 멀티모듈)

### 의사결정

**1. 도메인 리서치 방법론 — 실제 크롤링 기반**
- 선택: 실제 OTA 플랫폼 크롤링 + API 분석
- 대안: (A) 상상 기반 설계 (B) 기술 블로그 2차 자료 기반
- 이유: OTA 도메인을 처음 다루므로, 1차 자료에서 도메인을 도출해야 설계 근거가 실증적
- 결과: 초안에서 놓친 핵심 개념 발견 (성급, 결제방식, 가격 3단 구조, Supplier 법적 정보 등)

**2. 크롤링 대상 — 국내/해외 혼합 3개**
- 선택: 해외 글로벌 OTA + 해외 아시아 OTA + 국내 OTA
- 이유: Supplier 통합 구조를 이해하려면 해외 OTA가 공급자 역할을 하는 맥락을 파악해야 했습니다. 국내/해외 차이(대실, 회원가, 세금 표시)가 도메인 설계에 직접 영향을 줍니다

**3. 기술 스택 — MySQL + Redis + Testcontainers**
- 선택: H2 대신 MySQL, Testcontainers로 테스트
- 이유: 재고 차감에 원자적 UPDATE를 사용하는데, H2에서는 실제 MySQL과 동작이 달라 동시성 테스트 결과를 신뢰할 수 없습니다

**4. 아키텍처 — Hexagonal + 멀티모듈**
- 선택: 헥사고날 아키텍처 (Port & Adapter)
- 대안: 레이어드 아키텍처 (단순하지만 도메인이 인프라에 의존)
- 이유: Supplier 통합 시 Anti-Corruption Layer가 필수이며, 헥사고날의 Port/Adapter 패턴이 이 구조에 자연스럽습니다

### 기술적 문제 해결
- **문제**: OTA 사이트 크롤링 시 Playwright click이 오버레이에 가로막힘
- **해결**: 클릭 대신 URL 직접 접속 방식으로 우회
- **배운 점**: Playwright MCP로 크롤링 시 snapshot(접근성 트리)이 screenshot보다 데이터 분석에 훨씬 유용합니다

---

## Day 2 — 2026-04-02 [도메인 모델 설계 & 핵심 설계 결정]

### 수행 내용
- 요금 캐싱 설계 (RateRule → Rate → Redis 3단 레이어)
- 재고 동시성 설계 (Redis 원자적 카운터 + 임시 홀드)
- ERD 작성 및 도메인 모델 상세화
- 멀티 에이전트 시스템 초기 설계

### 의사결정

**5. 요금 구조 — RateRule + RateOverride + Rate 3단**
- 선택: 규칙(RateRule) → 특정일 덮어쓰기(RateOverride) → 계산된 스냅샷(Rate)
- 이유: 파트너는 "기간 + 요일별 가격"이라는 규칙을 설정하고, 고객은 계산된 날짜별 가격을 조회합니다. 실제 국내 OTA의 캘린더 API에서 요일별 가격 변동(금토: 23만, 평일: 8만)을 확인하여 이 설계의 근거로 삼았습니다

**6. 요금 캐싱 — Write-Through + Outbox**
- 선택: 파트너가 가격 변경 시 캐시를 지우지 않고 새 값으로 덮어쓰기(SET)
- 대안: Cache-Aside (읽을 때 캐시 미스면 DB 조회) → Thundering Herd 문제 발생
- 이유: Rate는 쓰기 빈도 낮고(파트너 가격 변경 시만) 읽기 빈도 높은(고객 조회마다) 데이터입니다

**7. 재고 동시성 — Redis 원자적 카운터**
- 선택: Redis DECR로 원자적 차감
- 대안: DB 비관적 락(행 잠금, 경합), DB 낙관적 락(재시도 폭발)
- 이유: 초당 10만+ 처리 가능하며, Lua 스크립트로 다중 날짜를 원자적으로 차감/롤백할 수 있습니다

---

## Day 3 — 2026-04-03 [도메인 코드 생성 & 시간 컨벤션]

### 수행 내용
- 도메인 모델 코드 생성 (Accommodation, Pricing, Location, Partner, Supplier BC)
- 시간 컨벤션 정의 (도메인에서 Instant.now() 직접 호출 금지)
- 멀티 에이전트 시스템 구축 시작

### 의사결정

**8. 시간 처리 — 외부 주입 원칙**
- 선택: 도메인에서 Instant.now() 직접 호출 금지, forNew() 파라미터로 외부에서 주입
- 이유: 시간을 직접 생성하면 테스트에서 시간을 제어할 수 없습니다. ArchUnit으로 이 규칙을 강제했습니다

---

## Day 4 — 2026-04-04 [도메인 하네스 & 구조 정비]

### 수행 내용
- 멀티 에이전트 하네스 시스템 완성 (빌드 → 리뷰 → FIX → 테스트 자동 순환)
- domain-harness로 Accommodation BC 전체 실행 (432개 테스트 통과)
- record → class 전환 결정 및 실행 (13개 하위 엔티티)
- ArchUnit 규칙 12개로 보강 (Instant.now() 금지, Enum displayName() 필수, jakarta.validation 금지)
- BC 세분화: accommodation → property, roomtype, brand, propertytype, roomattribute 5개로 분리
- ERD에 Outbox 테이블 추가, Money를 BigDecimal로 통일

### 의사결정

**9. Outbox를 BC별 전용 테이블로 분리**
- 선택: ReservationOutbox, SupplierOutbox 각각 별도 테이블
- 대안: 공통 Outbox 1개 → 이벤트 유형별 파티셔닝 불가
- 이유: 예약 이벤트와 공급자 이벤트는 소비 주기와 재처리 정책이 다릅니다

**10. record → class 전환**
- 선택: Long id를 가진 record 13개를 전부 class로 전환
- 이유: record의 compact constructor가 모든 생성 경로에서 실행되어, reconstitute()에서도 검증이 돌아갑니다. DB에 저장된 과거 데이터가 규칙 변경 시 복원 자체가 실패하는 치명적 문제입니다
- 발견 경위: **하네스(자동화)가 잡지 못한 구조적 문제를 직접 리뷰에서 발견**. 자동화는 유용하지만 만능이 아니며, 사람의 설계 리뷰가 여전히 필수라는 교훈을 얻었습니다

**11. Manager 레이어 유지, Service @Transactional 금지**
- 선택: Manager 레이어에서만 트랜잭션 관리, Service(UseCase)에는 @Transactional 금지
- 이유: Service에 @Transactional을 걸면 전체가 하나의 긴 트랜잭션이 되어 DB 커넥션 점유가 길어집니다

**12. BrandId nullable 유지 (컨벤션 이의 판정)**
- 배경: 코드 리뷰어가 "참조 ID는 null 불허" 컨벤션으로 BLOCKER 지적
- 판정: OTA 리서치 데이터(모텔 769개, 펜션 21개 등 대다수 브랜드 없음)를 근거로 nullable 유지
- 교훈: **도메인 현실이 컨벤션보다 우선**합니다

### 기술적 문제 해결
- **ERD Outbox 누락**: 설계에 명시했으나 ERD에 반영하지 않은 상태를 PL 정합성 점검에서 발견 → BC별 전용 Outbox 추가
- **Money 타입 불일치**: int → BigDecimal 통일 (Supplier 다통화 지원 필요)
- **record reconstitute() 검증 문제**: class 전환으로 해결. forNew()에서만 검증, reconstitute()에서는 검증 없이 복원

---

## Day 5 — 2026-04-05 [Phase 1~2 구현 (기반 + 숙소 등록 E2E)]

### 수행 내용
- Phase 1 기반 구축: Docker Compose (MySQL + Redis), Flyway 마이그레이션, Persistence 공통 (BaseAuditEntity, SoftDeletableEntity)
- Phase 2 숙소 등록 E2E 완성: Domain → Application → Persistence → REST API **전 레이어를 처음으로 관통**
- Application 컨벤션 정비 (UseCase, Port, Manager, Factory, Assembler 패턴 정의)
- Extranet 숙소 등록 API 9개 엔드포인트 구현
- 숙소 사진/편의시설/속성값 설정 API에 **diff 패턴** 적용
- 객실 등록 API에 **번들 패턴** 적용 (RoomType + Bed + View 원자적 저장)

### 의사결정

**13. Application 복합조회 패턴 — 전용 Result + CriteriaFactory + Assembler**
- 선택: 사용자 유형별 UseCase 분리(Customer/Extranet), 전용 Result 래핑, Factory로 변환, Assembler로 조립
- 이유: 제네릭 SliceResult<T>는 필드 추가 시 래퍼를 깰 수 없습니다. 전용 Result가 확장성과 테스트 용이성에서 우수합니다

**14. 편의시설/사진 설정 — diff 패턴 (등록+수정을 하나의 UseCase로)**
- 선택: 클라이언트가 "현재 상태 전체"를 보내면, 서버가 기존 데이터와 비교하여 added/removed/retained를 자동 계산
- 대안: 개별 추가/삭제 API를 각각 제공 → 클라이언트가 상태를 관리해야 하는 부담
- 이유: OTA Extranet 리서치에서 편의시설은 "토글 on/off" 방식으로 관리됩니다. 전체 상태를 보내는 것이 이 UX와 일치합니다. 서버에서 diff를 계산하면 클라이언트는 현재 선택 상태만 보내면 됩니다

**15. 객실 등록 — 번들 패턴 (RoomType + Bed + View 원자적 저장)**
- 선택: 객실 등록 시 침대 구성/전망을 함께 받아 원자적으로 저장. Bed/View는 `forPending()` 상태로 생성 후 RoomType ID가 채번되면 `withRoomTypeId()`로 연결
- 이유: 객실 없이 침대 구성만 존재하는 것은 비즈니스적으로 의미가 없습니다. 하나의 트랜잭션에서 함께 저장되어야 합니다

### 기술적 문제 해결
- **Flyway + JPA validate 모드**: DDL은 Flyway가 관리하고, JPA는 validate만 수행하도록 설정했습니다. ddl-auto=validate로 스키마 불일치를 부팅 시 즉시 감지합니다
- **Singleton 컨테이너**: Testcontainers를 JVM당 1회만 기동하여 테스트 속도를 최적화했습니다. `MySqlTestContainerConfig`에서 static 블록으로 컨테이너를 한 번만 시작합니다
- **ErrorMapper 추상화**: 도메인 ErrorCode → HTTP 상태코드 매핑을 모듈별로 분리했습니다. ErrorCategory(NOT_FOUND/VALIDATION/CONFLICT/FORBIDDEN)를 ErrorCode에 추가하여, ErrorMapper가 카테고리 기반으로 HTTP 상태를 결정합니다. 모듈마다 ErrorMapper 구현체를 두고 ErrorMapperRegistry가 자동 수집합니다

---

## Day 6 — 2026-04-06 [Phase 3~7 구현 (검색 → 예약 → Supplier)]

### 수행 내용

**Phase 3: 객실 + 요금/재고 설정**
- RatePlan 등록 API + 요금 규칙(RateRule) 설정
- 요금/재고 설정 시 Rate 스냅샷 자동 계산 + Redis 캐시/재고 카운터 초기화

**Phase 4: 고객 검색 + 요금 조회**
- Customer 숙소 검색 API (5개 BC JOIN + 커서 페이지네이션)
- 요금 조회 API (Redis 3단 캐싱: MGET → DB 폴백 → MSET)

**Phase 5: 예약 (핵심 동시성)**
- 예약 세션 생성 (1단계: 멱등키 + Redis 재고 선점)
- 예약 확정 (2단계: Redis Lua + DB 원자적 UPDATE 2중 구조)
- 예약 취소 (재고 복구 + 상태 전이)
- 좀비 세션 복구 스케줄러 (만료 세션 감지 → 재고 자동 복구)

**Phase 6: Supplier 통합**
- Supplier ACL + 2단계 동기화 (수집: 외부 API → Raw 저장, 가공: ACL 변환 → Diff → Property 반영)
- 스케줄러 3개 (TaskTrigger → TaskExecutor → RawDataProcessor)

**Phase 7: Outbox + 비동기 처리**
- ReservationOutbox, SupplierOutbox, SupplierTask 테이블 구현
- Outbox 기반 이벤트 발행 (Spring Event 미사용)

**테스트**
- Persistence 통합 테스트 (Testcontainers MySQL 190개 + Redis 23개)
- 동시성 테스트 (재고 1개 + 10 동시 요청 → 정확히 1건 성공 검증)
- REST Docs 기반 API 문서 생성

### 의사결정

**16. 동시성 제어 — Redis Lua + DB 원자적 UPDATE 2중 구조 (ADR-001)**
- 선택: Redis가 1차 게이트키퍼, DB가 최종 정합성 보장
- 대안: (A) DB 비관적 락 — 데드락 위험, 10박 예약 시 10행 잠금 필요 (B) DB 낙관적 락 — 동시 요청 많으면 재시도 폭발 (C) Redis만 — SPOF, Redis-DB 정합성 관리 필요 (D) DB 원자적 UPDATE만 — 중규모까지 충분하지만 동시 부하 시 병목
- 이유: Redis가 대부분의 동시 요청을 밀리초 단위로 걸러내고, DB는 최종 방어선 역할만 수행하여 부하를 최소화합니다. Redis 장애 시 DB 폴백 경로도 구현했습니다

**17. 예약 프로세스 — 2단계 (세션 → 확정)**
- 선택: OTA 업계 표준인 2단계 프로세스 채택
- 근거: Amadeus, Expedia, HotelBeds 등 주요 OTA API 공식 문서를 분석한 결과, 모두 2~3단계 예약 프로세스를 채택하고 있었습니다. 서버가 발급한 토큰이 멱등키 역할을 수행하여 재고 선점과 멱등성을 동시에 해결합니다
- 좀비 세션: Redis TTL 이벤트에 의존하면 Redis 장애 시 세션이 영원히 남습니다. DB 기반 스케줄러(`findPendingBefore(cutoff)`)로 만료 세션을 감지하고 재고를 복구하는 방식이 더 안전합니다

**18. Supplier 동기화 — Outbox 2단계 (수집 → 가공)**
- 선택: PROPERTY_CONTENT(컨텐츠, 1일 1회) + RATE_AVAILABILITY(요금/재고, 짧은 주기) 분리
- 이유: 실제 OTA Supplier API 리서치 결과, 컨텐츠와 요금/재고를 분리 제공하고 있었습니다. 수집 주기가 다르므로 분리가 필수입니다
- Service에서 직접 외부 API를 호출하는 구조를 처음에 만들었다가 폐기했습니다. 외부 호출 실패 시 트랜잭션과 외부 호출이 뒤섞여 장애가 전파되는 문제가 있었기 때문입니다

**19. Redis 폴백 — InventoryClientManager**
- 선택: Redis 장애 시 DB `WHERE available_count >= 1`로 자동 폴백
- 이유: Redis가 SPOF가 되면 안 됩니다. InventoryClientManager가 `isRedisConnectionFailure()`로 장애를 감지하면 InventoryCommandManager(DB)로 전환합니다. 테스트에서 `RedisConnectionException` 발생 시 DB 폴백이 정상 동작하는 것을 검증했습니다

### 기술적 문제 해결
- **Lua 스크립트 부분 차감 방지**: 다중 날짜 재고를 하나의 Lua 호출로 DECRBY 후, 하나라도 음수면 전체 INCRBY로 롤백합니다. 10박 예약 시 10개 키를 원자적으로 처리하여 "5일치만 차감되고 나머지는 실패"하는 부분 차감 문제를 원천 차단했습니다
- **보상 트랜잭션 순서**: 예약 세션 생성에서 Redis 차감 후 DB 저장이 실패하면, catch 블록에서 `inventoryClientManager.incrementStock()`으로 재고를 복구합니다. 보상이 실패하는 경우는 좀비 세션 복구 스케줄러가 주기적으로 정리합니다
- **Supplier Service 직접 호출 → Outbox 전환**: 처음에 `DispatchSupplierFetchService`에서 외부 API를 직접 호출하는 구조로 만들었는데, 외부 호출 실패 시 내부 트랜잭션까지 롤백되는 문제가 있었습니다. Outbox + 스케줄러 구조로 전환하여 외부 호출을 트랜잭션 밖으로 분리했습니다
- **검색 크로스 BC 쿼리**: Property + RoomType + Inventory + Rate + RatePlan + Amenity 6개 테이블을 조회하는 검색 쿼리에서, 메인 쿼리는 `select(property.id).from(property)`로 단순하게 두고 나머지 조건은 `JPAExpressions.exists()` 서브쿼리로 처리했습니다. 페이지네이션은 커서 기반(`property.id.gt(cursor)`)으로 구현하여 offset 성능 문제를 회피했습니다

---

## Day 7 — 2026-04-06 [문서 정비 & 제출 준비]

### 수행 내용
- 가산점 문서 작성: 설계/조사, 테스트 코드, AI 활용 기록, 에러 처리 전략, 이벤트 기반 아키텍처, 외부 클라이언트 장애 대응, 성능 테스트 결과
- ERD 현행화: 구현 과정에서 추가된 테이블 4개(ReservationSession, ReservationLine, SupplierRawData, SupplierTask) 반영, Reservation 구조 변경(rate_plan_id → ReservationLine으로 이동) 반영
- 설계 문서 vs 실제 코드 정합성 검증: ADR-001(동시성) 6개 주장 모두 코드와 일치 확인, BC 개수 "11개 → 14개" 수정, 구현 로드맵 현행화
- 리서치 원본 문서 회사명 익명화 (과제 주의사항 준수)
- README 전면 재작성 (실행 방법, 아키텍처, 구현 범위, 가산점 항목)
- 불필요한 중간 산출물 정리 (평가에 직접 기여하지 않는 내부 문서 제거)

---

## 테스트 전략

### 접근 방식
- **H2 대신 Testcontainers**: 동시성 제어(원자적 UPDATE, Lua 스크립트)의 정확한 검증을 위해 실제 MySQL/Redis에서 테스트했습니다
- **레이어별 독립 검증**: Domain은 순수 로직, Application은 흐름, Persistence는 DB 연동, REST API는 요청/응답만 각각 검증합니다
- **동시성 테스트**: ExecutorService 10 스레드로 재고 1개에 동시 10건 예약을 시도하여, 정확히 1건만 성공하고 9건이 실패하는지 검증했습니다

### 테스트 현황
| 레이어 | 유형 | 테스트 수 |
|--------|------|:--------:|
| Domain | 단위 (순수 Java) | 1,000 |
| Application | 단위 (Mockito) | 277 |
| Persistence MySQL | 통합 (Testcontainers) | 191 |
| Persistence Redis | 통합 (Testcontainers) | 23 |
| REST API | 슬라이스 (MockMvc) | 202 |
| E2E + 성능 | 전체 흐름 + 동시성 부하 | 17 |
| **합계** | | **1,710** |
