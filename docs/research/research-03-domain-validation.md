# 도메인 초안 검증 결과

## 검증 대상
- 도메인 초안: `domain_draft.txt`
- 비교 데이터: 해외 글로벌 OTA (크롤링), 국내 OTA (API 응답)
- 검증 일시: 2026-04-01

---

## 1. 잘 설계된 부분

### 숙소 vs 재고/요금 분리 — 정확함
> 초안: "변경 빈도가 다름. 숙소 정보는 정적, 재고/요금은 매일 변동"

실제 데이터에서도 확인됨:
- 숙소 기본 정보(이름, 위치, 편의시설)는 정적
- 가격, 잔여 객실은 실시간 변동 (검색할 때마다 다른 값)
- 해외 OTA에서 "기간 한정 특가 (최대 48시간)" 같은 시간 제한 프로모션이 존재

### Supplier 도메인의 ACL 패턴 — 실제로 필요함
> 초안: "외부 API 의존성을 격리 (ACL 패턴)"

국내 OTA의 sample.json에서 실제로:
- 3개 서플라이어(EPS, A사, DOTW)가 동일 객실에 대해 서로 다른 가격 제공
- 서플라이어마다 데이터 구조가 다름 (terms URL 유무, 주소 형식 등)
- `supplierCode: "A사"` 같은 공급자 식별이 가격에 직접 매핑

### RatePlan 개념 — 핵심적으로 필요
> 초안: "환불가능/환불불가, 조식포함 등 요금 조건 묶음"

해외 OTA 상세 페이지에서 확인:
- 같은 "스탠다드 더블룸"에 4~5개 요금 행이 존재
- 환불가능 + 조식별도, 환불불가 + 조식포함, 패키지 상품 등 조합
- 각 행마다 가격, 취소정책, 결제방식이 다름

### 예약 상태 흐름 — 적절함
> 초안: "PENDING → CONFIRMED → COMPLETED 또는 CANCELLED"

OTA 공통 패턴과 일치. 다만 세부 상태 추가 검토 필요 (아래 참조).

---

## 2. 보강이 필요한 부분

| 영역 | 현재 초안 | 실제 데이터 | 개선 제안 |
|------|-----------|-------------|-----------|
| **Amenity** | 단일 리스트 ("와이파이, 주차장, 조식, 수영장") | 해외 OTA: 숙소 시설(13개 카테고리) + 객실 시설(25개 카테고리) 명확 분리. 국내 OTA: 67개 flat 배열 | `PropertyAmenity`와 `RoomAmenity`를 분리. type enum으로 카테고리화 |
| **Location** | "주소, 좌표, 지역 분류" | 해외 OTA: 행정구(서대문구) + 관광지역(명동, 홍대) + 중심부 거리(1.3km) + 지하철 연결 + 랜드마크 거리 | `Location`에 neighborhood, distanceFromCenter, nearbyTransport, nearbyLandmarks 추가 |
| **Rate** | "날짜별 객실 가격" | totalPrice = roomPrice + taxAndFees. 할인가/원가 분리. 쿠폰가 별도. 1박가/총액 분리 | `Rate`에 roomPrice, taxAndFees, totalPrice, originalPrice, discountRate 모두 필요 |
| **Image** | "숙소/객실 사진" | 해외 OTA: 37개 이상 갤러리. 국내 OTA: photos 배열(photoType별 분류: ROOM 등) | `Photo`에 photoType (ROOM, EXTERIOR, LOBBY 등) 추가 |
| **Property 유형** | "호텔, 펜션, 모텔 등" | 해외 OTA: 호텔, 아파트, 게스트하우스, 모텔, 호스텔, 빌라, 캡슐호텔, B&B, 홈스테이, 펜션 (10개+) | `PropertyType` enum 확장 필요 |

---

## 3. 초안에 없는데 추가 필요한 개념

