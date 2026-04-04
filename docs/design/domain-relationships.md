# OTA-TOY 도메인 객체 연관관계

> 이 문서는 "이 시스템이 어떤 개념들로 이루어져 있고, 서로 어떻게 연결되는지"를 비즈니스 관점에서 설명한다.
> 코드 변경 시 이 문서도 함께 갱신한다.

---

## 전체 구조 한눈에 보기

```mermaid
graph TD
    subgraph 숙소 관리
        property["숙소<br/>(호텔·모텔·펜션)"]
        roomtype["객실<br/>(스탠다드·디럭스·스위트)"]
        propertytype["숙소 유형 분류<br/>(호텔/모텔/리조트 등)"]
        roomattribute["침대·전망 유형<br/>(싱글/더블/오션뷰 등)"]
        brand["브랜드<br/>(신라·롯데·힐튼)"]
    end

    subgraph 가격·재고
        pricing["요금 정책<br/>(가격 규칙·취소 정책)"]
        inventory["재고<br/>(날짜별 객실 수량)"]
    end

    subgraph 예약
        reservation["예약<br/>(투숙객·기간·결제)"]
    end

    subgraph 운영
        partner["파트너<br/>(숙소 운영 사업자)"]
        supplier["외부 공급자<br/>(야놀자·부킹닷컴 등)"]
        location["랜드마크<br/>(서울역·인천공항)"]
    end

    partner -->|"누가 등록했는지"| property
    brand -->|"어떤 브랜드인지"| property
    propertytype -->|"어떤 유형인지"| property
    property -->|"어떤 숙소의 객실인지"| roomtype
    roomattribute -->|"어떤 침대/전망인지"| roomtype
    roomtype -->|"어떤 객실의 요금인지"| pricing
    roomtype -->|"어떤 객실의 재고인지"| inventory
    pricing -->|"어떤 요금으로 예약했는지"| reservation
    inventory -->|"어떤 재고를 차감했는지"| reservation
    supplier -->|"어떤 공급자의 요금인지"| pricing
    supplier -->|"어떤 숙소를 공급하는지"| property
    supplier -->|"어떤 객실을 공급하는지"| roomtype
    location -->|"숙소 근처 랜드마크"| property
```

---

## 숙소 (Property)

**"호텔, 모텔, 펜션, 리조트 같은 숙박 시설 하나"**

파트너(사업자)가 등록한 숙소의 기본 정보를 관리한다. 야놀자나 부킹닷컴에서 검색하면 나오는 숙소 한 건이 이것.

| 필드 | 설명 | 예시 |
|------|------|------|
| 숙소명 (PropertyName) | 숙소 이름 | "서울 신라호텔" |
| 설명 (PropertyDescription) | 숙소 소개글 | "도심 속 최고급 호텔..." |
| 위치 (Location) | 주소 + 좌표 + 동네/지역 | "서울시 중구 / 37.5, 127.0 / 명동 / 서울" |
| 프로모션 (PromotionText) | 프로모션 문구 | "얼리버드 30% 할인" |
| 상태 (PropertyStatus) | 운영 상태 | ACTIVE(운영중), INACTIVE(비활성) |

연결된 것들:
- **이 숙소는 어떤 브랜드인지** → `BrandId`로 브랜드 참조 (예: 신라, 롯데)
- **누가 등록했는지** → `PartnerId`로 파트너 참조
- **어떤 유형인지** → `PropertyTypeId`로 숙소 유형 참조 (호텔/모텔/펜션)

### 숙소에 딸린 것들 (각각 독립 관리)

**편의시설 (PropertyAmenity)** — 수영장, 피트니스, 무료 와이파이 등
- 편의시설 종류(AmenityType), 이름(AmenityName), 추가 요금(Money), 정렬 순서
- 래핑 객체 `PropertyAmenities`가 정렬순서 중복을 검증

**사진 (PropertyPhoto)** — 외관, 로비, 전경 사진 등
- 사진 유형(PhotoType), 원본 URL(OriginUrl), CDN URL(CdnUrl), 정렬 순서
- 래핑 객체 `PropertyPhotos`가 정렬순서 중복을 검증

**속성값 (PropertyAttributeValue)** — EAV 패턴으로 유형별 추가 속성
- 예: 호텔이면 "성급=5", 펜션이면 "바베큐=가능"
- 래핑 객체 `PropertyAttributeValues`가 속성 ID 중복을 검증

