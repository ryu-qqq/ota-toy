# Reservation BC -- Test Report

> 테스트 일시: 2026-04-04
> 모드: review -> Phase 5 (test-designer)

---

## 테스트 현황

### 기존 테스트 (4개 파일)

| 파일 | 테스트 수 | 커버리지 |
|------|:--------:|---------|
| ReservationTest.java | 31개 | 생성 검증 16 + 상태 전이 12 + reconstitute 1 + 동등성 2 |
| ReservationItemTest.java | 5개 | 생성 4 + reconstitute 1 |
| GuestInfoTest.java | 7개 | 생성 5 + 동등성 2 |
| ReservationIdTest.java | 3개 | isNew 2 + 동등성 1 |

### 추가 테스트 (5개 파일, 신규)

| 파일 | 테스트 수 | 카테고리 |
|------|:--------:|---------|
| ReservationItemIdTest.java | 4개 | T-5: ID VO isNew/동등성 |
| ReservationNoTest.java | 5개 | T-5: VO 생성/동등성 |
| ReservationStatusTest.java | 7개 | T-5: Enum displayName 전체 검증 |
| ReservationErrorCodeTest.java | 7개 | T-5: ErrorCode code/httpStatus/message |
| ReservationItemEqualityTest.java | 3개 | T-6: ReservationItem 동등성 |

### 테스트 총계

| 구분 | 파일 수 | 테스트 수 |
|------|:------:|:--------:|
| 기존 | 4 | 46 |
| 추가 | 5 | 26 |
| **합계** | **9** | **72** |

---

## testFixtures

| 파일 | 위치 |
|------|------|
| ReservationFixture.java | domain/src/testFixtures/java/com/ryuqq/otatoy/domain/reservation/ |

메서드 11개 제공 (상세는 fixture-catalog.md 참조)

---

## 실행 결과

```
./gradlew :domain:test --tests "com.ryuqq.otatoy.domain.reservation.*"
BUILD SUCCESSFUL
```

reservation 패키지 테스트 전체 통과.

참고: LocationTest 5건 실패는 reservation과 무관한 기존 이슈 (accommodation BC 소관).

---

## 미커버 영역 (향후 추가 고려)

- ReservationException 직접 생성 테스트 (protected 생성자이므로 직접 테스트 어려움)
- Reservation.items()의 방어적 복사 검증 (기존 ReservationTest에 포함)
- Cross-BC 참조 ID (RatePlanId, InventoryId) 유효성은 Application 레이어에서 검증