| 개념 | 발견 플랫폼 | 중요도 | 도메인 모델 반영 제안 |
|------|-------------|:------:|----------------------|
| **결제 방식 (PaymentPolicy)** | 해외 OTA | **높음** | "선결제 필요 없음 - 숙소에서 결제" vs "체크인 전 결제". RatePlan의 속성으로 추가 |
| **조식 정보 (Breakfast)** | 양쪽 모두 | **높음** | 포함/불포함 + 가격 + 평점. RatePlan에 breakfastIncluded, breakfastPrice, breakfastRating |
| **브랜드 (Brand)** | 해외 OTA | 중간 | 체인 호텔 식별. Property에 brand 속성 추가 |
| **성급 (StarRating)** | 해외 OTA | **높음** | 1~5성급. Property에 starRating 추가. 초안에 누락됨 |
| **평점 체계 (Rating)** | 양쪽 모두 | **높음** | 10점/5점 등 플랫폼마다 다름. 정규화 로직 + 리뷰 수 함께 관리 |
| **접근성 (Accessibility)** | 해외 OTA | 중간 | 숙소 레벨 + 객실 레벨 이중. 장애인 화장실, 엘리베이터 등 |
| **패키지 상품** | 해외 OTA | 중간 | 시티투어+객실, 비즈니스 조식 패키지 등. RatePlan의 packageType |
| **마케팅 뱃지 (Badge)** | 양쪽 모두 | 중간 | "이 숙소 최저가", "기간 한정 특가", "신규 숙소" 등. Promotion 엔티티 |
| **Supplier 법적 정보** | 국내 OTA | **높음** | 사업자등록번호, 대표자명, 주소, 이메일, 이용약관 URL. 법적 의무사항 |
| **쿠폰 가격 분리** | 국내 OTA | 중간 | coupon / nonCoupon 가격 분리. Rate에 couponPrice 추가 |
| **멤버십/등급** | 국내 OTA | 중간 | 회원 등급(NONE, ELITE 등)에 따른 특가/전용 객실 |
| **여행객 유형 정책** | 해외 OTA | 낮음 | 반려동물 허용, 성인전용, LGBTQ+ 친화 등. Property.guestPolicy |
| **정렬 기준** | 해외 OTA | 낮음 | "가장 일치하는 옵션부터" — 검색 도메인의 정렬 로직 |

---

## 4. 개선된 도메인 모델 제안

### Accommodation 도메인 (보강)
```
Property
├── id, name, description
├── propertyType: enum (HOTEL, MOTEL, PENSION, RESORT, GUESTHOUSE, HOSTEL, VILLA, CAPSULE, BNB, HOMESTAY)
├── starRating: 1~5 (nullable)
├── brand: String (nullable) — 체인 호텔
├── location
│   ├── address, latitude, longitude
│   ├── neighborhood — 행정구역 + 관광지역
│   ├── distanceFromCenter — 중심부까지 거리
│   └── nearbyTransport — 지하철 등
├── rating
│   ├── score: BigDecimal — 정규화된 평점
│   ├── reviewCount: Integer
│   └── ratingCategory: String — "매우 좋음" 등
├── amenities[]: PropertyAmenity — 숙소 레벨 편의시설
├── accessibility[]: AccessibilityFeature — 배리어프리
├── policies
│   ├── checkInTime, checkOutTime
│   ├── petPolicy, smokingPolicy
│   └── guestPolicy
├── photos[]: Photo(photoType, url)
└── isSponsored, isNew

RoomType
├── id, name, description
├── areaSqm: BigDecimal — m² (필수)
├── areaPyeong: String — 평 (국내 전용, nullable)
├── maxOccupancy: Integer
├── bedType: String — "더블침대 1개", "싱글베드 2개"
├── viewType: String — "시티뷰", "오션뷰"
├── amenities[]: RoomAmenity — 객실 레벨 편의시설
├── accessibility[]: RoomAccessibility
├── photos[]: Photo
└── ratePlans[]: RatePlan (1:N)
```

