# OTA 플랫폼 데이터 구조 분석

## 조사 개요
- 조사 일시: 2026-04-01
- 검색 조건: 서울, 2026-04-02~04-03 (1박), 성인 2명, 객실 1개
- 분석 방법: Playwright 기반 크롤링 + API 응답 분석
- 분석 대상: 해외 글로벌 OTA 2개(검색결과 + 상세페이지), 국내 OTA 1개(검색결과 + API 응답)

---

## 1. 해외 글로벌 OTA 분석

### 1-1. 검색 결과 카드 구조

| 필드 | 노출 여부 | 표시 방식 | 도메인 의미 |
|------|:---------:|-----------|-------------|
| 숙소명 | O | heading (h3) | Property.name |
| 숙소 유형/성급 | O | 별 아이콘 (1~5성급) | Property.starRating |
| 대표 이미지 | O | 1장 썸네일 | Property.mainImage |
| 위치 | O | "서대문구, 서울 · 지도에서 표시" | Property.location |
| 중심부까지 거리 | O | "중심부에서 1.3km" | Property.distanceFromCenter |
| 지하철 연결 | O | "지하철 연결" (일부 숙소) | Property.nearbyTransport |
| 평점 (10점 만점) | O | "8.2" 숫자 + "매우 좋음" 텍스트 | Property.rating |
| 리뷰 수 | O | "1,859개 이용 후기" | Property.reviewCount |
| 객실 유형명 | O | "스탠다드 더블룸 - 도시 전망" | RoomType.name |
| 침대 유형 | O | "더블침대 1개" | RoomType.bedType |
| 가격 (원가) | O | 취소선 "₩792,000" | Rate.originalPrice |
| 가격 (할인가) | O | 강조 "₩356,400" | Rate.discountedPrice |
| 세금 포함 여부 | O | "세금 및 기타 요금 포함" | Rate.taxIncluded |
| 할인율 | O | "55% OFF" 뱃지 | Rate.discountRate |
| 잔여 객실 | O | "이 요금으로 남은 옵션 3개" | Inventory.remainingCount |
| 결제 방식 | O | "선결제 필요 없음 – 숙소에서 결제" | Rate.paymentPolicy |
| 프로모션 태그 | O | "기간 한정 특가" | Promotion.tag |
| 광고 여부 | O | "광고" 라벨 | Property.isSponsored |
| 신규 숙소 | O | "해외 OTA(B사) 신규 숙소" (일부) | Property.isNew |

### 1-2. 검색 필터 구조 (플랫폼이 중요시하는 속성)

| 필터 카테고리 | 세부 옵션 | 도메인 의미 |
|---------------|-----------|-------------|
| **예산** | 슬라이더 ₩30,000~₩700,000+ | Rate.priceRange |
| **인기 필터** | 호텔, 매우좋음 8이상, 5성급, 트윈침대, 조식포함, 전용욕실, 아파트, 게스트하우스 | 복합 필터 |
| **식사** | 직접 취사 가능, 조식 포함 | RoomType.mealOption |
| **침대 구성** | 트윈침대, 더블침대 | RoomType.bedType |
| **숙소 유형** | 호텔(305), 단독홈/아파트(99), 아파트(82), 게스트하우스(56), 모텔(41), 호스텔(39), 빌라(11), 캡슐호텔(5), 비앤비(3), 홈스테이(3), 펜션(1) | Property.propertyType |
| **침실/욕실 수** | 수량 선택 (0~) | RoomType.bedroomCount, bathroomCount |
| **시설** | 주차장, 레스토랑, 룸서비스, 24시간 프런트, 피트니스 (13개) | Property.amenities |
| **객실 시설** | 전용욕실, 발코니, 주방, 에어컨, 바다전망 (25개) | RoomType.amenities |
| **숙소 등급** | 1~5성급 | Property.starRating |
| **후기 평점** | 최고 9+, 매우좋음 8+, 좋음 7+, 만족 6+ | Property.ratingCategory |
| **동네** | 중구, 마포구, 종로구, 강남구, 명동, 홍대 등 (25개) | Property.neighborhood |
| **예약 정책** | 무료취소, 신용카드 없이 예약, 선결제 불필요 | Rate.bookingPolicy |
| **장소** | 랜드마크/공항 기준 거리 | Property.nearbyLandmarks |
| **온라인 결제** | 온라인 결제 가능 | Rate.onlinePayment |
| **여행객 유형** | 반려동물 허용, 성인전용, LGBTQ+ 친화 | Property.guestPolicy |
| **중심까지 거리** | 1km, 3km, 5km 미만 | Property.distanceFromCenter |
| **액티비티** | 피트니스, 수영장, 사우나, 스파 | Property.activities |
| **호평 받은 점** | 조식 (평가: 매우 좋음) | Property.highlightedReviews |
| **브랜드** | 신라스테이, 스카이파크, 롯데시티, 노보텔 등 (20개) | Property.brand |
| **인증** | 지속가능성 인증 | Property.certifications |
| **배리어프리** | 장애인 화장실, 키높은 변기, 점자 등 (7개) | Property.accessibility |
| **장애인 편의(객실)** | 지상층, 엘리베이터, 휠체어, 손잡이 등 (11개) | RoomType.accessibility |

