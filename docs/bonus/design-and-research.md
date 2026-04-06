# 설계/조사 관련 문서

> OTA 숙박 플랫폼을 설계하기 위해 수행한 리서치와, 리서치 결과가 설계에 어떻게 반영되었는지 정리한 문서입니다.

---

## 1. 리서치 방법론

OTA 도메인을 처음 다루는 상황에서 "상상으로 도메인을 설계"하면 실제 업무 규칙을 놓칠 위험이 큽니다. 실제 서비스를 관찰하여 도메인을 도출하는 접근을 택했습니다.

### 조사 대상과 방법

| 대상 | 방법 | 수집 데이터 |
|------|------|-----------|
| 해외 글로벌 OTA (B사) | Playwright 실시간 크롤링 | 검색 결과 카드, 상세 페이지, 검색 필터 구조 |
| 해외 아시아 특화 OTA (A사) | Playwright 실시간 크롤링 | 검색 결과, 필터, 가격 구조, 지역 평점 |
| 국내 OTA (Y사) | API 응답 구조 분석 + 크롤링 | JSON 응답 전체 구조, 서플라이어 데이터, 가격 분리 체계 |
| OTA Extranet (파트너센터) | 공개 문서 + 가이드 조사 | 등록 흐름 단계, 관리 메뉴 구조 |
| 예약 API (Amadeus, Expedia, HotelBeds 등) | 공식 API 문서 분석 | 멱등키 처리, 2단계 예약, 재고 선점 패턴 |

조사일: 2026-04-01 ~ 2026-04-06

> 원본 데이터: [docs/research/](../research/)

---

## 2. 핵심 발견과 설계 반영

### 발견 1: 같은 객실에 4~5개 요금 행이 존재합니다

해외 OTA(B사) 상세 페이지에서 "스탠다드 더블룸" 하나에 다음 요금 행들이 동시에 노출됩니다:

| 요금 행 | 취소 정책 | 조식 | 결제 방식 | 가격 |
|---------|----------|------|----------|------|
| 1 | 무료 취소 | 별도 | 현장 결제 | ₩356,400 |
| 2 | 환불 불가 | 별도 | 선결제 | ₩298,000 |
| 3 | 무료 취소 | 포함 (₩23,000) | 선결제 | ₩423,500 |
| 4 | 패키지 (시티투어) | 포함 | 선결제 | ₩512,000 |

**설계 반영**: `RoomType : RatePlan = 1:N` 관계를 도입했습니다. RatePlan이 취소 정책, 결제 방식을 담고, 조식/패키지는 `RatePlanAddOn`으로 분리하여 조합 폭발을 방지했습니다.

### 발견 2: 가격은 단순 숫자가 아니라 3단 구조입니다

3개 플랫폼 모두에서 가격이 다음과 같이 분리됩니다:

```
totalPrice = roomPrice + taxAndFees
표시 가격 = totalPrice에서 할인/쿠폰 적용
```

국내 OTA(Y사)는 추가로 태그가(정가)/회원가 분리, 쿠폰 적용가 별도 관리까지 존재합니다.

**설계 반영**: 단순 "날짜별 가격"이 아닌, `RateRule`(규칙) → `Rate`(계산된 스냅샷) → `Redis`(캐시) 3단 레이어로 설계했습니다. 파트너는 "기간 + 요일별 가격"이라는 규칙을 설정하고, 고객은 미리 계산된 날짜별 가격을 Redis에서 즉시 조회합니다.

### 발견 3: 국내 OTA 실제 API 응답에서 도메인 필드를 도출했습니다

국내 OTA(여회사)의 실제 화면에서 호출되는 API 엔드포인트 3개의 JSON 응답을 수집하여 분석했습니다.

**수집한 엔드포인트:**
- 메인 페이지 SSR 데이터 (Next.js `_next/data/` 경로) — 숙소 목록 + 객실 + 가격
- 날짜별 캘린더 요금 API (`/places/{id}/calendar`) — 날짜별 최저가 + 객실 ID
- 추천 숙소 API (`/places/{id}/recommendations`) — 유사 숙소 목록

**실제 API 필드에서 발견한 핵심 구조:**

