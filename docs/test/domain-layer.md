# Domain 레이어 테스트 시나리오

> 순수 Java 단위 테스트로 도메인 모델의 비즈니스 규칙, 불변식, 상태 전이를 검증한다.  
> Spring/JPA 의존 없음. 총 **1,019개 테스트** (50개 테스트 클래스 + ArchUnit 1개)

---

## 테스트 환경

| 항목 | 설정 |
|------|------|
| 프레임워크 | JUnit 5 + AssertJ |
| 외부 의존 | **없음** (순수 Java) |
| 구조 | `@Nested` + `@DisplayName` 계층화 |
| Fixture | `testFixtures` 소스셋으로 도메인 객체 생성 헬퍼 공유 |
| 아키텍처 | ArchUnit으로 레이어 규칙 자동 강제 |

---

## 검증 카테고리 체계

| 카테고리 | 코드 | 검증 내용 |
|----------|------|----------|
| 생성 검증 | T-1 | forNew() 팩토리, 필수값 null/경계값 검증 |
| 상태 전이 | T-2 | 허용/불허 전이, 예외 발생 |
| DB 복원 | T-3 | reconstitute() — 검증 없이 필드 복원 |
| 도메인 로직 | T-4 | 비즈니스 메서드 (계산, 판단, 변환) |
| VO 검증 | T-5 | 값 객체 생성, 경계값, 불변성 |
| 동등성 | T-6 | equals/hashCode (ID 기반 엔티티, 값 기반 VO) |

---

## Accommodation BC (12개 테스트)

### AccommodationEnumTest
- AmenityType — displayName, 전체 개수
- PhotoType — displayName, 전체 개수 9개

### AmenityNameTest
- 생성: 정상/null/빈값/200자 경계값
- 동등성: 같은 값 동등

### VoTest
- BrandId, PropertyTypeId, PartnerId, PropertyId, RoomTypeId — isNew() 판별
- BrandName, PropertyTypeCode — null/빈값 검증

### AccommodationErrorCodeTest
- ErrorCode 5개 전수 검증, 접두사/카테고리

---

## Brand BC (27개 테스트)

### BrandTest (10개)
- **T-1 생성**: forNew() → id null, nullable 필드
- **T-2 복원**: reconstitute() → 전체 필드 복원
- **T-3 이름 변경**: rename() → name/updatedAt 갱신, 다른 필드 유지
- **T-4 로고 변경**: updateLogoUrl() → null 허용
- **T-5 동등성**: 같은/다른 ID, forNew() null ID

### BrandVoTest (17개)
- BrandId: of(), isNew(), forNew(), 동등성
- BrandName: null/빈값/100자 경계값
- BrandNameKr: nullable/200자 경계값
- LogoUrl: nullable/500자 경계값

---

## Common (53개 테스트)

### MoneyTest (13개)
- 생성: BigDecimal/int/0원/null/음수
- 연산: add, multiply, 0 곱하기
- 비교: greaterThan, lessThan, 같은 금액
- 동등성 + 불변성: 연산 후 원본 유지

### EmailTest (7개), PhoneNumberTest (9개), CoordinateTest (11개)
- 생성 + 경계값 + 동등성
- Coordinate: 위도 ±90, 경도 ±180 경계값

### DateRangeTest (10개)
- 생성: null/같은날/역전 차단
- 로직: nights() 계산, dates() 스트림

### CdnUrl/OriginUrl/DeletionStatus (13개)
- nullable 처리, 경계값, 동등성

### DomainExceptionTest (7개)
- ErrorCode/args 생성, null args 처리, args 불변성, ErrorCategory 전체 값

### QueryTest (12개)
- PageRequest: 음수page/size범위/offset 계산
- CursorPageRequest: size 범위, null cursor
- PageMeta/SliceMeta/QueryContext/SortDirection/SortKey

---

## Inventory BC (92개 테스트)

### InventoryTest (46개)
- **T-1 생성**: forNew() — availableCount == totalInventory, null/0/음수 차단
- **T-2 복원**: reconstitute() — 음수 재고도 복원 가능
- **T-3 차감**: decrease() — 1개/N개 차감, 재고 부족 → InventoryExhaustedException, 판매 중지 → InventoryStopSellException
- **T-4 복구**: restore() — 1개/N개, 판매 중지에서도 복구 가능, 초과 → InventoryOverflowException
- **T-5 수량 설정**: updateAvailableCount() — 0 허용, 초과/음수 차단
- **T-5b 전체 수량**: updateTotalInventory() — 증감 시 가용 수량 연동, 예약보다 작으면 차단
- **T-6 판매 제어**: stopSell/resumeSell, 재고 0에서 resume → isAvailable false
- **T-7~T-10**: 가용성, 예약 수량, 동등성, 복합 시나리오 (차감→복구→가용성, 판매중지→복구→재개)

