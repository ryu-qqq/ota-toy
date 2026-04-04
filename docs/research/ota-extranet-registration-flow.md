# OTA Extranet 숙소 등록/관리 흐름 조사

> 조사일: 2026-04-04
> 목적: 숙소 기본정보/사진/편의시설/객실이 한 번에 등록되는지 별도 단계인지 확인
> 결론: **각각 별도 단계로 저장되고, 등록 후에도 독립적으로 수정 가능**

---

## 1. Booking.com Extranet

### 등록 흐름 (8단계, 각 단계 별도 저장)

| 단계 | 내용 | 비고 |
|------|------|------|
| 1 | 계정 생성 | 이메일, 비밀번호 |
| 2 | **기본정보** | 숙소 유형, 주소 |
| 3 | **객실 + 편의시설** | 객실별 크기, 침대 구성, 편의시설 |
| 4 | **사진 업로드** | 드래그&드롭, 객실별 분류 |
| 5 | **정책 설정** | 체크인/아웃, 세금, 환불 규칙 |
| 6 | 결제 방법 | 은행 계좌 등록 |
| 7 | 소유권 인증 | 4자리 코드 확인 |
| 8 | 목록 활성화 | 숙소 ID 발급, Extranet 접근 |

**중간 저장 가능**: "You can pause the registration process anytime – your progress will be saved. To pick up where you left off, go to the homepage and click Continue your registration."

### 등록 후 관리 — Property 탭 하위 별도 섹션

| 섹션 | 경로 | 독립 수정 |
|------|------|----------|
| **Photos** | Property > Photos | O — 업로드/삭제/재정렬, 객실별 드래그&드롭 분류 |
| **General Info / Description** | Property > Property Details | O — 숙소 설명 독립 수정 |
| **Facilities & Amenities** | Property > Facilities | O — 편의시설 독립 토글 |
| **Rooms** | Rates & Availability | O — 객실 추가/삭제/수정 독립 |
| **Policies** | Property > Policies | O — 체크인/아웃, 하우스룰 독립 |

### Extranet 메인 메뉴 탭 구조

```
Home | Rates & Availability | Promotions | Reservations | Property | Opportunities | Inbox | Guest Reviews | Analytics | Booking Suite
```

Property 탭 하위:
```
Property
├── Property Details (기본정보, 설명)
├── Photos (사진 관리)
├── Facilities (편의시설)
├── Policies (정책)
└── Messaging preferences
```

### 핵심 인사이트

1. **사진은 완전히 독립적인 생명주기** — 업로드/삭제/재정렬이 숙소 기본정보와 무관하게 수시 발생
2. **편의시설은 토글 방식** — 전체 목록에서 on/off, 숙소 상태 변경과 무관
3. **객실은 Rates & Availability에서 관리** — Property 탭이 아닌 별도 탭, 요금과 함께 관리
4. **등록과 관리의 메뉴 구조가 동일** — 등록 시 단계별로 입력한 것을 이후에도 같은 메뉴에서 독립 수정

---

## 2. 야놀자 NOL 파트너센터

### 등록 흐름 (단계별 별도 저장)

| 단계 | 내용 |
|------|------|
| 1 | **기본 정보 입력** — 숙소명, 주소, 연락처, 체크인/체크아웃 시간 |
| 2 | **객실 정보 등록** — 객실 타입, 가격, 인원 기준, 편의시설 설정 |
| 3 | **사진 업로드** — 실내외 전경, 객실, 욕실 등 고화질 이미지 |
| 4 | **요금 설정** — 요일별, 시즌별 요금 조정 |
| 5 | **할인/프로모션** — 쿠폰, 즉시할인, 시즌 특가 |

### 입점 심사

- 사업자등록증 + 숙박업 등록증 필요
- 심사 기간: 2~5일

### 등록 후 관리

- 실시간 객실 수정 가능
- 판매 설정에서 객실 마감, 수량 변경, 가격 변경 독립 관리
- 모바일 앱(NOL 파트너센터)에서도 관리 가능 (기능 일부 제한)

### 핵심 인사이트