### 1-3. 숙소 상세 페이지 구조

#### Property 레벨
- 숙소명 + 성급 (별 아이콘)
- 위치: 주소 + "지도에서 표시" + "중심부에서 Xkm"
- 평점: 10점 만점, "매우 좋음" 텍스트, 리뷰 수
- 실제 투숙객 리뷰 인용문 (장점 하이라이트)
- 숙소 매력 포인트: "1박 투숙에 적합!", "단골 고객이 많은 숙소"
- 인기 시설 및 서비스 (아이콘 리스트)
- 조식 정보 (별도 섹션)

#### Room 레벨 (테이블 구조)
| 필드 | 값 예시 | 비고 |
|------|---------|------|
| 객실 유형명 | 스탠다드 더블룸 - 도시 전망 | 뷰 타입이 이름에 포함 |
| 잔여 객실 | "남은 객실 3개" | 긴급성 표시 |
| 층수 정보 | "고층 객실" | 일부 객실만 |
| 침대 유형 | 더블침대 1개 | |
| 면적 | 21 제곱미터 | m² 단위 |
| 뷰 타입 | 도시 전망 | |
| 객실 편의시설 | 에어컨, 전용욕실, 평면TV, 방음, WiFi, 침대옆콘센트, 난방, 긴침대 등 | 아이콘+텍스트 |

#### Rate/Price 레벨
| 필드 | 값 예시 | 비고 |
|------|---------|------|
| 최대 투숙 인원 | 2명 | 아이콘 표시 |
| 기존 요금 (원가) | ₩792,000 | 취소선 |
| 현재 요금 (할인가) | ₩356,400 | 강조 표시 |
| 세금 포함 | "세금 및 기타 요금 포함" | |
| 할인율 | 55% OFF | 뱃지 |
| 프로모션 | "기간 한정 특가" (최대 48시간) | |

#### Cancellation/Policy 레벨
| 필드 | 값 예시 | 비고 |
|------|---------|------|
| 취소 정책 | "취소 시 요금 전액 지불" 또는 "환불 불가" | 요금 플랜마다 다름 |
| 결제 방식 | "선결제 필요 없음 - 숙소에서 결제" 또는 "체크인 전 숙소 측에서 결제" | |
| 식사 옵션 | "조식 ₩23,000 (평가: 좋음)" 또는 "조식 포함" | 요금 플랜마다 다름 |

#### 패키지/번들 상품
- "[서울시티투어버스] 스탠다드 더블룸 - 1박당 티켓 2매" 같은 패키지 상품이 별도 행으로 표시
- "[비즈니스 1인 조식 패키지]" 같은 B2B 패키지도 존재

### 1-4. 인사이트

1. **가격 구조가 매우 복잡**: 같은 객실에 대해 4~5가지 요금 플랜이 존재 (조식 유무 × 환불 가능 여부 × 패키지)
2. **필터가 곧 도메인**: 검색 필터 22개 카테고리가 그대로 도메인 모델의 속성이 됨
3. **평점이 10점 만점**: 5점 만점과 다른 체계 → 통합 시 정규화 필요
4. **"숙소 시설"과 "객실 시설"이 명확히 분리**: 도메인 모델에서도 Property.amenities와 RoomType.amenities를 분리해야 함
5. **배리어프리/접근성이 별도 카테고리**: 숙소 레벨 + 객실 레벨 이중 구조
6. **브랜드 필터 존재**: Property에 brand 속성 필요
7. **동네(neighborhood) 필터**: 행정구역 + 관광지역 혼합 (중구, 명동, 홍대)

---

## 2. 국내 OTA 분석 (API 응답 기반)

### 2-1. API 응답 최상위 구조