> OTA 리서치 결과: 실제 부킹닷컴, 야놀자에서 숙소 기본정보/사진/편의시설은 **각각 별도 탭**에서 독립적으로 관리된다. 그래서 Aggregate에 컬렉션을 포함하지 않고 ID 참조로 분리했다.

---

## 객실 유형 (RoomType)

**"스탠다드 더블, 디럭스 트윈 같은 객실 종류"**

하나의 숙소 안에서 구분되는 객실 카테고리. 같은 호텔이라도 스탠다드/디럭스/스위트가 다르다.

| 필드 | 설명 | 예시 |
|------|------|------|
| 객실명 (RoomTypeName) | 객실 이름 | "디럭스 더블" |
| 설명 (RoomTypeDescription) | 객실 소개 | "시티뷰가 보이는 넓은 객실" |
| 면적 (areaSqm / areaPyeong) | 객실 크기 | 33.5m² / 10평 |
| 기본 인원 (baseOccupancy) | 기준 투숙 인원 | 2명 |
| 최대 인원 (maxOccupancy) | 최대 투숙 인원 | 4명 |
| 기본 재고 (baseInventory) | 해당 객실 총 수량 | 10개 |
| 체크인/아웃 시간 | 입퇴실 시간 | 15:00 / 11:00 |
| 상태 (RoomTypeStatus) | 운영 상태 | ACTIVE, INACTIVE |

연결된 것들:
- **어떤 숙소의 객실인지** → `PropertyId`로 숙소 참조

### 객실에 딸린 것들 (각각 독립 관리)

**침대 구성 (RoomTypeBed)** — 이 객실에 어떤 침대가 몇 개 있는지
- 침대 유형(`BedTypeId` → 싱글, 더블, 킹 등), 수량
- 래핑 객체 `RoomTypeBeds`가 침대유형 중복 검증 + `totalQuantity()` 제공

**전망 (RoomTypeView)** — 이 객실에서 볼 수 있는 전망
- 전망 유형(`ViewTypeId` → 시티뷰, 오션뷰, 마운틴뷰 등)
- 래핑 객체 `RoomTypeViews`가 전망 중복 검증

**편의시설 (RoomAmenity)** — 객실 내 미니바, 에어컨, TV 등
**사진 (RoomPhoto)** — 객실 내부 사진
**추가 속성 (RoomTypeAttribute)** — 키-값 형태의 추가 정보

---

## 숙소 유형 (PropertyType)

**"호텔, 모텔, 펜션, 리조트 같은 분류 체계"**

숙소를 카테고리로 분류하는 마스터 데이터. 각 유형별로 필요한 속성이 다르다.

| 필드 | 설명 | 예시 |
|------|------|------|
| 코드 (PropertyTypeCode) | 유형 코드 | HOTEL, MOTEL, PENSION, RESORT |
| 이름 (PropertyTypeName) | 유형명 | "호텔" |
| 설명 (PropertyTypeDescription) | 유형 설명 | "정식 호텔업 등록 숙박시설" |

### 유형별 속성 정의 (PropertyTypeAttribute)
- 예: 호텔이면 "성급"이라는 속성이 필요, 펜션이면 "바베큐 가능 여부"가 필요
- 이 정의에 따라 숙소가 `PropertyAttributeValue`로 실제 값을 채운다

---

## 침대·전망 유형 (BedType, ViewType)

**"싱글베드, 더블베드, 킹베드 / 시티뷰, 오션뷰, 마운틴뷰 같은 참조 마스터"**

시스템 전체에서 공유하는 참조 데이터.

| 클래스 | 필드 | 예시 |
|--------|------|------|
| BedType | 코드 + 이름 | SINGLE/"싱글베드", DOUBLE/"더블베드", KING/"킹베드" |
| ViewType | 코드 + 이름 | CITY/"시티뷰", OCEAN/"오션뷰", MOUNTAIN/"마운틴뷰" |

---

## 브랜드 (Brand)

**"신라, 롯데, 힐튼 같은 숙박 브랜드"**

여러 숙소가 하나의 브랜드에 속할 수 있다. 독립적으로 관리된다.

| 필드 | 설명 | 예시 |
|------|------|------|
| 영문명 (BrandName) | 브랜드 영문 이름 | "Shilla" |
| 한글명 (BrandNameKr) | 브랜드 한글 이름 | "신라" |
| 로고 (LogoUrl) | 로고 이미지 URL | "https://cdn.example.com/shilla-logo.png" |

---

