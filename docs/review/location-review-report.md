# Location BC Review Report

> 실행일: 2026-04-04
> 모드: review (Phase 2~6)
> 대상: domain/src/main/java/com/ryuqq/otatoy/domain/location/

---

## Phase 2: ArchUnit

| 결과 | 상세 |
|------|------|
| PASS | 모든 ArchUnit 규칙 통과 (외부 의존 금지, Setter 금지, 생성자 제한, VO Record, Enum displayName, 시간 직접 생성 금지 등) |

---

## Phase 3: 리뷰 결과

### code-reviewer (9개 체크리스트 + 추가 발견)

| # | 체크리스트 | 결과 | 심각도 | 상세 |
|---|-----------|------|--------|------|
| 1 | DOM-AGG-001: 팩토리 메서드 | PASS | - | forNew() + reconstitute() 패턴 준수 |
| 2 | DOM-AGG-002: ID VO | PASS | - | LandmarkId, PropertyLandmarkId Record |
| 3 | DOM-AGG-004: Setter 금지 | PASS | - | set 메서드 없음 |
| 4 | DOM-AGG-010: equals/hashCode | PASS | - | ID 기반 구현 |
| 5 | DOM-VO-001: VO Record | PASS | - | Location, LandmarkName 등 Record |
| 6 | DOM-VO-002: Enum displayName | PASS | - | LandmarkType displayName() 구비 |
| 7 | DOM-CMN-002: 외부 의존 금지 | **FAIL -> FIX** | BLOCKER | PropertyLandmark가 accommodation.PropertyId 직접 참조 |
| 8 | DOM-ERR-001: ErrorCode enum | **FAIL -> FIX** | MAJOR | LocationErrorCode 부재 |
| 9 | DOM-EXC-001: DomainException | **FAIL -> FIX** | MAJOR | IllegalArgumentException 직접 사용 |
| 10 | forNew() 비즈니스 검증 | **FAIL -> FIX** | MAJOR | Landmark.forNew() 검증 누락 |
| 11 | propertyId null 체크 | **FAIL -> FIX** | MINOR | PropertyLandmark.forNew() |

### spec-reviewer

| # | 비즈니스 규칙 | 결과 | 상세 |
|---|-------------|------|------|
| 1 | Landmark-Property 거리/도보 매핑 | PASS | ERD 정합 |
| 2 | LandmarkType 다양성 | WARN | 5개 타입으로 현재 범위 충분 |
| 3 | BC 간 의존 방향 | **FAIL -> FIX** | accommodation 의존 제거 |
| 4 | distanceKm/walkingMinutes 일관성 | WARN | 현재 범위에서 선택적 |
| 5 | Location VO 패키지 위치 | PASS | location 패키지가 적절 |

---

## Phase 4: FIX 루프

| 라운드 | 수정 내용 | 결과 |
|--------|----------|------|
| 1/3 | 5건 수정 (BLOCKER 1 + MAJOR 3 + MINOR 1) | 전체 PASS |

### FIX 상세

1. **BLOCKER** PropertyLandmark: `accommodation.PropertyId` -> `long propertyId` (BC 간 결합 제거)
2. **MAJOR** LocationErrorCode.java 신규 생성 (LOC-001 ~ LOC-010)
3. **MAJOR** LocationException.java 신규 생성 (DomainException 상속)
4. **MAJOR** Landmark.forNew(): name, type null 체크 + latitude/longitude 범위 검증 추가
5. **MINOR** PropertyLandmark.forNew(): propertyId <= 0 검증 추가
6. Location.java, LandmarkName.java: IllegalArgumentException -> LocationException 교체
7. 기존 LocationTest.java: 예외 타입 + 메시지 검증 갱신

---

## Phase 5: 테스트

### 신규 테스트 파일

| 파일 | 테스트 수 | 카테고리 |
|------|----------|---------|
| LandmarkTest.java | 14 | forNew 검증, reconstitute, equals/hashCode |
| PropertyLandmarkTest.java | 11 | forNew 검증, reconstitute, equals/hashCode |
| LocationVoTest.java | 14 | LandmarkId, PropertyLandmarkId, LandmarkName, LandmarkType |

### 기존 테스트 수정

| 파일 | 변경 | 이유 |
|------|------|------|
| accommodation/LocationTest.java | 예외 타입 갱신 | IllegalArgumentException -> LocationException |

### Fixture

| 파일 | 위치 |
|------|------|
| LocationFixture.java | domain/src/testFixtures/java/.../location/ |

---

## Phase 6: 파일 매니페스트

### 수정된 파일

| 파일 | 변경 유형 |
|------|----------|
| domain/.../location/PropertyLandmark.java | 수정 (PropertyId -> long, 검증 추가) |
| domain/.../location/Landmark.java | 수정 (forNew() 검증 추가) |
| domain/.../location/Location.java | 수정 (예외 타입 변경) |
| domain/.../location/LandmarkName.java | 수정 (예외 타입 변경) |

### 신규 생성 파일

| 파일 | 유형 |
|------|------|
| domain/.../location/LocationErrorCode.java | ErrorCode enum |
| domain/.../location/LocationException.java | DomainException |
| domain/src/testFixtures/.../location/LocationFixture.java | 테스트 Fixture |
| domain/src/test/.../location/LandmarkTest.java | 단위 테스트 |
| domain/src/test/.../location/PropertyLandmarkTest.java | 단위 테스트 |
| domain/src/test/.../location/LocationVoTest.java | VO 단위 테스트 |

### 미변경 파일

| 파일 | 이유 |
|------|------|
| LandmarkId.java | 리뷰 통과 |
| PropertyLandmarkId.java | 리뷰 통과 |
| LandmarkType.java | 리뷰 통과 |

---

## 알려진 이슈 (범위 밖)

1. **RateRuleTest 실패** -- 기존 pricing BC 테스트 1건 실패. location BC와 무관.
2. **Landmark equals 설계 주의** -- ID VO(`LandmarkId(null)`)를 쓰면 `id != null` 체크가 의도대로 동작하지 않음. `LandmarkId(null)`은 객체 자체가 null이 아니므로 forNew()로 만든 두 객체가 equals true. 전체 BC에 걸친 설계 이슈로 별도 검토 권장.