### InventoriesTest (8개)
- from() 생성: null/빈/정상, 불변성
- groupByRoomTypeAndDate() 그룹핑

### InventoryErrorCodeTest (13개)
- INV- 접두사, 카테고리 매핑, 전수 검증

### InventoryExceptionTest (19개)
- 4개 예외 (Exhausted/NotFound/StopSell/Overflow): 상속, 에러 코드, 메시지, 카테고리
- 도메인 로직 통합 검증: 실제 차감/복구에서 올바른 예외 발생

### InventoryIdTest (6개)
- of(), forNew(), isNew()

---

## Location BC (20개 테스트)

### LandmarkTest (12개)
- forNew(): 정상, null name/type, 위도/경도 경계값
- reconstitute() 복원, equals/hashCode

### PropertyLandmarkTest (11개)
- forNew(): 정상, propertyId/landmarkId 검증, distanceKm/walkingMinutes 경계값
- reconstitute(), equals/hashCode

### LocationVoTest (15개)
- LandmarkId, PropertyLandmarkId, LandmarkName, LandmarkType

### LocationErrorCodeTest (8개)
- LOC- 접두사, 에러 코드 중복 없음, LocationException

---

## Member BC (54개 테스트)

### MemberTest (16개)
- **T-1 생성**: forNew() → CUSTOMER/ACTIVE, null email/password/name 차단
- **T-2 상태 전이**: ACTIVE ↔ SUSPENDED, 동일 상태 전이 예외
- **T-3 비밀번호**: 변경 성공, null 차단
- **T-4 이름 변경**, **T-5 reconstitute**, **T-6 동등성**

### MemberVoTest (42개)
- MemberId: of/isNew/forNew/동등성
- MemberEmail: null/빈값/@없음/200자 경계값
- MemberPassword: null/빈값/500자 경계값
- MemberName: null/빈값/100자 경계값
- MemberStatus: canTransitTo() 모든 조합, transitTo(), displayName
- MemberRole: displayName
- MemberErrorCode: code/카테고리 검증
- MemberException/MemberNotFoundException: 에러 코드, 상속

---

## Partner BC (38개 테스트)

### PartnerTest (11개)
- forNew() → ACTIVE, reconstitute(), suspend/activate 상태 전이, 중복 전이 예외, equals/hashCode

### PartnerMemberTest (15개)
- forNew() → ACTIVE, null 검증, reconstitute()
- suspend/activate 상태 전이, changeRole, updateProfile, equals/hashCode

### PartnerEnumTest, PartnerErrorCodeTest, PartnerExceptionTest (각 ~4개)
- displayName, PTN- 접두사, DomainException 상속

### PartnerVoTest (8개)
- PartnerId, PartnerMemberId, PartnerName, MemberName

---

## Pricing BC (69개 테스트)

### RatePlanTest (12개)
- forNew(): DIRECT/SUPPLIER, null roomTypeId/paymentPolicy, SUPPLIER에서 null supplierId, 무료취소+환불불가 동시 설정
- updatePolicy(), reconstitute(), equals/hashCode

### RateRuleTest (28개)
- forNew(): null startDate/endDate, 역전, 1일짜리 규칙, null/음수/0 basePrice
- **calculatePrice()**: 월~일 요일별 가격, null fallback → basePrice, 범위 밖 예외
- **resolvePrice()**: 오버라이드 반영/미반영, null/빈 오버라이드
- **covers()**: 시작일/종료일/범위 내/범위 밖

### RateOverrideTest (12개)
- forNew(): null/음수 price, null reason 허용, 날짜 범위 경계값
- reconstitute(), equals/hashCode

### RateRulesTest, RateOverridesTest (각 ~7개)
- 일급 컬렉션: 날짜 겹침/부분 겹침 검증, reconstitute (겹침 무검증)

### RateTest (10개)
- forNew(): null/음수/0 basePrice
- updatePrice(), reconstitute(), equals/hashCode

### RatePlanAddOnTest (12개)
- forNew(): 유료/무료, included+price 조합 검증
- isFree() 비즈니스 로직, reconstitute(), equals/hashCode

### PricingVoTest (6개)
- RatePlanName, AddOnName, AddOnType, RatePlanId, PricingErrorCode, Enum displayName