1. **대실/숙박 구분이 있는 국내 특수성** — 해외 OTA에 없는 개념
2. **객실 + 편의시설이 같은 단계** — Booking.com과 동일하게 객실 등록 시 편의시설 함께 설정
3. **사진은 별도 단계** — 기본정보/객실과 분리
4. **요금과 프로모션이 분리** — 기본 요금 설정과 할인/프로모션이 별도 단계

---

## 3. 여기어때 파트너센터

### 등록 흐름

- 구체적인 단계별 흐름 공개 정보 부족
- 파트너센터 앱에서 예약 확정, 판매 설정 변경 가능
- 판매 설정: 객실 마감, 수량 변경, 가격 변경 독립 관리

---

## 4. 크로스 플랫폼 공통 패턴

### 등록 흐름

| 항목 | Booking.com | 야놀자 | 패턴 |
|------|------------|--------|------|
| 기본정보 | 별도 단계 | 별도 단계 | **공통** |
| 객실 | 별도 단계 | 별도 단계 | **공통** |
| 사진 | 별도 단계 | 별도 단계 | **공통** |
| 편의시설 | 객실과 함께 or 별도 | 객실과 함께 | 객실 레벨에서 설정 |
| 요금 | 별도 단계 | 별도 단계 | **공통** |
| 정책 | 별도 단계 | 요금과 함께 | 플랫폼마다 다름 |
| 중간 저장 | 가능 | 가능 | **공통** |

### 등록 후 관리

| 항목 | 독립 수정 | 생명주기 | 빈도 |
|------|----------|---------|------|
| 기본정보 | O | 거의 안 바뀜 | 낮음 |
| 사진 | O | 수시 업로드/삭제/재정렬 | 높음 |
| 편의시설 | O | 토글 on/off | 중간 |
| 객실 | O | 추가/삭제/수정 | 중간 |
| 요금 | O | 시즌별/요일별 수시 변경 | 매우 높음 |

---

## 5. 도메인 설계 시사점

### Collection VO Aggregate 포함 여부 → **포함하지 않는 것이 맞다**

| 근거 | 설명 |
|------|------|
| **생명주기 독립** | 사진/편의시설은 숙소 기본정보와 별개로 수시 변경 |
| **행위자 분리** | 사진=마케팅, 편의시설=운영, 기본정보=계약 |
| **트랜잭션 경계** | 사진 정렬 실패가 숙소 정보 롤백을 유발하면 안 됨 |
| **API 엔드포인트 분리** | 실제 OTA 모두 사진/편의시설/객실을 별도 API로 관리 |
| **Extranet UI 구조** | Property 탭 하위 별도 섹션으로 분리 |

### 현재 설계 (ID 참조) 가 업계 표준과 일치

```
Property (Aggregate Root) — 기본정보, 상태
  ↕ ID 참조
PropertyPhotos — 독립 관리 (업로드/삭제/재정렬)
PropertyAmenities — 독립 관리 (토글)
RoomType (별도 Aggregate Root) — 독립 관리
```

조회 시 조립은 Application 레이어에서:
```java
// Application 레이어
public record PropertyDetail(
    Property property,
    PropertyPhotos photos,
    PropertyAmenities amenities,
    PropertyAttributeValues attributes
) {}
```

---

## 출처

- [Booking.com - Registering your property](https://partner.booking.com/en-us/help/working-booking/going-live/registering-your-property)
- [Booking.com - Setting up your property page](https://partner.booking.com/en-us/learn-more/new-partner/setting-your-property-listing)
- [Booking.com - Changing property description or room details](https://partner.booking.com/en-us/help/property-page/general-info/changing-your-property-description-or-room-details)
- [Booking.com Extranet Guide - BnB Management London](https://bnbmanagementlondon.co.uk/booking-com-extranet-guide/)
- [Booking.com Extranet Guide - SiteMinder](https://www.siteminder.com/r/trends-advice/hotel-management-tips-ideas/booking-com-extranet/)
- [Booking.com Extranet Guide - SmartOrder](https://www.smartorder.ai/resources/blog/booking-com-extranet/)
- [야놀자 파트너센터](https://partner.yanolja.com/)
- [야놀자 파트너센터 운영 가이드](https://koreastrb.moneyenter.com/489)