## 요금 정책 (RatePlan)

**"이 객실을 얼마에, 어떤 조건으로 판매할 것인가"**

동일한 객실이라도 요금 조건(환불 가능/불가, 조식 포함/미포함)에 따라 가격이 달라진다.

| 필드 | 설명 | 예시 |
|------|------|------|
| 이름 (RatePlanName) | 요금 정책명 | "스탠다드 환불가능" |
| 출처 (SourceType) | 직접 입점 or 외부 공급 | DIRECT, SUPPLIER |
| 결제 방식 (PaymentPolicy) | 결제 시점 | 선결제, 현장결제 |
| 취소 정책 (CancellationPolicy) | 무료취소/환불불가/기한 | "체크인 3일 전까지 무료 취소" |

연결된 것들:
- **어떤 객실의 요금인지** → `RoomTypeId`로 객실 참조
- **외부 공급이면** → `SupplierId`로 공급자 참조

### 요금 하위 구조

```
요금 정책 (RatePlan)
├── 요금 규칙 (RateRule) — "4/1~4/30 기간, 평일 10만원, 금요일 12만원, 토요일 15만원"
│   └── 요금 오버라이드 (RateOverride) — "4/5 공휴일은 17만원으로 덮어쓰기"
├── 일별 확정 요금 (Rate) — "4/10 = 100,000원" (계산 결과 스냅샷)
└── 부가 서비스 (RatePlanAddOn) — "조식 23,000원", "스파 무료 포함"
```

**요금 계산 흐름**: RateRule의 요일별 가격 → RateOverride가 있으면 덮어쓰기 → 최종 가격 결정

---

## 재고 (Inventory)

**"4월 10일에 디럭스 더블이 몇 개 남았는지"**

날짜별, 객실 유형별 가용 수량을 관리한다.

| 필드 | 설명 | 예시 |
|------|------|------|
| 날짜 (inventoryDate) | 해당 날짜 | 2026-04-10 |
| 가용 수량 (availableCount) | 남은 객실 수 | 3개 |
| 판매 중지 (stopSell) | 수동 판매 중지 여부 | false |
| 버전 (version) | 낙관적 잠금용 | 동시 수정 방지 |

연결된 것들:
- **어떤 객실의 재고인지** → `RoomTypeId`로 객실 참조

주요 동작: `decrease()`로 차감, `restore()`로 복구, `stopSell()`로 판매 중지

---

## 예약 (Reservation)

**"고객이 숙소를 예약한 건 하나"**

| 필드 | 설명 | 예시 |
|------|------|------|
| 예약번호 (ReservationNo) | 고유 예약 번호 | "RSV-20260410-001" |
| 투숙객 (GuestInfo) | 이름, 연락처, 이메일 | "홍길동 / 010-1234-5678" |
| 숙박 기간 (DateRange) | 체크인~체크아웃 | 4/10 ~ 4/12 (2박) |
| 인원수 (guestCount) | 투숙 인원 | 2명 |
| 총 금액 (Money) | 결제 총액 | 300,000원 |
| 상태 (ReservationStatus) | 예약 상태 | 대기 → 확정 → 완료 |

연결된 것들:
- **어떤 요금 정책으로 예약했는지** → `RatePlanId`로 요금 참조

### 예약 항목 (ReservationItem)

예약 1건에 숙박 날짜만큼 항목이 생긴다. 2박이면 2개.

```
예약 (2박)
├── 4/10 항목 → 재고 #100 차감
└── 4/11 항목 → 재고 #101 차감
```

### 예약 상태 흐름

```
대기(PENDING) → 확정(CONFIRMED) → 완료(COMPLETED)
     ↓               ↓                  
   취소(CANCELLED)  취소(CANCELLED) / 노쇼(NO_SHOW)
```

---

## 파트너 (Partner)

**"숙소를 등록하고 관리하는 사업자"**

| 필드 | 설명 | 예시 |
|------|------|------|
| 파트너명 (PartnerName) | 사업자명 | "(주)서울호텔" |
| 상태 (PartnerStatus) | 운영 상태 | ACTIVE(활성) ↔ SUSPENDED(정지) |

### 파트너 멤버 (PartnerMember)

파트너 소속 직원. 역할(OWNER/MANAGER/STAFF)에 따라 권한이 다르다.

| 필드 | 설명 |
|------|------|
| 이름 (MemberName) | 직원 이름 |
| 이메일 (Email) | 로그인 이메일 |
| 전화번호 (PhoneNumber) | 연락처 |
| 역할 (PartnerMemberRole) | OWNER, MANAGER, STAFF |