```
meta: {
  id: 48951,                              → Property.id
  name: "세인트존스 호텔",                   → Property.name
  grade: "블랙 · 특급 · 호텔",              → Property.grade (등급 분류 체계)
  gradeCode: "S",                          → PropertyType.code
  review: { rate: 9.2, count: 10996 },     → 10점 만점 평점 + 리뷰 수
  category: 2,                             → 숙소 카테고리 (1=모텔, 2=호텔...)
  address: {
    address: "강릉시",
    traffic: "강문해변 앞"                   → Location.traffic (교통 정보)
  },
  location: { latitude, longitude },        → Coordinate VO
  freeCancel: null,                         → 무료 취소 여부
  description: "강문해변 앞에 자리 잡아..."    → Property.description
}
```

**대실/숙박 분리 구조 — 국내 OTA만의 핵심 개념:**

```
room: {
  type: "MOTEL",                            → 모텔일 때만 rent/stay 분리
  rent: {                                   → 대실 (시간제 이용)
    title: "대실",
    label: { option: "대실", checkInOut: "5시간" },
    price: { strikePrice: 40000, discountPrice: 37500, discountRate: "6%" },
    placeMaxUseTime: 5                      → 대실 최대 시간
  },
  stay: {                                   → 숙박
    title: "숙박",
    label: {
      option: "숙박",
      checkInOut: "17:00 체크인",
      remain: "이 가격으로 남은 객실 1개"      → 재고 긴급성 표시
    },
    price: { strikePrice: 40000, discountPrice: 39000 },
    coupon: { code: "COUPON_VOUCHER", value: "2500", unit: "UNIT_KRW" }
  }
}
```

**날짜별 요금 캘린더 구조:**

```
{ checkInDate: "2026-04-06", roomId: 563891, discountPrice: 176000, roundDiscountPrice: "17.6만" }
{ checkInDate: "2026-04-11", roomId: 292074, discountPrice: 232300, roundDiscountPrice: "23.2만" }
// → 날짜마다 최저가 객실이 다르고, 요일별 가격 변동이 뚜렷함
// → 금토: 23~26만, 평일: 8~9만 → RateRule의 요일별 가격 설계 근거
```

**설계 반영:**

| 실제 API 필드 | 도메인 모델 반영 |
|-------------|---------------|
| `meta.grade` / `gradeCode` | PropertyType + PropertyTypeAttribute (EAV 패턴) |
| `room.type: MOTEL/NON_MOTEL` | 숙소 유형에 따른 분기 처리 |
| `rent` / `stay` 분리 | 대실/숙박 개념은 설계 범위에 포함, 구현은 숙박(stay)에 집중 |
| `price.strikePrice` / `discountPrice` | Rate의 원가/할인가 분리 |
| `coupon.value` | 쿠폰 가격 분리 (설계만) |
| `label.remain` | Inventory.availableCount → 프론트에서 잔여 객실 표시 |
| `address.traffic` | Location VO의 교통 정보 필드 |
| 캘린더 요일별 가격 변동 | RateRule의 weekdayPrice/fridayPrice/saturdayPrice 설계 근거 |
| `favorites: ["#온수풀", "#바닷가"]` | 해시태그 기반 편의시설 → AmenityType enum |

### 발견 4: 편의시설은 숙소/객실 2레벨로 분리됩니다

해외 OTA(B사) 검색 필터에서 "숙소 시설"과 "객실 시설"이 별도 카테고리로 명확히 분리됩니다. 국내 OTA(Y사)도 객실 수준에서 다수의 편의시설을 배열로 관리합니다.

**설계 반영**: `PropertyAmenity`(숙소 편의시설)와 `RoomAmenity`(객실 편의시설)를 분리하고, `AmenityType` enum으로 카테고리화했습니다.

### 발견 5: 숙소 등록은 단계별 별도 저장입니다

해외 OTA(B사) Extranet 공개 문서를 조사한 결과, 숙소 등록은 여러 단계로 구성되며 각 단계가 **별도 저장**됩니다. 중간에 등록을 중단해도 진행 상황이 보존되고, 등록 후에도 사진/편의시설/객실/요금을 각각 독립적으로 수정할 수 있습니다.

