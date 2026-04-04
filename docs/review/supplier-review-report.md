# Supplier BC 리뷰 보고서

> 실행일: 2026-04-04
> 모드: review (Phase 2~6)
> 대상: domain/src/main/java/com/ryuqq/otatoy/domain/supplier/

---

## Phase 2: ArchUnit

| 결과 | 상세 |
|------|------|
| BUILD SUCCESSFUL | 12/12 규칙 통과 |

---

## Phase 3: 리뷰 결과

### Code Review

| 체크리스트 | 판정 |
|-----------|------|
| DOM-AGG-001 팩토리 메서드 | PASS (4/4 파일) |
| DOM-AGG-002 ID VO | PASS |
| DOM-AGG-004 Setter 금지 | PASS |
| DOM-AGG-010 equals/hashCode | PASS |
| DOM-VO-001 VO Record | PASS |
| DOM-VO-002 Enum displayName | PASS (3/3 enum) |
| DOM-ERR-001 ErrorCode | PASS |
| DOM-EXC-001 Exception | **FAIL** -- Exception 클래스 부재 |
| DOM-CMN-002 외부 의존 금지 | PASS |

추가 발견:
- **[MAJOR]** 상태 전이 가드 없음 (Supplier.suspend/activate/terminate)
- **[MAJOR]** forNew() 필수 필드 검증 부재
- **[MAJOR]** syncType이 String (타입 안전성 부재)
- **[MINOR]** SupplierRoomType에 unmap() 누락
- **[MINOR]** forNew() ID 전달 방식 비일관성
- **[MINOR]** forNew() 네이밍 (성공/실패 구분 불명확)

### Spec Review

- **FAIL**: 상태 전이 규칙 미준수 (TERMINATED에서 재활성화 가능)
- **FAIL**: 숙소 매핑 해제 시 하위 객실 연쇄 처리 불가
- **WARN**: SupplierApiConfig 모델 미구현 (ERD에 정의됨)
- **WARN**: 공급자 정지/해지 시 하위 매핑 정책 미정의
- **WARN**: SyncLog 카운트 정합성 검증 없음

---

## Phase 4: FIX 루프

| 라운드 | 수정 내용 | 결과 |
|--------|----------|------|
| 1/3 | 상태 전이 가드, forNew 검증, syncType Enum화, Exception 클래스, unmap() 추가, equals 개선 | **재리뷰 PASS** |

### 수정된 파일 목록

1. `Supplier.java` -- 상태 전이 가드, forNew 검증, ID 일관성, equals/hashCode 개선
2. `SupplierSyncLog.java` -- syncType String->SupplierSyncType, forNew->forSuccess 리네이밍, equals/hashCode 개선
3. `SupplierRoomType.java` -- unmap() 추가, equals/hashCode 개선
4. `SupplierProperty.java` -- equals/hashCode 개선
5. `SupplierErrorCode.java` -- 3개 코드 추가 (SUP-004~006)
6. `SupplierSyncType.java` -- 신규 생성 (Enum VO)
7. `SupplierException.java` -- 신규 생성 (기반 예외)
8. `SupplierNotFoundException.java` -- 신규 생성
9. `SupplierAlreadySuspendedException.java` -- 신규 생성
10. `SupplierAlreadyTerminatedException.java` -- 신규 생성
11. `InvalidSupplierStateTransitionException.java` -- 신규 생성

---

## Phase 5: 테스트

| 테스트 클래스 | 테스트 수 | 결과 |
|-------------|----------|------|
| SupplierTest | 20 | PASS |
| SupplierPropertyTest | 10 | PASS |
| SupplierRoomTypeTest | 10 | PASS |
| SupplierSyncLogTest | 9 | PASS |
| SupplierNameTest | 4 | PASS |
| SupplierErrorCodeTest | 3 | PASS |
| **합계** | **56** | **ALL PASS** |

### 테스트 커버리지 카테고리

| 카테고리 | 시나리오 수 |
|----------|-----------|
| 생성 (forNew/forSuccess/forFailed) | 12 |
| 복원 (reconstitute) | 5 |
| 상태 전이 (suspend/activate/terminate/synced/unmap/markFailed) | 15 |
| 검증 (null/blank 거부) | 9 |
| 동등성 (equals/hashCode) | 12 |
| VO/ErrorCode | 7 |

---

## equals/hashCode 개선 사항

ID VO를 사용하는 엔티티(Supplier, SupplierProperty, SupplierRoomType, SupplierSyncLog)에서 `XXXId.of(null)`로 생성된 forNew() 객체의 동등성 문제를 발견하고 수정했다.

**문제**: `id != null` 체크만으로는 `SupplierId(null)` 같은 래퍼 객체가 null이 아니므로 통과하여, 아직 persist되지 않은 두 객체가 동등하다고 판정되는 문제.

**해결**: `id != null && id.value() != null` 체크로 변경. hashCode도 id.value()가 null이면 identityHashCode를 사용하도록 개선.

---

## 미해결 사항 (Phase 5 범위 밖)

1. **SupplierApiConfig**: ERD에 정의되어 있으나 도메인 모델 미구현. 향후 구현 필요.
2. **Supplier 정지/해지 시 하위 매핑 정책**: Application 레이어에서 처리할 것으로 판단.
3. **SyncLog 카운트 정합성 검증**: totalCount >= sum(created+updated+deleted) 검증은 Application 레이어에서 처리 가능.
4. **다른 BC의 동일한 equals/hashCode 문제**: Partner, Landmark 등도 같은 패턴을 가지고 있을 수 있음.
