# OTA 크로스 플랫폼 비교 분석

## 비교 대상
- 해외 글로벌 OTA A: 검색결과 + 상세페이지 크롤링 데이터
- 해외 OTA B (아시아 특화): 검색결과 크롤링 데이터
- 국내 OTA A: 검색결과 크롤링 + API 응답(sample.json) 데이터
- 조사 일시: 2026-04-01

---

## 1. 공통 필드 (Core Domain — 모든 OTA 공통)

### Property (숙소) 레벨
| 필드 | 해외 OTA | 국내 OTA | 도메인 모델 제안 |
|------|----------|----------|------------------|
| 숙소 식별자 | URL slug 기반 | propertyId (숫자) | `Property.id` |
| 숙소명 | heading 텍스트 | - (별도 API) | `Property.name` |
| 위치 | "서대문구, 서울" + 중심부 거리 | - (별도 API) | `Property.location` |
| 평점 | 10점 만점 (8.2) | - (별도 API) | `Property.rating` (정규화 필요) |
| 리뷰 수 | "1,859개" | - (별도 API) | `Property.reviewCount` |

### Room (객실) 레벨
| 필드 | 해외 OTA | 국내 OTA | 도메인 모델 제안 |
|------|----------|----------|------------------|
| 객실명 | "스탠다드 더블룸 - 도시 전망" | nameKr: "트윈 룸" | `RoomType.name` |
| 면적 | "21 제곱미터" | pyeong: "7평", squareMeter: "25.0m²" | `RoomType.areaSqm` + 국내용 `areaPyeong` |
| 최대 인원 | "최대 투숙 인원: 2" | "성인 2명" | `RoomType.maxOccupancy` |
| 침대 유형 | "더블침대 1개" | "싱글베드 2개" | `RoomType.bedType` |
| 뷰 타입 | "도시 전망" | "시티뷰" | `RoomType.viewType` |
| 편의시설 | 에어컨, 전용욕실, TV 등 | 67개 facilities 배열 | `RoomType.amenities[]` |
| 사진 | 이미지 갤러리 | photos 배열 | `RoomType.photos[]` |

### Rate (요금) 레벨
| 필드 | 해외 OTA | 국내 OTA | 도메인 모델 제안 |
|------|----------|----------|------------------|
| 총 가격 | ₩356,400 | totalPrice: 62,235 | `Rate.totalPrice` |
| 1박 기준가 | (1박 검색이라 동일) | dailyPrice: 62,235 | `Rate.dailyPrice` |
| 객실 요금 | (분리 안됨) | roomPrice: 54,881 | `Rate.roomPrice` |
| 세금/수수료 | "세금 및 기타 요금 포함" | taxAndFees: 7,354 | `Rate.taxAndFees` |
| 원가 | ₩792,000 (취소선) | originalPrice: 62,235 | `Rate.originalPrice` |
| 할인율 | "55% OFF" | discountRate: null | `Rate.discountRate` |
| 잔여 객실 | "남은 옵션 3개" | "이 가격으로 1개 남음" | `Inventory.remainingText` |

### Cancellation (취소 정책) 레벨
| 필드 | 해외 OTA | 국내 OTA | 도메인 모델 제안 |
|------|----------|----------|------------------|
| 무료 취소 여부 | "무료 취소" 필터 | isFreeCancellation: boolean | `RatePlan.isFreeCancellation` |
| 환불 불가 여부 | "환불 불가" 텍스트 | isNonRefundable: boolean | `RatePlan.isNonRefundable` |
| 취소 정책 상세 | "취소 시 요금 전액 지불" | cancellationPolicy.text | `RatePlan.cancellationPolicyText` |

---

## 2. 차별 필드 (Extended — 일부 플랫폼만)