| 항목 | 독립 수정 | 변경 빈도 | 행위자 |
|------|----------|----------|--------|
| 기본정보 | O | 거의 안 바뀜 | 계약 담당 |
| 사진 | O | 수시 업로드/삭제/재정렬 | 마케팅 |
| 편의시설 | O | 토글 on/off | 운영 |
| 객실 | O | 추가/삭제/수정 | 운영 |
| 요금 | O | 시즌별/요일별 수시 변경 | 수익 관리 |

**설계 반영**: Property Aggregate에 사진/편의시설 컬렉션을 포함하지 않고, ID 참조로 분리했습니다. UseCase도 독립적으로 분리했습니다 (`RegisterPropertyUseCase`, `SetPropertyPhotosUseCase`, `SetPropertyAmenitiesUseCase` 등). 사진 업로드 실패가 숙소 기본정보 롤백을 유발하지 않습니다.

### 발견 6: Supplier 통합은 B2B 도매 모델입니다

국내 OTA(Y사) API 응답에서 같은 숙소에 대해 복수의 외부 공급자가 동시에 가격을 제공합니다. 전자상거래법에 따라 각 공급자의 사업자등록번호, 대표자명, 이용약관 URL 등 법적 정보도 함께 관리됩니다.

**설계 반영**: Supplier 도메인을 독립 BC로 분리하고, ACL(Anti-Corruption Layer) 패턴으로 외부 API 응답이 자사 도메인을 오염시키지 않도록 격리했습니다. 2단계 동기화(수집: 외부 API → Raw 저장, 가공: ACL 변환 → Diff → Property 저장)로 구현했습니다.

### 발견 7: OTA 예약은 "2단계 프로세스"가 업계 표준입니다

Amadeus, Expedia, HotelBeds 등 주요 OTA API 공식 문서를 분석한 결과, 모두 2~3단계 예약 프로세스를 채택하고 있습니다.

| 플랫폼 | 예약 흐름 | 핵심 메커니즘 |
|--------|----------|-------------|
| Amadeus | Search → Offer → Book | offerId가 세션 토큰 역할 |
| Expedia Rapid | PriceCheck → Hold → Resume | 임시 홀드 + 토큰화된 resume 링크 |
| HotelBeds | Avail → CheckRate → Book | rateKey + clientReference |

서버가 발급한 토큰(offerId, booking_link, rateKey)이 자연스럽게 멱등키 역할을 수행하며, 재고 선점과 멱등성 보장을 동시에 해결합니다.

**설계 반영**: OTA 업계의 2단계 프로세스와 Stripe의 Idempotency-Key를 결합한 하이브리드 방식을 채택했습니다.

```
1단계: POST /reservation-sessions (Idempotency-Key 헤더)
  → 서버가 sessionId 발급 + Redis 재고 임시 차감

2단계: POST /reservations (sessionId로 확정)
  → Redis Lua 스크립트 원자적 차감 → DB 기록 → 실패 시 보상
```

---

## 3. 도메인 초안 검증

리서치 전에 작성한 도메인 초안을, 실제 크롤링 데이터와 대조 검증했습니다.

### 초안에서 맞았던 부분
- 숙소/재고/요금 분리 (변경 빈도 차이)
- Supplier ACL 패턴의 필요성
- RatePlan 개념 (요금 조건 묶음)
- 예약 상태 흐름 (PENDING → CONFIRMED → COMPLETED/CANCELLED)

### 초안에서 보강한 부분

| 영역 | 초안 | 실제 데이터에서 확인 | 개선 |
|------|------|-------------------|------|
| 편의시설 | 단일 리스트 | 숙소/객실 2레벨 분리 필수 | PropertyAmenity + RoomAmenity |
| 위치 | 주소 + 좌표 | 행정구 + 관광지역 + 중심부 거리 + 랜드마크 | Location VO 확장 + Landmark 독립 BC |
| 가격 | 날짜별 단일 가격 | roomPrice + taxAndFees + 할인 + 쿠폰 | RateRule → Rate 계산 구조 |
| 사진 | 숙소/객실 사진 | 유형별 분류 (EXTERIOR, ROOM, LOBBY) | PhotoType enum 추가 |
| 숙소 유형 | 호텔/모텔/펜션 | 10개+ 유형, EAV 패턴으로 유형별 속성 | PropertyType + PropertyTypeAttribute |