---

## Property BC (72개 테스트)

### PropertyTest (5개)
- forNew(): 정상, null partnerId
- 상태 전이: ACTIVE → INACTIVE → ACTIVE

### PropertyNameTest (6개)
- null/빈값/공백/100자 경계값

### PropertyPhotoTest (3개), PropertyAmenityTest (7개)
- 생성 검증, isFree() 로직

### PropertyAttributeValueTest (11개)
- 생성: null propertyId/attributeId 차단
- soft delete: 정상/멱등, attributeKey(), reconstitute(), equals/hashCode

### PropertyCollectionTest (22개)
- **PropertyAmenities**: 빈/null 생성, sortOrder 중복, **update() diff 패턴** (추가/삭제/유지/혼합), allPersistTargets()
- **PropertyPhotos**: sortOrder 중복, update() diff
- **PropertyAttributeValues**: attributeId 중복, update() diff, allPersistTargets()

### PropertySliceCriteriaTest (17개)
- 필수값: checkIn/checkOut/guests/size
- 경계값: starRating 0~6, 가격 범위
- 로직: stayDates(), nights()

### RateFetchCriteriaTest (10개), ExtranetPropertySliceCriteriaTest (7개)

### PropertyVoTest (32개)
- PropertyId, PropertyAmenityId, PropertyPhotoId, PropertyAttributeValueId
- PropertyDescription (2000자), PromotionText (500자), PropertyStatus, PropertySortKey
- 예외: PropertyNotFoundException, RequiredPropertyAttributeMissingException

---

## PropertyType BC (41개 테스트)

### PropertyTypeTest (7개)
- forNew() → id null, reconstitute(), updateInfo(), equals/hashCode

### PropertyTypeAttributeTest (11개)
- forNew(): 필수/선택, null attributeKey/attributeName/valueType
- reconstitute(), equals/hashCode

### PropertyTypeVoTest (20개)
- PropertyTypeId, PropertyTypeAttributeId, PropertyTypeCode (50자), PropertyTypeName (200자), PropertyTypeDescription (2000자)

### PropertyTypeErrorCodeTest (1개), PropertyTypeExceptionTest (2개)

---

## Reservation BC (72개 테스트)

### ReservationTest (37개)
- **T-1 생성**: forNew() → PENDING, customerId/guestInfo/stayPeriod/guestCount/totalAmount/lines null 차단, 과거 체크인 차단, 30박 초과 차단, 다객실 조합
- **T-2 상태 전이**: confirm/cancel/complete/noShow — 모든 허용/불허 조합 전수 검증
  - PENDING → CONFIRMED/CANCELLED
  - CONFIRMED → CANCELLED/COMPLETED/NO_SHOW
  - 이미 취소 → ReservationAlreadyCancelledException (RSV-003)
  - 이미 완료 → ReservationAlreadyCompletedException (RSV-004)
- **T-3 reconstitute**, **T-6 동등성**

### ReservationLineTest (12개)
- forNew(): null ratePlanId, roomCount 0/음수, null items/빈 items, 불변 복사
- reconstitute(), equals/hashCode

### ReservationItemTest (5개)
- forNew(): null stayDate/inventoryId/nightlyRate
- reconstitute()

### GuestInfoTest (10개)
- 생성: null name, null phone/email 허용, 잘못된 포맷
- 동등성: Record 특성

### ID/Status/ErrorCode (8개)
- ReservationId, ReservationItemId, ReservationLineId, ReservationNo
- ReservationStatus: 5개 상태 displayName
- ReservationErrorCode: RSV- 접두사

---

## RoomAttribute BC (32개 테스트)

### BedTypeTest (17개)
- 생성/reconstitute
- BedTypeCode: null/빈값/50자 경계값
- BedTypeName: null/빈값/200자 경계값
- BedTypeId: null/forNew/isNew
- 동등성

### ViewTypeTest (15개)
- 동일 구조로 ViewTypeCode/ViewTypeName/ViewTypeId 검증

---

## RoomType BC (110개 테스트)

### RoomTypeTest (32개)
- **생성**: forNew(), baseOccupancy > maxOccupancy, areaSqm 0/음수/null, 객실명 null/빈값, 재고 음수
- **상태 전이**: ACTIVE ↔ INACTIVE, 멱등 비활성화
- **정보 수정**: updateInfo/updateInventory/updateCheckInOut, 불변식 위반
- **VO 경계값**: RoomTypeName 200/201자, RoomTypeDescription 2000/2001자, RoomTypeId, baseOccupancy 0
- **동등성**, **reconstitute**