```
{
  code: 200,
  data: {
    propertyId: 189042,
    supplierPropertyId: 25010158,
    supplierType: "EPS",
    rooms: { main: {...}, others: [...], hasCoupon: false },
    quickFilters: [...],
    suppliers: { EPS: {...}, A사: {...}, DOTW: {...} },
    categoryEventBanner: [...],
    isLowPriceGuaranteed: false,
    membership: { grade: "NONE", hasEliteRooms: false }
  }
}
```

### 2-2. 객실 데이터 구조 (rooms.others[])

#### Room Meta (객실 메타 정보)
| 필드 | 값 예시 | 해외 OTA 대응 |
|------|---------|---------------|
| name | "TWIN" | RoomType code |
| nameKr | "트윈 룸" | RoomType.name |
| mainPhotoPath | URL | RoomType.mainImage |
| maxPersonnel | "성인 2명" | RoomType.maxOccupancy |
| pyeong | "7평" | *국내 전용* |
| squareMeter | "25.0m²" | RoomType.area |
| bedType | null 또는 "싱글베드 2개" | RoomType.bedType |
| viewType | "시티뷰" | RoomType.viewType |
| facilities | 67개 항목 배열 | RoomType.amenities |
| photos | 6개 항목 배열 | RoomType.photos |

#### Room Item (요금 플랜 정보)
| 필드 | 값 예시 | 해외 OTA 대응 |
|------|---------|---------------|
| itemTagName | "이 숙소 최저가" | Promotion.badge |
| itemTagType | "TOP_CHEAPEST" | Promotion.type |
| roomId | 3090803 | RoomType.id |
| ratePlanId | "0" | RatePlan.id |
| cancellation.isFreeCancellation | false | RatePlan.isFreeCancellation |
| cancellationPolicy.text | (상세 텍스트) | RatePlan.cancellationPolicy |
| isNonRefundable | false | RatePlan.isNonRefundable |
| freeBreakfast.isFreeBreakfast | false | RatePlan.breakfastIncluded |
| freeBreakfast.content | "조식 불포함" | RatePlan.breakfastDescription |
| benefits | 4개 항목 | RatePlan.benefits |
| informations | 4개 항목 | RatePlan.additionalInfo |
| promotions | 배열 | RatePlan.promotions |
| notices | 1개 항목 | RatePlan.notices |
| bedType | "싱글베드 2개" | *Item 레벨에도 존재* |
| viewType | "시티뷰" | *Item 레벨에도 존재* |

#### Price (가격 구조)
| 필드 | 값 예시 | 해외 OTA 대응 |
|------|---------|---------------|
| prices.nonCoupon.totalPrice | 62,235 | Rate.totalPrice |
| prices.nonCoupon.dailyPrice | 62,235 | Rate.dailyPrice |
| prices.nonCoupon.roomPrice | 54,881 | Rate.roomPrice |
| prices.nonCoupon.taxAndFees | 7,354 | Rate.taxAndFees |
| prices.nonCoupon.supplierCode | "A사" | Rate.supplier |
| prices.nonCoupon.discountRate | null | Rate.discountRate |
| prices.coupon | null | Rate.couponPrice |
| originalPrice | 62,235 | Rate.originalPrice |
| dailyOriginalPrice | 62,235 | Rate.dailyOriginalPrice |
| priceBadge | "none" | Rate.priceBadge |
| remainingRoomsText | "이 가격으로 1개 남음" | Inventory.remainingText |

### 2-3. Supplier (공급자) 구조

| 필드 | EPS (익스피디아) | A사 (아고다) | DOTW (웹베즈) |
|------|------------------|----------------|---------------|
| supplierType | EPS | A사 | DOTW |
| supplierNameKr | 익스피디아 | 아고다 | 웹베즈 |
| title | Travelscape LLC | 해외 OTA(A사) Company Pte. Ltd. | WebBeds FZ-LLC |
| ownerName | Ariane Gorin | Omri Morgenshtern | Amr Ahmed Ezzeldin A |
| address | Springfield, MO, US | Singapore | - |
| phoneNo | +1-866-257-8493 | 18666568207 | 971-4-4400600 |
| bussinessNo | NV20051441087 | 200506877R | 91277 |
| email | support@chat.travelnow.com | cs_partners@해외 OTA(A사).com | kor-customerservice@webbeds.com |
| terms | URL | null | null |

### 2-4. 기타 구조