---

## 외부 공급자 (Supplier)

**"야놀자, 부킹닷컴 같은 외부 채널에서 숙소/객실/요금을 공급하는 사업자"**

| 필드 | 설명 | 예시 |
|------|------|------|
| 영문명 (SupplierName) | 공급자 이름 | "Booking.com" |
| 한글명 (SupplierNameKr) | 한글 이름 | "부킹닷컴" |
| 회사명 (CompanyTitle) | 법인명 | "Booking Holdings" |
| 사업자번호 (BusinessNo) | 사업자등록번호 | "123-45-67890" |
| 상태 (SupplierStatus) | ACTIVE → SUSPENDED → TERMINATED |

### 공급자-숙소 매핑 (SupplierProperty)

공급자가 제공하는 숙소와 우리 숙소를 연결한다.
- 공급자 코드(supplierPropertyCode) — 공급자 측 숙소 ID
- 매핑 상태(SupplierMappingStatus) — MAPPED / UNMAPPED
- 마지막 동기화 시각(lastSyncedAt)

### 공급자-객실 매핑 (SupplierRoomType)

공급자가 제공하는 객실과 우리 객실을 연결한다. 구조는 SupplierProperty와 동일.

### 동기화 로그 (SupplierSyncLog)

공급자 데이터 동기화 이력. 성공/실패 여부, 처리 건수를 기록한다.

---

## 랜드마크 (Landmark)

**"서울역, 인천공항, 명동 같은 주요 지점"**

숙소 근처의 랜드마크 정보. 숙소까지의 거리/도보 시간을 제공한다.

| 필드 | 설명 | 예시 |
|------|------|------|
| 이름 (LandmarkName) | 랜드마크 이름 | "서울역" |
| 유형 (LandmarkType) | 역/관광지/공항/쇼핑/공원 | STATION |
| 좌표 (Coordinate) | 위도/경도 | 37.55 / 126.97 |

### 숙소-랜드마크 연결 (PropertyLandmark)

| 필드 | 설명 | 예시 |
|------|------|------|
| 거리 (distanceKm) | 숙소까지 거리 | 1.5km |
| 도보 시간 (walkingMinutes) | 걸어서 몇 분 | 18분 |

---

## 공유 타입

### accommodation 패키지 — 숙소/객실이 함께 쓰는 것

| 타입 | 용도 | 사용처 |
|------|------|--------|
| AmenityType | 편의시설 종류 (POOL, WIFI, GYM 등) | 숙소 편의시설, 객실 편의시설 |
| AmenityName | 편의시설 이름 | 숙소 편의시설, 객실 편의시설 |
| PhotoType | 사진 종류 (EXTERIOR, INTERIOR, ROOM 등) | 숙소 사진, 객실 사진 |

### common 패키지 — 전체 도메인이 함께 쓰는 것

| 타입 | 용도 | 사용처 |
|------|------|--------|
| Money | 금액 (BigDecimal 래핑) | 편의시설 추가요금, 예약 총액 |
| Coordinate | 위도/경도 좌표 | 숙소 위치, 랜드마크 위치 |
| DateRange | 날짜 범위 | 예약 숙박 기간 |
| Email | 이메일 주소 | 파트너 멤버, 공급자 |
| PhoneNumber | 전화번호 | 파트너 멤버, 공급자 |
| OriginUrl / CdnUrl | 이미지 URL | 숙소 사진, 객실 사진 |

---

## BC 간 ID 참조 요약

"누가 누구를 알고 있는가"

```
파트너 ──PartnerId──→ 숙소
브랜드 ──BrandId──→ 숙소
숙소유형 ──PropertyTypeId──→ 숙소
숙소 ──PropertyId──→ 객실, 공급자 매핑, 랜드마크 연결
침대/전망 ──BedTypeId, ViewTypeId──→ 객실
객실 ──RoomTypeId──→ 요금 정책, 재고, 공급자 매핑
공급자 ──SupplierId──→ 요금 정책
요금 정책 ──RatePlanId──→ 예약
재고 ──InventoryId──→ 예약 항목
```

> 모든 BC 간 참조는 ID VO를 통해 이루어지며, 객체를 직접 참조하지 않는다.
> 유일한 예외: PropertyLandmark는 `long propertyId`로 숙소를 참조한다 (location BC의 property BC 컴파일 의존 제거 목적).
