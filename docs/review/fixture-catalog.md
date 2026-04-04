# Fixture 카탈로그

> test-designer가 Fixture를 만들 때마다 이 문서를 갱신한다.
> "어떤 Fixture가 어떤 상태의 객체를 만드는지" 한눈에 볼 수 있어야 한다.

---

## accommodation

| Fixture 클래스 | 메서드 | 반환 객체 상태 | 용도 |
|---------------|--------|-------------|------|
| *(아직 없음)* | | | |

## pricing

| Fixture 클래스 | 메서드 | 반환 객체 상태 | 용도 |
|---------------|--------|-------------|------|
| PricingFixtures | directRatePlan() | DIRECT 소스, 무료취소, 선결제, id=null | 기본 RatePlan 생성 테스트 |
| PricingFixtures | supplierRatePlan() | SUPPLIER 소스, 환불불가, id=null | SUPPLIER RatePlan 테스트 |
| PricingFixtures | reconstitutedRatePlan(id) | DB 복원, id 있음 | 복원/동등성 테스트 |
| PricingFixtures | defaultRateRule() | 4월 한 달, 요일별 가격 설정 | 가격 계산 테스트 |
| PricingFixtures | basePriceOnlyRateRule() | 요일별 가격 null, basePrice만 | 폴백 테스트 |
| PricingFixtures | reconstitutedRateRule(id) | DB 복원 | 복원/동등성 테스트 |
| PricingFixtures | defaultOverride() | 4/5 공휴일 17만원 | 오버라이드 테스트 |
| PricingFixtures | reconstitutedOverride(id) | DB 복원 | 복원/동등성 테스트 |
| PricingFixtures | defaultRate(date, price) | 특정 날짜 Rate 스냅샷 | Rate 생성 테스트 |
| PricingFixtures | reconstitutedRate(id, date, price) | DB 복원 | 복원/동등성 테스트 |
| PricingFixtures | breakfastAddOn() | 유료 조식 23,000원 | AddOn 유료 테스트 |
| PricingFixtures | freeIncludedAddOn() | 무료 포함 수건세트 | AddOn 무료 테스트 |
| PricingFixtures | reconstitutedAddOn(id) | DB 복원 | 복원/동등성 테스트 |
| PricingFixtures | overrideListWithApril5() | 4/5, 4/15 오버라이드 리스트 | resolvePrice 테스트 |

## inventory

| Fixture 클래스 | 메서드 | 반환 객체 상태 | 용도 |
|---------------|--------|-------------|------|
| InventoryFixture | defaultInventory() | 신규, 가용 10개, stopSell=false | 기본 재고 테스트 |
| InventoryFixture | inventoryWithCount(count) | 신규, 지정 수량 | 수량별 테스트 |
| InventoryFixture | inventoryForDate(date) | 신규, 지정 날짜 | 날짜별 테스트 |
| InventoryFixture | exhaustedInventory() | 신규, 가용 0개 | 재고 소진 테스트 |
| InventoryFixture | reconstitutedInventory() | DB 복원, id=1L, 가용 10개, v=1 | 기본 복원 테스트 |
| InventoryFixture | stopSellInventory() | DB 복원, id=2L, stopSell=true | 판매 중지 테스트 |
| InventoryFixture | exhaustedStopSellInventory() | DB 복원, id=3L, 가용 0, stopSell=true | 소진+중지 테스트 |
| InventoryFixture | reconstituted(id, roomTypeId, date, count, stopSell, version) | 지정 파라미터 | 임의 상태 테스트 |

## reservation

| Fixture 클래스 | 메서드 | 반환 객체 상태 | 용도 |
|---------------|--------|-------------|------|
| ReservationFixture | pendingReservation() | PENDING, id=null | 신규 예약 생성 테스트 |
| ReservationFixture | confirmedReservation() | CONFIRMED, id=1L | 확정된 예약 상태 전이 테스트 |
| ReservationFixture | cancelledReservation() | CANCELLED, id=2L, 취소사유+시각 포함 | 취소된 예약 검증 |
| ReservationFixture | completedReservation() | COMPLETED, id=1L | 완료된 예약 검증 |
| ReservationFixture | noShowReservation() | NO_SHOW, id=1L | 노쇼 상태 검증 |
| ReservationFixture | reservationWithStatus(status) | 지정 상태, id=1L | 임의 상태 테스트 |
| ReservationFixture | defaultItems() | 2박 항목 리스트 (4/10, 4/11) | 예약 생성 시 기본 항목 |
| ReservationFixture | singleItem(date, inventoryId) | 1박 항목 | 단일 항목 테스트 |
| ReservationFixture | reconstitutedItem(id, resId, invId, date) | DB 복원 항목 | reconstitute 테스트 |
| ReservationFixture | defaultGuestInfo() | 홍길동, 전화, 이메일 | 기본 투숙객 |
| ReservationFixture | minimalGuestInfo() | 김철수, 전화/이메일 null | 최소 정보 투숙객 |

