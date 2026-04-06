# OTA 멱등키 처리 방식 리서치

## 조사 일시
2026-04-06

## 조사 배경
OTA 플랫폼의 예약 생성 시 멱등성 보장 방식을 조사하여, ota-toy 프로젝트의 Reservation BC 설계 근거로 활용한다.
네트워크 장애, 클라이언트 재시도, 이중 클릭 등으로 인한 중복 예약을 방지하는 것이 핵심 목표이다.

---

## 1. 업계 표준 패턴

### 1.1 Stripe Idempotency-Key 패턴 (결제 업계 표준)

Stripe는 멱등성 구현의 사실상 업계 표준으로, 대부분의 결제/예약 시스템이 이 패턴을 참조한다.

**핵심 구현 방식:**

| 항목 | 내용 |
|------|------|
| 키 생성 주체 | 클라이언트 (UUID v4 권장, 최대 255자) |
| 전달 방식 | HTTP 헤더 `Idempotency-Key` |
| 적용 대상 | 모든 POST 요청 (GET/DELETE는 본질적으로 멱등) |
| 키 만료 | 24시간 후 자동 제거 |
| 응답 캐싱 | 첫 요청의 status code + body를 저장, 동일 키 재요청 시 캐시된 응답 반환 |
| 파라미터 검증 | 동일 키로 다른 파라미터 전송 시 에러 반환 |

**서버 처리 로직:**
1. `Idempotency-Key` 헤더 수신
2. 해당 키로 기존 요청 조회
3. 기존 요청 없음 → 정상 처리 후 결과 저장
4. 기존 요청 있음 + 완료 상태 → 캐시된 응답 반환
5. 기존 요청 있음 + 처리 중 → 409 Conflict 반환
6. 기존 요청 있음 + 다른 파라미터 → 422 에러 반환

**에러 처리 특이사항:**
- 검증 실패(400 등)는 결과를 저장하지 않아 재시도 가능
- 500 에러도 저장하여 동일 응답 반환 (의도적 설계)
- 엔드포인트 실행이 시작된 이후의 결과만 저장

