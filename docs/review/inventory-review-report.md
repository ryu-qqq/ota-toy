# Inventory BC 리뷰 보고서

> 실행 일시: 2026-04-04
> 모드: review (기존 코드 검증)
> 대상: domain/src/main/java/com/ryuqq/otatoy/domain/inventory/

---

## 파이프라인 실행 요약

| Phase | 결과 | 상세 |
|-------|------|------|
| Phase 2: ArchUnit | PASS | 12/12 규칙 통과 |
| Phase 3: code-reviewer | PASS 9 / ISSUE 5 (MAJOR 3, MINOR 2) | BLOCKER 0 |
| Phase 3: spec-reviewer | PASS 3 / WARN 2 / FAIL 2 | |
| Phase 4: FIX 루프 | Round 1/3 완료 | 수정 3건 -> 재리뷰 전원 PASS |
| Phase 5: 테스트 | 41/41 통과 | InventoryTest 36 + InventoryIdTest 5 |

---

## Phase 3: Code Review 결과

### 체크리스트 (9개 항목)

| # | 규칙 | 판정 |
|---|------|------|
| 1 | DOM-AGG-001: 팩토리 메서드 패턴 | PASS |
| 2 | DOM-AGG-002: ID VO | PASS |
| 3 | DOM-AGG-004: Setter 금지 | PASS |
| 4 | DOM-AGG-010: equals/hashCode ID 기반 | PASS |
| 5 | DOM-VO-001: VO는 Record | PASS |
| 6 | DOM-ERR-001: ErrorCode 구조 | PASS |
| 7 | DOM-EXC-001: Exception 구조 | PASS |
| 8 | DOM-CMN-002: 외부 의존 금지 | PASS |
| 9 | DOM-TIME: 시간 직접 생성 금지 | PASS |

### ISSUE 목록

| # | 심각도 | 파일 | 내용 | FIX |
|---|--------|------|------|-----|
| C-1 | MAJOR | Inventory.java | decrease(int count) 미지원 | FIX-REQ-3 |
| C-2 | MAJOR | Inventory.java | restore() 무제한 복구 (상한 없음) | 현행 유지 (Redis가 실제 관리) |
| C-3 | MAJOR | Inventory.java | updateAvailableCount 부재 | FIX-REQ-1 |
| C-4 | MINOR | Inventory.java | 0개 재고 생성 허용 | 의도적 -- FIX 불필요 |
| C-5 | MINOR | InventoryId.java | isNew() 미사용 | Persistence에서 사용 예정 |

---

## Phase 3: Spec Review 결과

| # | 검증 항목 | 판정 | 비고 |
|---|-----------|------|------|
| S-1 | ERD 필드 일치 | PASS | |
| S-2 | FB-13: 판매 재고만 관리 | PASS | |
| S-3 | Redis 패턴 역할 분담 | WARN | 도메인은 DB 기록용 보조 역할 |
| S-4 | version 필드 관리 | WARN | JPA @Version으로 관리 -- 도메인 현행 유지 |
| S-5 | 재고 수량 업데이트 메서드 부재 | FAIL -> FIXED | updateAvailableCount 추가 |
| S-6 | restore() 비즈니스 규칙 불완전 | FAIL -> 현행유지 | stopSell 무관 복구가 올바른 판단 |

---

## Phase 4: FIX 적용 내역

### Round 1 (최종)

| FIX-REQ | 내용 | 적용 결과 |
|---------|------|-----------|
| FIX-REQ-1 | updateAvailableCount(int newCount) 추가 | PASS |
| FIX-REQ-2 | restore(int count) 오버로드 추가 | PASS |
| FIX-REQ-3 | decrease(int count) 오버로드 추가 | PASS |

### 변경된 파일
- `domain/src/main/java/com/ryuqq/otatoy/domain/inventory/Inventory.java`

---

## Phase 5: 테스트 결과

### 테스트 파일
- `domain/src/test/java/com/ryuqq/otatoy/domain/inventory/InventoryTest.java` (36 tests)
- `domain/src/test/java/com/ryuqq/otatoy/domain/inventory/InventoryIdTest.java` (5 tests)

### Fixture
- `domain/src/testFixtures/java/com/ryuqq/otatoy/domain/inventory/InventoryFixture.java`

### 테스트 카테고리

| 카테고리 | 테스트 수 | 내용 |
|---------|----------|------|
| T-1: 생성 (forNew) | 5 | 정상 생성, 0개 허용, null 검증 3건 |
| T-2: DB 복원 (reconstitute) | 2 | 필드 복원, 비즈니스 검증 미수행 확인 |
| T-3: 재고 차감 (decrease) | 8 | 1개/N개 차감, 소진, stopSell, 경계값 |
| T-4: 재고 복구 (restore) | 6 | 1개/N개 복구, stopSell 상태 복구, 경계값 |
| T-5: 수량 설정 (updateAvailableCount) | 3 | 정상 설정, 0 허용, 음수 거부 |
| T-6: 판매 제어 (stopSell/resumeSell) | 3 | 중지/재개/0개 재고 재개 |
| T-7: 가용성 (isAvailable) | 3 | 정상/0개/stopSell |
| T-8: 동등성 (equals/hashCode) | 3 | 같은ID/다른ID/null ID |
| T-9: 복합 시나리오 | 3 | 차감->복구, stopSell->복구->재개, 수량변경->차감 |

---

## 설계 판단 기록

### 왜 restore()에서 stopSell 검증을 하지 않는가
예약 취소로 인한 재고 복구는 판매 상태와 무관하게 수행되어야 한다. 판매 중지는 "새로운 예약을 받지 않겠다"는 의미이지 "기존 취소된 재고를 복구하지 않겠다"는 의미가 아니다. 재고 정합성이 우선한다.

### 왜 restore() 상한 검증을 하지 않는가
base_inventory(초기 재고)는 도메인 모델이 보유하지 않는다. Redis 원자적 카운터가 실제 재고 관리 주체이고, 도메인 모델의 Inventory는 DB 기록용 보조 역할이다. 상한 검증은 Application 또는 Redis 레이어에서 처리한다.

### 왜 version을 도메인에서 직접 증가시키지 않는가
version은 JPA Persistence 레이어에서 @Version 어노테이션으로 자동 관리된다. 도메인이 version을 직접 조작하면 JPA의 낙관적 잠금 메커니즘과 충돌한다. 도메인은 version을 읽기 전용으로만 노출한다.