- **quickFilters**: `[{ type: "FREE_CANCELLATION", typeName: "무료취소" }]`
- **categoryEventBanner**: 프로모션 배너 3개 (제목, 이미지, URL)
- **membership**: 등급(NONE), 엘리트 객실 여부
- **isLowPriceGuaranteed**: 최저가 보장 여부

### 2-5. 인사이트

1. **멀티 서플라이어 가격 비교**: 같은 객실에 대해 EPS, A사, DOTW 세 공급자의 가격을 비교하여 최저가 제공
2. **가격 3단 분리**: totalPrice = roomPrice + taxAndFees → 해외 OTA처럼 세금 분리 구조
3. **쿠폰/비쿠폰 가격 분리**: nonCoupon / coupon으로 가격이 분리됨
4. **편의시설 67개**: 매우 상세한 편의시설 목록 (전기주전자, 거울, 청소용품까지)
5. **pyeong(평) 단위**: 국내 전용 면적 단위가 별도 존재
6. **bedType/viewType 중복**: Room Meta와 Item 양쪽에 침대/뷰 타입이 존재 → 같은 RoomType에서도 Item(요금 플랜)마다 다를 수 있음
7. **서플라이어 법적 정보**: 사업자등록번호, 대표자명, 이용약관 URL까지 포함 → 법적 요구사항

---

---

## 3. 해외 OTA B (아시아 특화) 분석

### 3-1. 검색 필터 구조

| 필터 카테고리 | 세부 옵션 | 해외 OTA A와 차이 |
|---------------|-----------|-------------------|
| **1박당 요금** | 슬라이더 ₩0~₩12,225,590 | 범위가 훨씬 넓음 |
| **인기 검색 조건** | 지금 바로 결제, 예약 무료 취소, 투숙객 평점 8+, 인터넷, 주차장, 난방, TV, 에어컨 | **"지금 바로 결제"가 최상단** — 결제 방식 강조 |
| **숙소 종류** | 호텔(1143), 모텔(769), 아파트(175), 게스트하우스/비앤비(116), 호스텔(71), 서비스아파트(29), 펜션(21), 캡슐호텔(5), 홈스테이(5), 리조트빌라(1), 리조트(1), 프라이빗하우스전체(134) | **모텔(769)이 2위** — 한국 시장 특성 반영. "서비스 아파트", "프라이빗 하우스 전체" 별도 분류 |
| **지역** | 강남, Myeong-dong, 홍대, 동대문, 강북, 구로, 종로 등 + **지역별 위치 평점** + 명소 | **지역별 위치 평점 (10점)과 특징 태그(쇼핑, 나이트라이프, 자연)** 표시 — 고유 |
| **결제 관련 옵션** | 예약 무료 취소(35), 숙소에서 요금 결제(28), 지금 바로 결제(1037) | **별도 카테고리로 독립** — 결제 옵션 매우 중시 |
| **숙소 성급** | 1~5성급 + **해외 OTA(A사) Luxe (NEW)** | **자체 등급 시스템** "해외 OTA(A사) Luxe" 추가 |
| **이용 가능 서비스** | 조식포함(178), 외부배달음식허용(53), 가족배달허용(7), 편의점배달(6), 에스프레소머신(5), 얼리체크인(3), 레이트체크아웃(3), 무료스낵(2), 공항교통편(2), 사우나무료(2) | **"외부 배달 음식 허용"** — 국내 OTA A에도 없는 고유 필터. 얼리체크인/레이트체크아웃도 별도 |
| **숙소 편의시설** | 수영장, 인터넷, 주차장, 공항교통편 | 해외 OTA A와 유사하지만 항목 적음 |
| **객실 편의시설** | 난방, TV, 에어컨, 냉장고, 커피/티메이커, 세탁기, 인터넷, 욕조, 다림질도구, 발코니, 주방, 전용수영장 | **세탁기, 냉장고** 등 생활형 편의시설 포함 |
| **투숙객 평가 점수** | 슬라이더 (점수 범위) | 해외 OTA A의 카테고리형과 다름 |
| **도심까지 거리** | 도심위치, 2km미만, 2~5km, 5~10km, 10km이상 | 5단계 분류 |
| **침대 종류** | 더블, 싱글/트윈, 퀸, 킹, **벙크베드** | **벙크베드** — 호스텔 대응 |
| **가족 여행객** | 아동 무료 투숙 가능 | 가족 특화 필터 |
| **여행 테마** | (별도 카테고리) | 해외 OTA A에 없음 |
| **주변 인기 명소** | 명동거리(222), 홍대거리(213), 남대문시장(173), 인사동(137), 동대문(125), 북촌한옥마을(108), 창덕궁(102), 경복궁(65), N서울타워(54) | **관광 명소 기반 검색** — 해외 OTA A의 "장소" 필터보다 훨씬 구체적 |
| **침실 수** | 스튜디오/1개, 2개, 3+개 | |
| **호텔 브랜드** | 아코르(18), 메리어트(22), 롯데(9), 스카이파크(6), IHG(4), 신라스테이(8) 등 | |