## partner

| Fixture 클래스 | 메서드 | 반환 객체 상태 | 용도 |
|---------------|--------|-------------|------|
| PartnerFixture | activePartner() | 신규 ACTIVE, id=PartnerId(null) | 기본 파트너 생성 테스트 |
| PartnerFixture | partnerWithStatus(status) | DB 복원, id=1L, 지정 상태 | 상태별 테스트 |
| PartnerFixture | suspendedPartner() | DB 복원, SUSPENDED | 정지 파트너 테스트 |
| PartnerFixture | reconstitutedPartner() | DB 복원, ACTIVE, id=1L | 기본 복원 테스트 |
| PartnerFixture | partnerWithId(id) | DB 복원, ACTIVE, 지정 ID | 동등성 테스트 |
| PartnerFixture | activeOwnerMember() | 신규 ACTIVE, OWNER 역할 | OWNER 멤버 테스트 |
| PartnerFixture | activeStaffMember() | 신규 ACTIVE, STAFF 역할 | STAFF 멤버 테스트 |
| PartnerFixture | memberWithRole(role) | 신규 ACTIVE, 지정 역할 | 역할별 테스트 |
| PartnerFixture | memberWithStatus(status) | DB 복원, id=1L, STAFF, 지정 상태 | 상태별 멤버 테스트 |
| PartnerFixture | suspendedMember() | DB 복원, SUSPENDED | 정지 멤버 테스트 |
| PartnerFixture | reconstitutedMember() | DB 복원, ACTIVE, id=1L | 기본 복원 멤버 테스트 |

## supplier

| Fixture 클래스 | 메서드 | 반환 객체 상태 | 용도 |
|---------------|--------|-------------|------|
| SupplierFixture | activeSupplier() | ACTIVE, id.value()=null | 신규 공급자 생성 테스트 |
| SupplierFixture | supplierWithStatus(status) | 지정 상태, id=1L | 임의 상태 테스트 |
| SupplierFixture | suspendedSupplier() | SUSPENDED, id=1L | 정지 상태 테스트 |
| SupplierFixture | terminatedSupplier() | TERMINATED, id=1L | 해지 상태 테스트 |
| SupplierFixture | reconstitutedSupplier() | ACTIVE, id=1L | DB 복원 테스트 |
| SupplierFixture | mappedProperty() | MAPPED, id.value()=null | 신규 숙소 매핑 테스트 |
| SupplierFixture | reconstitutedProperty(status) | 지정 상태, id=1L | DB 복원 숙소 매핑 |
| SupplierFixture | unmappedProperty() | UNMAPPED, id=1L | 매핑 해제 테스트 |
| SupplierFixture | mappedRoomType() | MAPPED, id.value()=null | 신규 객실 매핑 테스트 |
| SupplierFixture | reconstitutedRoomType(status) | 지정 상태, id=1L | DB 복원 객실 매핑 |
| SupplierFixture | successSyncLog() | SUCCESS, id.value()=null | 성공 동기화 로그 |
| SupplierFixture | failedSyncLog() | FAILED, id.value()=null | 실패 동기화 로그 |
| SupplierFixture | reconstitutedSyncLog(status) | 지정 상태, id=1L | DB 복원 동기화 로그 |

## location

| Fixture 클래스 | 메서드 | 반환 객체 상태 | 용도 |
|---------------|--------|-------------|------|
| LocationFixture | defaultLocation() | Location VO (서울시 중구) | 기본 위치 테스트 |
| LocationFixture | locationWithAddress(address) | Location VO, 지정 주소 | 주소별 테스트 |
| LocationFixture | locationWithCoordinates(lat, lng) | Location VO, 지정 좌표 | 좌표별 테스트 |
| LocationFixture | defaultLandmark() | 신규, 서울역/STATION | 기본 랜드마크 테스트 |
| LocationFixture | landmarkOfType(type) | 신규, 지정 타입 | 타입별 테스트 |
| LocationFixture | reconstitutedLandmark() | DB 복원, id=1L, 서울역 | 기본 복원 테스트 |
| LocationFixture | reconstituted(id, name, type, lat, lng) | 지정 파라미터 | 임의 상태 테스트 |
| LocationFixture | defaultPropertyLandmark() | 신규, 거리 1.5km, 도보 18분 | 기본 매핑 테스트 |
| LocationFixture | propertyLandmarkWithDistance(km, min) | 신규, 지정 거리/도보 | 거리별 테스트 |
| LocationFixture | reconstitutedPropertyLandmark() | DB 복원, id=1L | 기본 복원 테스트 |
| LocationFixture | reconstitutedPropertyLandmark(id, propId, landId, km, min) | 지정 파라미터 | 임의 상태 테스트 |