### 초안에 없었는데 추가한 개념

| 개념 | 발견 플랫폼 | 중요도 | 반영 |
|------|-----------|:------:|------|
| 결제 방식 (PaymentPolicy) | 해외 OTA | 높음 | RatePlan.paymentPolicy enum |
| 브랜드 (Brand) | 해외 OTA | 중간 | Brand 독립 BC |
| EAV 속성 (성급 등) | 해외 OTA | 높음 | PropertyTypeAttribute → PropertyAttributeValue |
| Supplier 법적 정보 | 국내 OTA | 높음 | Supplier에 사업자번호, 대표자명 등 |
| RatePlanAddOn (조식/패키지) | 해외 OTA | 중간 | 조합 폭발 방지 |

> 원본: [docs/research/research-03-domain-validation.md](../research/research-03-domain-validation.md)

---

## 4. 국내 vs 해외 OTA 차이가 설계에 미친 영향

3개 플랫폼을 비교하면서 국내와 해외 OTA의 구조적 차이를 발견했고, 이를 통합 모델에 반영했습니다.

| 항목 | 국내 방식 | 해외 방식 | 통합 설계 |
|------|----------|----------|----------|
| 가격 표시 | roomPrice + taxAndFees 분리 | 세금 포함 합산 | Rate에 roomPrice, taxAndFees, totalPrice 모두 보유 |
| 면적 단위 | 평 + m² 이중 제공 | m² 단독 | areaSqm 필수, areaPyeong 선택 |
| 서플라이어 | 멀티 서플라이어 가격 비교 | 단일 플랫폼 | Supplier BC + RatePlan.sourceType |
| 취소 정책 | boolean + 텍스트 | 요금 행마다 텍스트 | isFreeCancellation + cancellationPolicyText |
| 편의시설 | flat 배열 (type enum) | 숙소/객실 분리 | 2레벨 분리 + AmenityType enum |
| 위치 | "OO역 도보 8분" | "중심부에서 1.3km" | Location에 역/도보분 + Landmark에 거리 |

---

## 5. 참고 자료

### 리서치 원본 (docs/research/)

| 문서 | 내용 |
|------|------|
| [research-01-platform-analysis.md](../research/research-01-platform-analysis.md) | 해외 2 + 국내 1 플랫폼 크롤링 상세 데이터 |
| [research-02-cross-platform-comparison.md](../research/research-02-cross-platform-comparison.md) | 3개 플랫폼 공통/차별 필드 비교표 |
| [research-03-domain-validation.md](../research/research-03-domain-validation.md) | 도메인 초안 vs 실제 데이터 대조 결과 |
| [research-04-extranet-registration-flow.md](../research/research-04-extranet-registration-flow.md) | 해외 OTA/국내 OTA Extranet 등록 흐름 |
| [research-05-idempotency-patterns.md](../research/research-05-idempotency-patterns.md) | OTA API 멱등키 처리 패턴 비교 |

### 설계 결정 문서

| 문서 | 내용 |
|------|------|
| [ADR-001: 예약 동시성 제어](../design/adr/adr-001-reservation-concurrency.md) | Redis Lua + DB 2중 구조 선택 근거 |
| [도메인 설계 근거](../design/design-01-domain-rationale.md) | BC 분리, Aggregate 패턴, 요금 구조 등 설계 판단 |
| [도메인 연관관계](../design/design-02-domain-relationships.md) | BC 간 ID 참조 관계 상세 |
| [ERD](erDiagram.md) | 전체 테이블 관계도 (Mermaid) |
| [구현 로드맵](../design/design-03-implementation-roadmap.md) | Phase 1~8 구현 순서 계획 |

### 외부 참고 자료

| 자료 | 출처 |
|------|------|
| Amadeus Hotel APIs Tutorial | developers.amadeus.com |
| Expedia Rapid API - Hold & Resume | developers.expediagroup.com |
| HotelBeds Booking API | developer.hotelbeds.com |
| Brandur - Idempotency Keys in Postgres | brandur.org |