### 3-2. 검색 결과 카드 구조

| 필드 | 노출 여부 | 표시 방식 | 해외 OTA A와 차이 |
|------|:---------:|-----------|-------------------|
| 숙소명 | O | link 텍스트 | 동일 |
| 성급 | O | "4성급" 버튼 + 별 아이콘 | + tooltip "성급은 편의시설, 투숙객 평점, 객실 크기 등 종합" |
| 이미지 | O | **10장 캐러셀** | 해외 OTA A는 1장 썸네일 → 훨씬 많은 이미지 |
| 위치 | O | "강남, 서울 - 도심까지 7.6km" | 동일 구조 |
| **근처 역/명소 거리** | O | "Seolleung Station 약 555m • 롯데월드 약 5.0km" | **역 + 명소 거리를 동시 표시** — 고유 |
| 평점 (10점) | O | "7.4" + "좋음" + "4,693 이용후기" | 동일 체계 |
| **위치 평점** | O | "8.5 위치 평점" (별도 표시) | **종합 평점과 위치 평점 분리** — 고유 |
| 가격 (원가) | O | "원래 요금: 100,251" | 취소선 + tooltip |
| 가격 (할인가) | O | "₩86,697" | |
| **1박당 총금액** | O | "1박당 총 금액 ₩104,904" | **세금 포함 총액을 별도 행으로** |
| 세금 표시 | O | "세금 및 수수료 포함 요금" | |
| 할인율 | O | "-14%" | 퍼센트 뱃지 |
| **쿠폰 할인** | O | "A사SPONSORED 적용됨 - ₩13,554 할인!" | **쿠폰 할인 금액을 명시적으로 표시** |
| 잔여 객실 | O | "객실 1개 남음" | |
| **오늘 예약 횟수** | O | "오늘 51회 예약됨", "오늘 137회 예약됨" | **해외 OTA A에 없음** — 강력한 사회적 증거 |
| 수상 뱃지 | O | "2025년 수상" + "아고다 여행객 인기 숙소" | **자체 어워드 시스템** |
| 베스트 요금 태그 | O | "4성급 호텔 베스트 요금" | |

### 3-3. 인사이트

1. **"오늘 N회 예약됨"** — 해외 OTA A의 "N명이 보고 있음"보다 더 강력한 사회적 증거. 실제 예약 수치를 보여줌
2. **위치 평점이 종합 평점과 분리** — Rating 모델에 `overallScore`와 `locationScore`를 별도 관리 필요
3. **지역별 위치 평점 + 특징 태그** — 지역 자체가 "쇼핑", "나이트라이프" 등 태그를 가짐 → Location에 tags 배열 필요
4. **결제 옵션이 별도 필터 카테고리** — 결제 방식이 숙소 선택의 핵심 기준임을 시사
5. **"외부 배달 음식 허용"** 필터 — 코로나 이후 새로운 편의시설 카테고리의 등장
6. **해외 OTA(A사) Luxe** — 플랫폼 자체 등급 시스템. starRating 외에 platformGrade가 필요할 수 있음
7. **이미지 10장 캐러셀** — 검색 결과에서도 다수 이미지 제공. photos 배열이 검색 결과 레벨에서도 필요
8. **가격 이중 표시**: "₩86,697" (세금 전) + "1박당 총 금액 ₩104,904" (세금 포함) → 세전가/세후가 분리 필수

---

## 4. 국내 OTA A (검색결과 크롤링 + API 응답)

### 4-1. 검색 필터 구조

