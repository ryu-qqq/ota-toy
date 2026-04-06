# 부트스트랩 핸드오프 문서

> 새 Claude 세션에서 이 문서를 읽고 바로 이어서 작업할 수 있다.
> 최종 갱신: 2026-04-06 (세션 3 종료 시점)

---

## 현재까지 완료된 것

### 1. 전체 아키텍처 완성

모듈 구조:
- domain/ (200개) — 순수 Java 도메인 모델
- application/ (137개) — UseCase, Port, Manager, Validator, Factory, Assembler, Facade
- adapter-in/rest-api-core/ (5개) — ApiResponse, GlobalExceptionHandler, ErrorMapper
- adapter-in/rest-api-extranet/ (21개) — 파트너 API 9개 엔드포인트
- adapter-in/rest-api-customer/ (16개) — 고객 API 5개 엔드포인트
- adapter-in/rest-api-admin/ (골격만)
- adapter-out/persistence-mysql/ (87개) — JPA Entity, Mapper, Adapter, Flyway 13개
- adapter-out/persistence-redis/ (6개) — Lua 스크립트 3개, InventoryRedisAdapter
- bootstrap/ (3개) — extranet:8080, customer:8081, admin:8082

### 2. 과제 필수 요구사항 6/6 충족

1. 파트너 숙소 등록/관리 — Extranet 9개 엔드포인트 (등록+조회+설정)
2. 고객 숙소 검색/요금 조회 — Customer 검색 + 요금 조회 API
3. 대규모 요금 조회 동시 처리 — Redis 3단 캐싱 (MGET/MSET 일괄 최적화)
4. 예약/취소 — 2단계 프로세스 (세션→확정) + 취소 API
5. 동시 예약 동시성 제어 — Redis Lua + DB 원자적 UPDATE 2중 구조 (ADR-001)
6. Supplier 통합 — ACL + 2단계 동기화 (수집→가공)

### 3. Extranet API (9개)

POST   /properties — 숙소 등록
GET    /properties — 목록 조회 (partnerId, 커서 페이징)
GET    /properties/{id} — 상세 조회 (사진/편의시설/속성값/객실 포함)
PUT    /properties/{id}/photos — 사진 설정 (diff 패턴)
PUT    /properties/{id}/amenities — 편의시설 설정 (diff 패턴)
PUT    /properties/{id}/attributes — 속성값 설정 (diff 패턴)
POST   /properties/{id}/rooms — 객실 등록 (번들 패턴)
POST   /rooms/{id}/rate-plans — 요금 정책 등록
PUT    /rate-plans/{id}/rates — 요금/재고 설정 + Redis 초기화

### 4. Customer API (5개)

GET    /search/properties — 숙소 검색
GET    /properties/{id}/rates — 요금 조회 (Redis 캐시)
POST   /reservation-sessions — 예약 세션 1단계 (Idempotency-Key 헤더)
POST   /reservations — 예약 확정 2단계 (sessionId)
PATCH  /reservations/{id}/cancel — 예약 취소

### 5. 핵심 설계 패턴

diff 패턴: 사진/편의시설/속성값의 등록+수정을 하나의 UseCase로 (added/removed/retained)
번들 패턴: RoomType + Bed + View 원자적 저장 (forPending + withRoomTypeId)
ErrorCategory: ErrorCode에 카테고리(NOT_FOUND/VALIDATION/CONFLICT/FORBIDDEN) 추가
ErrorMapper 추상화: 인터페이스 + ErrorMapperRegistry + 모듈별 구현체
Assembler: 도메인 → Application DTO 변환 (Port/ReadManager는 도메인만 반환)
CriteriaFactory: Query DTO → Domain Criteria 변환
2단계 예약: 세션(멱등키) → 결제 → 확정
동시성 2중: Redis Lua 1차 게이트키퍼 + DB WHERE available_count >= 1 최종 정합성
Supplier ACL: 수집(외부API→Raw저장) → 가공(ACL변환→Diff→Property저장)
좀비 복구: DB 기반 스케줄러 (Redis TTL 이벤트 비의존)

### 6. 테스트

Domain: 46개 + Fixture 12개
Application: 12개 + Fixture 10개
Persistence: 10개 (Testcontainers MySQL — MySqlTestContainerConfig 싱글톤)
REST API: Extranet 4개 + Customer 3개 (MockMvc + REST Docs)

### 7. 설계 문서

ADR-001: docs/design/adr/adr-001-reservation-concurrency.md
OTA 리서치: docs/research/ (5개 — 플랫폼 분석, 크로스 비교, 도메인 검증, 등록 플로우, 멱등키)
저널 시드: docs/seeds/ (8개)
REST Docs: adapter-in/rest-api-extranet/src/docs/asciidocs/ (index.adoc + CSS)

---

## 다음 세션 필수 작업 (우선순위순)

### P0 — 반드시

1. Supplier 코드 리뷰 + 리팩토링
   - MockSupplierClientAdapter가 persistence-mysql에 있음 → adapter-out/client/ 분리
   - SupplierTranslator 변환 로직 검증
   - Supplier 테스트 작성

2. 컴파일 에러 정리
   - domain:test ArchUnit DOM-AGG-014 (RatePlans.of() 등 일급 컬렉션)
   - CustomerGetRateService 타입 불일치 (RoomTypes, RatePlans, Inventories)

3. 과정 기록서 + AI 활용 기록 최종 문서 생성
   - /journal 스킬로 시드 → 최종 문서

4. 성능 테스트
   - 동시성: 10 동시 요청, 재고 1개, 정확히 1개 성공
   - Redis Lua 원자성 검증

### P1 — 높음

5. Persistence/API 테스트 보강
   - ReservationSession/Reservation 통합 테스트
   - Customer 예약 API MockMvc 테스트
   - Supplier Persistence 테스트

6. REST Docs 완성
   - Customer API REST Docs
   - asciidoctor 빌드

7. Outbox 패턴 (이벤트 발행)

### P2 — 중간

8. Admin API (가산점)
9. 컨벤션 문서 최종 정리

---

## 핵심 참조 파일

.claude/CLAUDE.md — 프로젝트 전체 설정
docs/design/application-convention.md — Application 컨벤션
docs/design/persistence-convention.md — Persistence 컨벤션
docs/design/api-convention.md — API 컨벤션
docs/design/adr/adr-001-reservation-concurrency.md — 동시성 ADR
docs/research/ota-idempotency-patterns.md — 멱등키 리서치
docs/backlog.md — 백로그
docs/erDiagram.md — ERD

---

## 커밋 이력 (세션 3)

45c0609 feat: STORY-402/403 Supplier ACL + 2단계 동기화 (수집→가공)
ed71b88 feat: STORY-303 예약 취소 + Customer 예약 REST API
e6f6b50 feat: STORY-302 예약 Persistence 구현 (ReservationSession + Reservation)
1ae9f72 feat: 예약 2단계 프로세스 + 멱등키 + 좀비 세션 복구
6ce5ed1 feat: STORY-302 예약 생성 + Redis/DB 2중 동시성 제어 (ADR-001)
0455f39 feat: Extranet 숙소 조회 API + 조회 컨벤션 정비
0aa6602 feat: STORY-107a/b RatePlan 등록 + 요금/재고 설정 E2E 완성
93808c8 feat: Phase 2 숙소 등록 E2E 완성 (Application + Persistence + REST API)