### RoomTypeBedTest (12개)
- 생성: 정상, quantity 0/음수, bedTypeId null
- **번들 패턴**: forPending → withRoomTypeId (원본 불변 복사)
- 동등성

### RoomTypeViewTest (9개)
- 생성: viewTypeId null, roomTypeId null
- **번들 패턴**: forPending → withRoomTypeId
- 동등성

### RoomTypeAttributeTest (7개)
- 생성: attributeKey null/빈값/공백, attributeValue null 허용
- 동등성

### 일급 컬렉션 (RoomTypeBeds 7개, RoomTypeViews 5개, RoomTypeAttributes 5개, RoomTypes 5개)
- 생성: null/빈 리스트 → 빈 컬렉션
- 불변식: 중복 검증 (bedTypeId/viewTypeId/attributeKey)
- reconstitute: 중복 무검증
- 도메인 로직: totalQuantity, roomTypeIds 추출

---

## Supplier BC (57개 테스트)

### SupplierTest (14개)
- forNew() → ACTIVE, reconstitute()
- 상태 전이: suspend/activate/terminate, 중복 전이 예외
- isActive(), equals/hashCode

### SupplierPropertyTest (11개), SupplierRoomTypeTest (11개)
- forNew() → MAPPED, null supplierPropertyCode/supplierRoomCode
- synced(): lastSyncedAt 갱신, UNMAPPED에서 예외
- unmap(): UNMAPPED 전이, 멱등
- reconstitute(), equals/hashCode

### SupplierSyncLogTest (9개)
- forSuccess/forFailed 생성
- reconstitute: 실패 상태 에러 메시지
- markFailed(), equals/hashCode

### SupplierTaskTest, SupplierTaskStatusTest, SupplierTaskTypeTest, SupplierTaskFailureReasonTest (~12개)
- 생성, 상태 전이, 재시도, Enum displayName

### SupplierNameTest (3개), SupplierErrorCodeTest (4개)

---

## ArchUnit — 아키텍처 규칙 자동 강제 (14개 테스트)

### DomainLayerArchTest

| 규칙 | 검증 내용 |
|------|----------|
| DOM-DEP-001 | Domain 모듈은 Spring/JPA/Jakarta 의존 금지 |
| DOM-PKG-002 | 허용된 패키지 구조만 사용 |
| DOM-AGG-003 | Aggregate는 forNew()/reconstitute() 팩토리만 허용 |
| DOM-VO-004 | VO는 record 타입이어야 함 |
| DOM-ENUM-005 | Enum은 displayName() 필수 |
| DOM-ERR-006 | ErrorCode는 BC별 접두사 필수 |
| DOM-EXC-007 | 예외는 DomainException 상속 |
| DOM-EXC-008 | 예외 클래스명은 Exception으로 끝남 |
| DOM-FLD-009 | 엔티티 필드는 private final |
| DOM-FLD-010 | 엔티티에 setter 금지 |
| DOM-COL-011 | 일급 컬렉션 사용 강제 (List 필드 금지) |
| DOM-COL-012 | 일급 컬렉션에 stream() 메서드 존재 |
| DOM-AGG-013 | 엔티티에 Builder 패턴 금지 |
| DOM-AGG-014 | 엔티티 class에 of() 메서드 금지 (from() 사용) |

---

## 도메인 패턴별 검증 요약

| 패턴 | 검증 위치 | 시나리오 |
|------|----------|---------|
| **diff 패턴** | PropertyCollectionTest | Amenities/Photos/AttributeValues update() — added/removed/retained |
| **번들 패턴** | RoomTypeBed/ViewTest | forPending() → withRoomTypeId() 불변 복사 |
| **상태 전이** | Reservation, Partner, Member, Supplier, Inventory | 모든 허용/불허 조합 전수 검증 |
| **요일별 가격** | RateRuleTest | calculatePrice() 월~일 + null fallback |
| **오버라이드 반영** | RateRuleTest | resolvePrice() — 오버라이드 존재/미존재 |
| **날짜 겹침 검증** | RateRulesTest, RateOverridesTest | 완전 겹침, 부분 겹침, 인접 |
| **일급 컬렉션** | Beds/Views/Attributes/Tasks/Types | 중복 검증, reconstitute 무검증, 도메인 연산 |
| **VO 불변성** | MoneyTest, DateRangeTest | 연산 후 원본 변경 없음 |
| **예외 계층** | 모든 BC ErrorCode/Exception | DomainException 상속, BC별 접두사, ErrorCategory |