| 필터 카테고리 | 세부 옵션 | 해외 OTA와 차이 |
|---------------|-----------|-----------------|
| **매진 숙소 제외** | 체크박스 | **재고 없는 숙소를 검색에서 제거** — 해외 OTA에는 없음 |
| **숙소 유형** | 전체, 모텔, 호텔·리조트, 펜션, 홈&빌라, 캠핑, 게하·한옥 | **"캠핑"** 별도 카테고리 — 국내 특화. **radio 버튼** (단일 선택) |
| **가격** | 범위 선택 | |
| **태그 필터** | #리뷰좋은 | 해시태그 기반 필터 — 고유 |
| **할인혜택** | 쿠폰할인, 할인특가 | **쿠폰/할인을 별도 필터로** — 해외 OTA에 없음 |
| **대실예약** | 체크박스 | **시간제 이용(대실)** — 국내 모텔 전용 개념 |
| **성급** | 5성급, 4성급 | 2개만 (해외 OTA는 1~5 전체) |
| **편의시설** | 조식제공 등 | |

### 4-2. 검색 결과 카드 구조

| 필드 | 노출 여부 | 표시 방식 | 해외 OTA와 차이 |
|------|:---------:|-----------|-----------------|
| 숙소명 | O | heading (h3) | 동일 |
| 숙소 유형 | O | "모텔" 태그 | 카드 상단에 명시적 표시 |
| 이미지 | O | 1장 | |
| **역 근접성** | O | "종로3가역 도보 3분" / "서강대역 도보 3분" | **도보 N분** — 해외 OTA는 km 단위. 국내는 역 중심 |
| 평점 | O | "9.4" (10점 만점) | |
| 평가 수 | O | "6,545명 평가" | "이용후기" 대신 "평가" |
| **대실 정보** | O | "대실 5시간 30,000원" | **시간제 이용 — 국내 전용** |
| **대실 시간** | O | "4시간", "5시간", "최대 6시간", "최대 7시간" | |
| **숙박 체크인 시간** | O | "숙박 19:00 체크인", "숙박 22:00 체크인" | 카드에 체크인 시간 직접 표시 |
| 가격 (원가) | O | 취소선 "55,000원" | |
| 가격 (할인가) | O | "회원가 50,500원" | **"회원가"** 표시 — 멤버십 강조 |
| **쿠폰 적용가** | O | "4,500원 쿠폰 적용가", "선착순 2,000원 쿠폰 적용가" | **쿠폰 할인 금액이 카드에 직접 표시** |
| 잔여 객실 | O | "이 가격으로 남은 객실 1개" | 동일 패턴 |
| **숙소 홍보 문구** | O | "전객실 온열매트,침대가드 대여,75TV,새침구" | **파트너가 직접 작성한 홍보 문구** — 해외 OTA에 없음 |
| **객실 랜덤 배정** | O | "객실 랜덤 배정" | 특정 객실 지정 불가 표시 |
| **저장하기** | O | "이 상품 저장하기" | |

### 4-3. 인사이트 (국내 전용 개념들)

1. **대실(시간제 이용)** — 국내 OTA의 핵심 차별화. 모텔 도메인에서 "대실 N시간" + "숙박" 이중 상품 구조
   → `ReservationType: enum (DAYUSE, OVERNIGHT)` + `dayUseHours: Integer` 필요
2. **회원가** — "태그 55,000원 → 회원가 50,500원" 구조. 비회원가(태그가)/회원가 분리
   → `Rate.tagPrice` (정가) + `Rate.memberPrice` (회원가) 분리
3. **쿠폰 적용가가 카드에 직접 표시** — "4,500원 쿠폰 적용가"처럼 쿠폰 할인 금액을 명시
   → `Rate.couponDiscountAmount` 필드 필요
4. **역 도보 분** — 해외는 "중심부에서 1.3km"인데 국내는 "역삼역 도보 8분"
   → `Location.nearestStation` + `Location.walkingMinutes` 추가
5. **파트너 홍보 문구** — "전객실 온열매트,침대가드 대여" 같은 자유 텍스트
   → `Property.promotionText` 또는 `Property.highlights[]`
6. **체크인 시간이 검색 결과에 표시** — "숙박 19:00 체크인" 카드에 직접 노출
   → `Property.checkInTime`이 검색 결과 API에도 포함되어야 함
7. **매진 숙소 제외 필터** — 재고 0인 숙소를 검색에서 제거하는 옵션
   → 검색 도메인에 `excludeSoldOut: boolean` 필터

---

## 분석 방법론

이 분석은 AI(Claude Code)를 활용하여 수행했다:
1. Playwright MCP를 이용한 해외 OTA 실시간 크롤링 (검색 결과 + 상세 페이지)
2. 접근성 트리(browser_snapshot) 기반 구조적 데이터 추출
3. 기존 수집된 국내 OTA API 응답(sample.json) 구조 분석
4. 크로스 플랫폼 필드 비교 및 도메인 모델 매핑