| 필드 | 보유 플랫폼 | 중요도 | 도메인 반영 제안 |
|------|-------------|:------:|------------------|
| 서플라이어 정보 | 국내 OTA | **높음** | `Supplier` 별도 도메인. 국내 OTA는 멀티 서플라이어 가격 비교가 핵심 |
| 쿠폰 가격 분리 | 국내 OTA | 중간 | `Rate.couponPrice` — 쿠폰 적용가를 별도 관리 |
| 멤버십 등급 | 국내 OTA | 중간 | `Membership.grade` — 엘리트 객실 등 등급별 상품 |
| 최저가 보장 | 국내 OTA | 낮음 | `Property.isLowPriceGuaranteed` |
| 이벤트 배너 | 국내 OTA | 낮음 | `CategoryEventBanner` — 마케팅용 |
| 평(pyeong) 면적 | 국내 OTA | 중간 | `RoomType.areaPyeong` — 국내 사용자 친화 |
| 성급 (별) | 해외 OTA | **높음** | `Property.starRating` — 1~5성급 |
| 브랜드 | 해외 OTA | 중간 | `Property.brand` — 체인 호텔 식별 |
| 배리어프리 | 해외 OTA | 중간 | `Property.accessibility`, `RoomType.accessibility` |
| 지속가능성 인증 | 해외 OTA | 낮음 | `Property.certifications` |
| 여행객 유형 | 해외 OTA | 중간 | `Property.guestPolicy` (반려동물, 성인전용 등) |
| 패키지 상품 | 해외 OTA | 중간 | `RatePlan.packageType` — 시티투어+객실 등 번들 |
| 결제 방식 | 해외 OTA | **높음** | `RatePlan.paymentPolicy` (선결제/현장결제) |
| 동네/랜드마크 거리 | 해외 OTA | 중간 | `Property.neighborhood`, `Property.nearbyLandmarks` |
| 조식 가격/평점 | 해외 OTA | 중간 | `RatePlan.breakfastPrice`, `RatePlan.breakfastRating` |

---

## 3. 국내 vs 해외 구조적 차이

| 항목 | 국내 방식 | 해외 방식 | 통합 모델 설계 시 고려사항 |
|------|-----------|-----------|---------------------------|
| **가격 표시** | totalPrice = roomPrice + taxAndFees (분리 제공) | "세금 및 기타 요금 포함" (합산 표시) | Rate에 roomPrice, taxAndFees, totalPrice 모두 필요 |
| **할인 표시** | discountRate 필드 (null 가능) | "55% OFF" 뱃지 + 원가 취소선 | Rate.originalPrice + discountRate |
| **면적 단위** | 평 + m² 이중 제공 | m² 단독 | areaSqm 필수, areaPyeong 선택 |
| **평점 체계** | (별도 API) | 10점 만점 | 정규화 로직 필요 (10점 → 5점 변환 등) |
| **서플라이어** | 멀티 서플라이어 가격 비교 | 단일 플랫폼 가격 | Supplier 도메인 + 가격 비교 로직 |
| **편의시설 분류** | flat 배열 (67개, type enum) | 숙소 시설 / 객실 시설 명확 분리 | amenities를 property/room 레벨로 분리하되, type enum으로 분류 |
| **취소 정책** | boolean + 상세 텍스트 | 텍스트 기반 (요금 플랜 행마다 다름) | RatePlan에 isFreeCancellation + cancellationPolicyText |
| **결제 방식** | (별도 API) | "선결제 필요 없음" / "체크인 전 결제" | RatePlan.paymentPolicy enum |
| **조식** | isFreeBreakfast + content | "조식 ₩23,000 (평가: 좋음)" 또는 "조식 포함" | RatePlan.breakfastIncluded + breakfastPrice |

---

## 4. 핵심 인사이트

### 인사이트 1: Rate Plan이 도메인의 핵심 복잡도
같은 객실(RoomType)에 대해 여러 요금 플랜이 존재한다:
- 환불 가능 / 환불 불가
- 조식 포함 / 조식 별도
- 선결제 / 현장 결제
- 패키지 (시티투어+객실)

→ **RoomType : RatePlan = 1 : N 관계가 필수**. 도메인 초안의 "RatePlan" 개념이 실제로도 핵심.

### 인사이트 2: 가격은 3단 구조
모든 OTA에서 가격은 단순 숫자가 아님:
```
totalPrice = roomPrice + taxAndFees
displayPrice = totalPrice에서 할인/쿠폰 적용
```
→ **Rate 모델에 roomPrice, taxAndFees, totalPrice, discountRate, couponDiscount 모두 필요**

### 인사이트 3: 편의시설은 2레벨 분리 필수
해외 OTA는 명확하게 "숙소 시설"과 "객실 시설"을 분리하고, 검색 필터에서도 별도 카테고리로 운영.
→ **Property.amenities[]와 RoomType.amenities[]를 분리해야 함**

### 인사이트 4: Supplier 통합은 국내 OTA의 차별화 포인트
국내 OTA는 EPS, AGODA, DOTW 등 멀티 서플라이어의 가격을 비교하여 최저가를 제공.
각 서플라이어마다 사업자 정보(법적 요구사항)까지 관리.
→ **Supplier 도메인은 단순 API 연동이 아니라, 법적 정보 + 가격 비교 로직이 포함되어야 함**