### Inventory & Rate 도메인 (대폭 보강)
```
RatePlan
├── id
├── roomTypeId
├── name — "환불가능", "조식포함" 등
├── isFreeCancellation: boolean
├── isNonRefundable: boolean
├── cancellationPolicyText: String
├── paymentPolicy: enum (PREPAY, PAY_AT_PROPERTY, PAY_BEFORE_CHECKIN)
├── breakfastIncluded: boolean
├── breakfastPrice: BigDecimal (nullable)
├── packageType: String (nullable) — 번들 상품 유형
└── benefits[]: String — 혜택 목록

Rate (날짜별 요금)
├── ratePlanId
├── date: LocalDate
├── roomPrice: BigDecimal — 순수 객실가
├── taxAndFees: BigDecimal — 세금 + 수수료
├── totalPrice: BigDecimal — roomPrice + taxAndFees
├── originalPrice: BigDecimal — 할인 전 원가
├── discountRate: Integer (nullable) — 할인율 %
├── couponPrice: BigDecimal (nullable) — 쿠폰 적용가
├── priceBadge: String — "최저가" 등
└── supplierCode: String (nullable) — 공급자 식별

Inventory (날짜별 재고)
├── roomTypeId
├── date: LocalDate
├── totalCount: Integer
├── availableCount: Integer
├── remainingText: String — "이 가격으로 1개 남음"
└── isSoldOut: boolean
```

### Supplier 도메인 (대폭 보강)
```
Supplier
├── supplierType: enum (EPS, A사, DOTW, HOTELBEDS, ...)
├── supplierName, supplierNameKr
├── legalInfo — 법적 필수 정보
│   ├── companyTitle — "Travelscape LLC"
│   ├── ownerName — 대표자명
│   ├── address
│   ├── phoneNo
│   ├── businessNo — 사업자등록번호
│   ├── email
│   └── termsUrl — 이용약관 URL
└── adapterConfig — API 연동 설정
```

### 검색 도메인 (보강)
```
SearchFilter
├── priceRange: (min, max)
├── propertyTypes[]: PropertyType enum
├── starRatings[]: Integer
├── ratingMin: BigDecimal
├── neighborhoods[]: String
├── amenities[]: AmenityType enum — 숙소 + 객실 통합 필터
├── bedTypes[]: String
├── mealOptions[]: enum (BREAKFAST_INCLUDED, SELF_CATERING)
├── bookingPolicy[]: enum (FREE_CANCELLATION, NO_PREPAYMENT)
├── distanceFromCenter: BigDecimal
├── guestPolicy[]: enum (PET_FRIENDLY, ADULT_ONLY)
└── sortBy: enum (RELEVANCE, PRICE_ASC, RATING_DESC, DISTANCE)
```

---

## 5. 우선순위 제안 (선택과 집중)

실제 데이터에서 확인된 중요도에 따라 구현 우선순위를 제안한다:

### 반드시 구현 (Core)
1. **Property + RoomType**: 기본 속성 + 편의시설 2레벨 분리
2. **RatePlan + Rate**: 가격 3단 구조(roomPrice + tax + total) + 취소정책
3. **Inventory**: 날짜별 재고 + 동시성 제어
4. **Reservation**: 예약 생성/취소 + 재고 차감 원자적 처리

### 설계만 (Design Only)
5. **Supplier 통합**: ACL 패턴 + 멀티 서플라이어 가격 비교
6. **Search**: 필터 + 정렬 (읽기 전용 뷰)
7. **Membership/Coupon**: 등급별 특가

### 미구현 (Future)
8. 배리어프리/접근성
9. 패키지 상품
10. 마케팅 뱃지/프로모션

---

## 분석 방법론

이 검증은 AI(Claude Code)를 활용하여 수행했다:
1. 도메인 초안(domain_draft.txt)을 기준으로 각 개념별 실제 데이터 대조
2. 해외/국내 OTA 실제 데이터에서 초안에 없는 필드 식별
3. OTA 도메인 가이드 참조하여 누락된 업계 표준 개념 보충
4. 구현 우선순위를 핵심 흐름 기준으로 제안