> **출처:** [Stripe API - Idempotent Requests](https://docs.stripe.com/api/idempotent_requests), [Stripe Blog - Designing robust and predictable APIs with idempotency](https://stripe.com/blog/idempotency)

### 1.2 IETF Idempotency-Key 헤더 표준 초안

IETF HTTPAPI Working Group에서 `Idempotency-Key` 헤더를 HTTP 표준으로 제정 중이다 (draft-ietf-httpapi-idempotency-key-header-07).

**표준 규격:**

```
Idempotency-Key: "8e03978e-40d5-43e8-bc93-6894a57f9324"
```

- Item Structured Header로 정의, 값은 반드시 String
- UUID 또는 충분한 엔트로피를 가진 랜덤 식별자 권장
- POST, PATCH 등 비멱등 HTTP 메서드에 적용

**서버 응답 규격:**
| 상황 | HTTP 상태 코드 |
|------|---------------|
| 필수 헤더 누락 | 400 Bad Request |
| 동일 키 + 다른 페이로드 | 422 Unprocessable Content |
| 원본 요청 처리 중 재시도 | 409 Conflict |

**보안 고려사항:**
- 강력한 엔트로피(UUID)로 키 추측 공격 방지
- 클라이언트별 속성과 결합한 복합 키 사용 권장 (user_id + idempotency_key)
- 키 만료 정책을 API 문서에 명시해야 함

> **출처:** [IETF Draft - The Idempotency-Key HTTP Header Field](https://www.ietf.org/archive/id/draft-ietf-httpapi-idempotency-key-header-07.html)

### 1.3 Brandur의 Postgres 기반 멱등키 구현 (Stripe 내부 엔지니어)

Stripe 엔지니어 Brandur가 공개한 Postgres 기반 구현 패턴으로, 실제 Stripe의 내부 구현을 참조한 설계이다.

**데이터베이스 스키마:**

```sql
CREATE TABLE idempotency_keys (
  id              BIGSERIAL PRIMARY KEY,
  idempotency_key TEXT NOT NULL,
  locked_at       TIMESTAMPTZ,
  request_method  TEXT NOT NULL,
  request_params  JSONB NOT NULL,
  response_code   INT NULL,
  response_body   JSONB NULL,
  recovery_point  TEXT NOT NULL,
  user_id         BIGINT NOT NULL,
  UNIQUE (user_id, idempotency_key)
);
```

**핵심 개념 - Atomic Phase:**
외부 서비스 호출(결제, 알림 등) 사이의 모든 로컬 DB 작업을 하나의 트랜잭션으로 묶는 단위.
SERIALIZABLE 격리 수준으로 동시성 충돌을 감지한다.

**Recovery Point (상태 머신):**
```
started → ride_created → charge_created → finished
```
- 각 단계 완료 시 recovery_point를 업데이트
- 재시도 시 저장된 recovery_point에서 재개
- finished 상태면 캐시된 응답 반환

**보조 프로세스:**
- **Completer**: 미완료 요청 자동 재개 (클라이언트 연결 끊김 대비)
- **Reaper**: 72시간 후 만료 키 삭제

> **출처:** [Implementing Stripe-like Idempotency Keys in Postgres](https://brandur.org/idempotency-keys)

### 1.4 2단계 예약 프로세스 (Booking Session 패턴)

대부분의 OTA/여행 플랫폼이 채택하는 예약 흐름으로, 멱등성과 밀접하게 연결된다.

**일반적인 2단계 흐름:**

```
[1단계: 세션 생성]
POST /booking-sessions → { sessionId: "abc-123", expiresAt: "..." }
  - 서버가 고유 세션/토큰 발급
  - 재고 임시 홀드 (soft lock)
  - 유효기간: 보통 5~15분

[2단계: 예약 확정]
POST /bookings { sessionId: "abc-123", paymentInfo: {...} }
  - sessionId가 멱등키 역할 수행
  - 동일 sessionId로 재요청 시 동일 결과
  - 만료된 세션 → 예약 실패
```

**멱등성과의 연결:**
- 1단계에서 서버가 발급한 sessionId/token이 자연스럽게 멱등키 역할
- 클라이언트가 별도로 UUID를 생성할 필요 없음
- 재고 선점(soft lock)과 멱등성 보장을 동시에 해결

**장점:**
- 가격 변동 방지 (세션 생성 시점의 가격 고정)
- 재고 경합 감소 (임시 홀드로 동시 접근 제어)
- 결제 전 최종 확인 기회 제공

---

## 2. OTA 플랫폼별 분석

### 2.1 Amadeus Hotel API

Amadeus는 3단계 예약 프로세스를 채택한다.

**예약 흐름:**

```
Search → Offer → Book

1. Hotel Search API: 가용성 검색
   → offerId 반환 (예: "ZBC0IYFMFV")

2. Hotel Offers API: offerId로 가격/가용성 재확인
   GET /v3/shopping/hotel-offers/{offerId}
   → 실시간 가격 검증

3. Hotel Booking API: 예약 확정
   POST /v1/booking/hotel-bookings
   → offerId + 게스트 정보 + 결제 정보
   → 예약 확인 ID 반환
```

**멱등성 관련 메커니즘:**
- offerId가 사실상 세션 토큰 역할 (시간 제한 있는 고유 식별자)
- 예약 전 offerId 재조회로 가용성 재확인 권장
- 명시적인 `Idempotency-Key` 헤더는 공개 문서에서 확인되지 않음
- offerId 기반 중복 방지가 주요 전략으로 추정

**특이사항:**
- 동시 접근 환경에서 offerId 재검증 로직을 개발자에게 위임
- "수천 명이 동시 예약하므로 가용성 변동 가능"을 명시적으로 경고

> **출처:** [Amadeus Hotel APIs Tutorial](https://developers.amadeus.com/self-service/apis-docs/guides/developer-guides/resources/hotels/)

### 2.2 Booking.com Connectivity API

Booking.com은 Pull 방식의 예약 연동을 사용한다. 일반적인 REST API 예약 생성과는 다른 패턴이다.

**예약 프로세스 (OTA 연동):**

```
1. 공급자가 GET OTA_HotelResNotif 폴링 (20초 간격 권장)
2. Booking.com이 신규 예약 데이터 반환
3. 공급자 시스템에 예약 통합 및 인벤토리 업데이트
4. POST OTA_HotelResNotif로 처리 확인 전송
5. 성공 응답 수신
```

**멱등성 관련 메커니즘:**
- `ota_res_response_token` 기능: 409 에러로 충돌 감지 후 최신 변경사항 재조회
- 폴링 기반이므로 전통적 멱등키보다는 "처리 확인(ACK)" 패턴 사용
- HTTP 500 오류 시 성공할 때까지 재시도 정책
- HotelReservationID 태그의 토큰으로 메시지 수신 확인

**특이사항:**
- B2C가 아닌 B2B(채널 매니저) 관점의 API
- 명시적 멱등키 헤더 없이, 구조적으로 멱등성을 보장 (GET → 처리 → ACK)
- 폴백: 연속 실패 시 이메일 직접 전송

> **출처:** [Booking.com - Understanding the OTA reservations process](https://developers.booking.com/connectivity/docs/reservations-api/reservations-process-ota)

### 2.3 Expedia Rapid API

Expedia는 Price Check → Book → Retrieve 흐름과 함께 **Hold and Resume** 기능을 제공한다.

**예약 흐름:**

```
1. Availability: 가용성 검색

2. Price Check: 최종 가격 확인
   → 예약 링크(booking link) 반환 (단시간 유효)

3. Book: 예약 생성
   POST {booking_link}
   → hold: true 옵션으로 임시 홀드 가능

4. Resume (Hold 사용 시):
   → 토큰화된 resume 링크로 확정
   → 10분 내 확정 필요

5. Retrieve: 예약 상세 조회
   → itinerary_id로 예약 확인
```

**Hold and Resume (멱등성 연관):**
- `hold: true` 전송 시 예약을 보류 상태로 생성
- 서버가 토큰화된 resume 링크 발급 (10분 유효)
- resume 링크가 사실상 멱등키 + 세션 토큰 역할
- 만료된 booking link로 재요청 시 503 에러

**멱등성 관련 메커니즘:**
- `affiliate_reference_id`: 파트너 측 고유 참조 ID (중복 방지 용도로 추정)
- booking link 자체가 일회성 토큰으로 기능
- 공식 문서에서 명시적 `Idempotency-Key` 헤더는 확인되지 않음

> **출처:** [Expedia Rapid API - Booking](https://developers.expediagroup.com/docs/products/rapid/lodging/booking), [Booking Hold and Resume](https://developers.expediagroup.com/rapid/lodging/booking/hold-resume)

### 2.4 HotelBeds Booking API

HotelBeds는 2단계 확인 프로세스를 명시적으로 채택한다.

**예약 흐름:**

```
1. /hotels: 객실 가용성 조회 → rateKey 반환

2. /checkrates: rateType이 "recheck"인 경우 가격 재확인
   → 최신 가용성/가격 검증

3. /bookings: 예약 확정
   → rateKey + clientReference + 게스트/결제 정보
```

**멱등성 관련 요소:**
- `rateKey`: 가용성 조회 시 반환되는 고유 키, 예약 확정 시 필수
- `clientReference`: 클라이언트 측 고유 참조 번호 (멱등키 역할 가능)
- 2단계 확인으로 가격/가용성 변동 방지

> **출처:** [HotelBeds Booking API](https://developer.hotelbeds.com/documentation/hotels/booking-api/)

### 2.5 Google Standard Payments (참고: 결제 도메인)

Google의 결제 통합 API는 `requestId`를 멱등키로 사용하는 명확한 패턴을 제시한다.

**구현 방식:**
- 모든 메서드의 RequestHeader에 `requestId` 포함 (전역 고유)
- `requestId` + `paymentIntegratorAccountId` 조합이 멱등키
- 비터미널 응답(HTTP 200 외)은 멱등 처리하지 않음
- 멱등 응답은 `responseTimestamp`만 다르고 나머지는 동일

> **출처:** [Google Standard Payments - Protocol Standards](https://developers.google.com/standard-payments/guides/connectivity/protocol-standards)

---

## 3. 패턴 비교

### 3.1 멱등키 생성 주체별 비교

| 패턴 | 멱등키 생성 | 전달 방식 | 장점 | 단점 |
|------|-----------|----------|------|------|
| **Stripe 스타일** (클라이언트 UUID) | 클라이언트 | `Idempotency-Key` HTTP 헤더 | 단순, 표준화, 범용 | 클라이언트 구현 필요, 키 관리 부담 |
| **세션 토큰 방식** (서버 발급) | 서버 | URL 경로 또는 Request Body | 서버 제어, 재고 선점 가능 | 2단계 호출 필요, 복잡도 증가 |
| **하이브리드** (서버 토큰 + 클라이언트 키) | 양쪽 | 토큰은 Body, 멱등키는 헤더 | 이중 보호, 유연 | 구현 복잡도 최대 |
| **requestId 방식** (Google) | 클라이언트 | Request Body 필드 | Body에 포함되어 로깅 용이 | 헤더 분리 안됨, 비표준 |

### 3.2 OTA 플랫폼별 멱등성 전략 비교

| 플랫폼 | 예약 흐름 | 멱등키 메커니즘 | 재고 선점 | 특이사항 |
|--------|----------|---------------|----------|---------|
| **Amadeus** | Search → Offer → Book | offerId (서버 발급 토큰) | offerId 유효기간 내 | 재검증 개발자 위임 |
| **Booking.com** | Pull 폴링 + ACK | response_token + ACK | 해당 없음 (B2B) | 폴링 기반 구조적 멱등 |
| **Expedia** | PriceCheck → Hold → Resume | booking_link + hold 토큰 | 10분 hold | affiliate_reference_id |
| **HotelBeds** | Avail → CheckRate → Book | rateKey + clientReference | checkRate 단계 | 2단계 확인 명시 |
| **Stripe** | 단일 요청 | Idempotency-Key 헤더 | 해당 없음 | 업계 표준, 24시간 TTL |

### 3.3 핵심 인사이트

1. **OTA는 "2단계 프로세스" 자체가 멱등성 전략이다**
   - 서버가 발급한 토큰(offerId, booking_link, rateKey)이 자연스럽게 멱등키 역할
   - 단순 Idempotency-Key 헤더보다 비즈니스 로직에 더 밀접하게 통합됨

2. **결제 도메인은 "단일 요청 + 헤더 기반 멱등키"가 표준**
   - Stripe, Google 등은 각 요청에 고유 키를 부여하는 범용 패턴

3. **재고 선점과 멱등성은 같은 문제의 두 측면**
   - OTA에서 재고 선점(soft lock)은 동시성 제어 + 멱등성 보장을 동시에 해결
   - 세션 만료는 자원 회수 + 키 만료를 동시에 처리

---

## 4. ota-toy 적용 제안

### 4.1 권장 패턴: 하이브리드 (2단계 프로세스 + Idempotency-Key)

ota-toy의 예약 도메인 특성상, OTA 업계의 2단계 프로세스와 Stripe의 Idempotency-Key를 결합하는 하이브리드 방식을 권장한다.

**제안 흐름:**

```
[1단계: 예약 세션 생성]
POST /api/v1/reservations/sessions
Request: { propertyId, roomTypeId, checkIn, checkOut, guestCount }
Response: {
  sessionId: "rsv-sess-uuid",    ← 서버 발급, 멱등키 역할
  totalAmount: 150000,
  expiresAt: "2026-04-06T12:15:00Z",  ← 10분 유효
  inventoryHeld: true              ← Redis 카운터로 임시 차감
}

[2단계: 예약 확정]
POST /api/v1/reservations
Headers: Idempotency-Key: "client-generated-uuid"   ← 이중 보호
Request: { sessionId: "rsv-sess-uuid", guestInfo, paymentInfo }
Response: { reservationId: "RSV-20260406-001", status: "CONFIRMED" }
```

### 4.2 선택 근거

| 판단 기준 | 결정 | 근거 |
|----------|------|------|
| 멱등키 생성 주체 | 1단계: 서버, 2단계: 클라이언트 + 서버 | OTA 표준(서버 토큰) + 결제 표준(클라이언트 키) 결합 |
| 멱등키 전달 방식 | `Idempotency-Key` HTTP 헤더 | IETF 표준 초안 준수, Stripe 호환 |
| 재고 선점 | 1단계에서 Redis 카운터 임시 차감 | ota-toy 기존 설계(Redis 원자적 카운터)와 정합 |
| 키 만료 | 세션: 10분, 멱등키: 24시간 | OTA 업계 관행(5~15분) + Stripe 관행(24시간) |
| 응답 캐싱 | Redis에 멱등키 기반 응답 캐싱 | ota-toy 기존 Redis 인프라 활용 |

### 4.3 구현 설계 (개략)

**서버 측 멱등성 처리:**

```
1. Idempotency-Key 헤더 추출
2. Redis에서 해당 키로 기존 응답 조회
   2-1. 존재 + 완료 → 캐시된 응답 반환
   2-2. 존재 + 처리 중 → 409 Conflict
   2-3. 존재 + 다른 파라미터 → 422 Unprocessable
3. 존재하지 않으면 → 요청 처리 시작
4. 처리 완료 후 응답을 Redis에 저장 (TTL 24시간)
```

**예약 세션 관리:**

```
1. 세션 생성 시 Redis에 세션 데이터 저장 (TTL 10분)
2. 재고 임시 차감 (Redis DECRBY)
3. 세션 만료 시 자동 재고 복구 (Redis TTL 만료 이벤트 또는 Reaper)
4. 확정 시 세션 소비 + DB 기록 + Outbox 이벤트
```

**Brandur 패턴 적용 (Recovery Point):**
- 예약 확정 과정에서 외부 서비스 호출(결제 등)이 있을 경우
- `recovery_point` 컬럼으로 진행 단계 추적
- 실패 시 해당 단계부터 재개 가능

### 4.4 트레이드오프

| 장점 | 단점 |
|------|------|
| 재고 경합 최소화 (세션 단계에서 선점) | 2단계 호출로 API 복잡도 증가 |
| 이중 멱등성 보호 (세션 + 헤더) | 세션 만료 시 재고 복구 로직 필요 |
| IETF 표준 헤더 준수 | 클라이언트가 UUID 생성 로직 필요 |
| 기존 Redis 인프라 재사용 | 세션 GC(Reaper) 프로세스 필요 |
| Outbox 패턴과 자연스럽게 연계 | 초기 구현 비용 증가 |

### 4.5 대안 (단순화 버전)

만약 2단계 프로세스가 과도하다면, Stripe 스타일의 단일 요청 + Idempotency-Key만으로도 충분하다.

```
POST /api/v1/reservations
Headers: Idempotency-Key: "client-uuid"
Request: { propertyId, roomTypeId, checkIn, checkOut, guestInfo, paymentInfo }
```

- 재고 차감과 예약 생성을 단일 트랜잭션으로 처리
- Redis 멱등키 캐시로 중복 방지
- 구현 단순하지만 재고 경합에는 취약

---

## 5. 참고 자료

### API 문서
- [Stripe API - Idempotent Requests](https://docs.stripe.com/api/idempotent_requests)
- [Stripe Blog - Designing robust and predictable APIs with idempotency](https://stripe.com/blog/idempotency)
- [Amadeus Hotel APIs Tutorial](https://developers.amadeus.com/self-service/apis-docs/guides/developer-guides/resources/hotels/)
- [Booking.com - OTA Reservations Process](https://developers.booking.com/connectivity/docs/reservations-api/reservations-process-ota)
- [Expedia Rapid API - Booking](https://developers.expediagroup.com/docs/products/rapid/lodging/booking)
- [HotelBeds Booking API](https://developer.hotelbeds.com/documentation/hotels/booking-api/)
- [Google Standard Payments - Protocol Standards](https://developers.google.com/standard-payments/guides/connectivity/protocol-standards)

### 표준/설계 문서
- [IETF Draft - Idempotency-Key HTTP Header Field (draft-07)](https://www.ietf.org/archive/id/draft-ietf-httpapi-idempotency-key-header-07.html)
- [Brandur - Implementing Stripe-like Idempotency Keys in Postgres](https://brandur.org/idempotency-keys)
- [MDN - Idempotency-Key Header](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Idempotency-Key)

### 시스템 설계 참고
- [System Design - Hotel Booking OTA](https://medium.com/@ankit.vashishta/system-design-hotel-booking-ota-like-booking-com-makemytrip-expedia-airbnb-etc-6e5d26e05d9e)
- [Design Hotel Booking System - Step by Step Guide](https://www.systemdesignhandbook.com/guides/design-hotel-booking-system/)
- [HTTP Toolkit - Working with the new Idempotency Keys RFC](https://httptoolkit.com/blog/idempotency-keys/)