### 인사이트 5: 검색 필터 = 도메인 속성의 우선순위
검색 필터로 노출된 속성은 플랫폼이 중요하게 생각하는 것:
- **공통 최상위**: 가격, 숙소유형, 성급, 평점, 위치
- **해외 강조**: 결제 방식, 배리어프리, 브랜드, 지속가능성
- **국내 강조**: 무료취소, 쿠폰, 멤버십, 서플라이어 가격 비교

### 인사이트 6: "긴급성" 표시가 양쪽 모두 존재
- 해외: "이 요금으로 남은 옵션 3개"
- 국내: "이 가격으로 1개 남음"
→ **Inventory에 remainingCount 또는 remainingText가 필수**

---

---

## 5. 3개 플랫폼 추가 비교 (아고다 + 국내 OTA 크롤링 반영)

### 가격 표시 방식 비교
| 항목 | 해외 OTA A | 해외 OTA B | 국내 OTA A |
|------|------------|------------|------------|
| 기본 표시 | 세금 포함 총액 | 세전가 + 세후 총액 이중 표시 | 회원가 (태그가 취소선) |
| 할인 표시 | "55% OFF" 뱃지 | "-14%" + 원래 요금 취소선 | 태그가 → 회원가 |
| 쿠폰 | 없음 | "AGODASPONSORED ₩13,554 할인" | "4,500원 쿠폰 적용가" |
| 세금 | "세금 포함" 텍스트 | "세금 및 수수료 포함 요금" (별도 행) | API에서 roomPrice + taxAndFees 분리 |

**인사이트**: 가격 모델에 필요한 필드가 플랫폼마다 다름
→ `tagPrice`(정가), `memberPrice`(회원가), `roomPrice`(세전), `taxAndFees`(세금), `totalPrice`(세후), `couponDiscount`(쿠폰), `discountRate`(할인율) 모두 필요

### 사회적 증거 / 긴급성 비교
| 항목 | 해외 OTA A | 해외 OTA B | 국내 OTA A |
|------|------------|------------|------------|
| 잔여 객실 | "남은 옵션 3개" | "객실 1개 남음" | "이 가격으로 남은 객실 1개" |
| 실시간 관심 | 없음 | "오늘 137회 예약됨" | 없음 |
| 판매 완료율 | "86% 이미 판매 완료" | 없음 | "매진 숙소 제외" 필터 |
| 수상/뱃지 | "신규 숙소" | "2025년 수상", "아고다 여행객 인기 숙소" | 없음 |

### 국내 전용 개념 (해외 OTA에 없음)
| 개념 | 설명 | 도메인 영향 |
|------|------|-------------|
| **대실 (시간제 이용)** | 모텔에서 N시간 이용. "대실 5시간 30,000원" | `ReservationType` enum + `dayUseHours` |
| **회원가/태그가 분리** | 비회원 정가 vs 회원 할인가 | `Rate.tagPrice` + `Rate.memberPrice` |
| **역 도보 분** | "종로3가역 도보 3분" | `Location.nearestStation` + `walkingMinutes` |
| **파트너 홍보 문구** | "전객실 온열매트, 75TV" | `Property.promotionText` |
| **체크인 시간 검색 노출** | "숙박 19:00 체크인" | 검색 API에 checkInTime 포함 |
| **객실 랜덤 배정** | 특정 객실 선택 불가 | `RoomType.isRandomAssignment` |

### 해외 OTA B 고유 개념
| 개념 | 설명 | 도메인 영향 |
|------|------|-------------|
| **위치 평점 분리** | 종합 7.4 + 위치 8.5 별도 표시 | `Rating.overallScore` + `Rating.locationScore` |
| **지역 특징 태그** | "강남: 쇼핑", "홍대: 나이트라이프" | `Neighborhood.tags[]` |
| **오늘 N회 예약됨** | 실시간 예약 카운트 | `Property.todayBookingCount` |
| **자체 등급 (Luxe)** | starRating 외 플랫폼 자체 등급 | `Property.platformGrade` |
| **외부 배달 음식 허용** | 편의시설 필터 | `Amenity.EXTERNAL_DELIVERY_ALLOWED` |
| **이미지 10장 캐러셀** | 검색 결과에서 다수 이미지 | 검색 API에 photos 배열 |

---

## 분석 방법론

이 비교 분석은 AI(Claude Code)를 활용하여 수행했다:
1. Playwright MCP 기반 해외 OTA 2개 + 국내 OTA 1개 실시간 크롤링
2. 기존 국내 OTA API 응답(sample.json) 분석
3. OTA 도메인 가이드(references/ota-domain-guide.md) 참조하여 업계 맥락 적용
4. 3개 플랫폼 크로스 비교 및 도메인 모델 시사점 도출
