# REST API 레이어 테스트 시나리오

> MockMvc 기반 슬라이스 테스트 + 순수 단위 테스트로 Controller, Mapper, ErrorMapper를 검증한다.  
> 총 **202개 테스트** (Extranet 101개 + Customer 101개)

---

## 테스트 환경

| 항목 | 설정 |
|------|------|
| Controller | `@WebMvcTest` + MockMvc + `@MockBean`(UseCase) |
| Mapper | 순수 Java 단위 테스트 (Spring 없음) |
| ErrorMapper | 순수 Java 단위 테스트 |
| 문서화 | Spring REST Docs (일부 Controller 테스트) |

---

## 검증 카테고리

| 카테고리 | 설명 |
|----------|------|
| **정상 요청** | 유효한 입력 → 기대 HTTP 상태 + 응답 구조 |
| **Validation** | 필수 필드 누락, 경계값, 포맷 오류 → 400 |
| **도메인 예외** | UseCase에서 DomainException → 404/409 |
| **에러 핸들링** | 헤더 누락, Enum 파싱, 날짜 파싱 → 400 |
| **Mapper 변환** | Request DTO → Domain Command/VO 정확성 |
| **ErrorMapper** | ErrorCategory → HttpStatus 매핑 |

---

## Extranet API (101개 테스트)

### Controller 테스트 (41개)

#### ExtranetPropertyCommandController (19개)

**RegisterProperty (6개)**

| # | 시나리오 | 기대 |
|---|---------|------|
| 1 | 유효한 요청 | 201 Created + propertyId |
| 2 | 최소 필수 필드만 | 201 Created |
| 3 | 필수 필드 누락 | 400 VALIDATION_FAILED |
| 4 | 존재하지 않는 파트너 | 404 PTN-001 |
| 5 | name 100자 경계값 | 201 Created |
| 6 | name 101자 경계값 | 400 VALIDATION_FAILED |

**SetPhotos (4개)**

| # | 시나리오 | 기대 |
|---|---------|------|
| 1 | 유효한 요청 | 200 OK |
| 2 | 빈 사진 목록 | 400 (@NotEmpty) |
| 3 | 존재하지 않는 숙소 | 404 ACC-001 |
| 4 | sortOrder 음수 | 400 (@PositiveOrZero) |

**SetAmenities (4개)**

| # | 시나리오 | 기대 |
|---|---------|------|
| 1 | 유효한 요청 | 200 OK |
| 2 | amenities 누락 | 400 |
| 3 | 존재하지 않는 숙소 | 404 ACC-001 |
| 4 | additionalPrice 음수 | 400 (@PositiveOrZero) |

**SetAttributes (5개)**

| # | 시나리오 | 기대 |
|---|---------|------|
| 1 | 유효한 요청 | 200 OK |
| 2 | 빈 속성값 목록 | 400 (@NotEmpty) |
| 3 | 존재하지 않는 숙소 | 404 ACC-001 |
| 4 | 필수 속성 누락 | 400 ACC-006 |
| 5 | value 빈 문자열 | 400 (@NotBlank) |

#### ExtranetPropertyQueryController (5개)

| # | 시나리오 | 기대 |
|---|---------|------|
| 1 | 목록 조회 (커서 페이징) | 200 OK + 리스트 |
| 2 | 빈 결과 | 200 OK + 빈 목록 |
| 3 | partnerId 누락 | 400 필수 파라미터 |
| 4 | 상세 조회 | 200 OK + nested (photos, amenities, attributeValues, roomTypes) |
| 5 | 존재하지 않는 숙소 | 404 ACC-001 |

#### ExtranetRoomTypeCommandController (9개)

| # | 시나리오 | 기대 |
|---|---------|------|
| 1 | 유효한 요청 (beds, views 포함) | 201 Created |
| 2 | 최소 필수 필드만 | 201 Created |
| 3 | 필수 필드 누락 | 400 |
| 4 | 존재하지 않는 숙소 | 404 ACC-001 |
| 5 | baseOccupancy = 0 | 400 (@Positive) |
| 6 | maxOccupancy < baseOccupancy | 400 도메인 예외 |
| 7 | areaSqm 음수 | 400 (@PositiveOrZero) |
| 8 | beds quantity = 0 | 400 (@Positive) |
| 9 | checkInTime "25:00" | 400 INVALID_DATETIME_FORMAT |

#### ExtranetRatePlanCommandController (8개)

**RegisterRatePlan (3개)**

| # | 시나리오 | 기대 |
|---|---------|------|
| 1 | 유효한 요청 | 201 Created |
| 2 | 필수 필드 누락 | 400 |
| 3 | 존재하지 않는 객실유형 | 404 ACC-002 |

**SetRateAndInventory (5개)**

| # | 시나리오 | 기대 |
|---|---------|------|
| 1 | 유효한 요청 (overrides 포함) | 200 OK |
| 2 | 필수 필드 누락 | 400 |
| 3 | 존재하지 않는 요금정책 | 404 PRC-001 |
| 4 | baseInventory 음수 | 400 (@Min) |
| 5 | overrides price = null | 400 (@NotNull) |

---

### Mapper 단위 테스트 (47개)

#### PropertyApiMapper (6개)
- 전체 필드 → Domain VO 변환 (PartnerId, PropertyName, Location 등)
- nullable 필드 (brandId, description, promotionText) → null 전달

#### PropertyPhotoApiMapper (6개)
- 리스트 변환, cdnUrl nullable, PhotoType Enum 변환

#### PropertyAmenityApiMapper (6개)
- AmenityType Enum 변환, Money(additionalPrice) 변환, 리스트 순서 유지

#### PropertyAttributeApiMapper (4개)
- PropertyTypeAttributeId VO 변환, value 매핑, 순서 유지

#### PropertyQueryApiMapper (3개)
- PartnerId 변환, 커서 페이지네이션 파라미터

#### RoomTypeApiMapper (8개)
- 전체 필드 변환, LocalTime.parse (checkInTime/checkOutTime)
- beds/views 리스트 변환, null beds/views → 빈 리스트
- nullable 필드 (description, areaPyeong)

#### RatePlanApiMapper (9개)
- 전체 필드, CancellationPolicy 조합 (무료취소/환불불가/기본)
- nullable 기본값 (freeCancellation false, cancellationDeadlineHours 0)
- PaymentPolicy Enum 변환

#### RateAndInventoryApiMapper (5개)
- 전체 필드, nullable overrides → 빈 리스트, BigDecimal 정밀도

---

### ErrorMapper 테스트 (13개)

#### ExtranetPropertyErrorMapper (13개)
- **supports**: ACC-, PTN-, PT-, PRC-, INV-, RT- 접두사 지원 (ParameterizedTest)
- 미지원 접두사 거부 (USR-, ORD-, UNKNOWN-)
- **map**: ErrorCategory → HttpStatus 매핑
  - NOT_FOUND → 404, VALIDATION → 400, CONFLICT → 409, FORBIDDEN → 422
- 실제 도메인 ErrorCode 기반 검증 (6건)

---

## Customer API (101개 테스트)

### Controller 테스트 (32개)

#### CustomerSearchQueryController (7개)

| # | 시나리오 | 기대 |
|---|---------|------|
| 1 | 유효한 요청 (커서, 지역 필터) | 200 OK + content[], hasNext |
| 2 | 빈 결과 | 200 OK + 빈 목록 |
| 3 | checkIn 누락 | 400 필수 파라미터 |
| 4 | checkOut 누락 | 400 필수 파라미터 |
| 5 | guests=0 → 기본값 1로 보정 | 200 OK |
| 6 | size=0 → 기본값 20으로 보정 | 200 OK |
| 7 | amenityTypes 유효하지 않은 값 | 400 INVALID_ARGUMENT |

#### CustomerRateQueryController (7개)

| # | 시나리오 | 기대 |
|---|---------|------|
| 1 | 유효한 요청 | 200 OK + dailyRates[] |
| 2 | 객실 없음 | 200 OK + 빈 목록 |
| 3 | checkIn 누락 | 400 필수 파라미터 |
| 4 | checkOut 누락 | 400 필수 파라미터 |
| 5 | 존재하지 않는 숙소 | 404 ACC-001 |
| 6 | guests=0 → 기본값 1로 보정 | 200 OK |
| 7 | 체크아웃 < 체크인 | 400 INVALID_ARGUMENT |

#### CustomerReservationCommandController (18개)

**CreateSession (5개)**

| # | 시나리오 | 기대 |
|---|---------|------|
| 1 | 유효한 요청 (Idempotency-Key 헤더) | 201 Created + sessionId, expiresAt |
| 2 | 필수 필드 누락 | 400 |
| 3 | 재고 소진 | 409 INV-002 |
| 4 | Idempotency-Key 헤더 누락 | 400 MISSING_HEADER |
| 5 | guestCount = 0 | 400 (@Min) |
| 6 | checkIn 과거 날짜 | 400 (@Future) |

**ConfirmReservation (7개)**

| # | 시나리오 | 기대 |
|---|---------|------|
| 1 | 유효한 요청 (nested guestInfo, lines, items) | 201 Created |
| 2 | 필수 필드 누락 | 400 |
| 3 | 세션 없음 | 404 RSV-005 |
| 4 | 세션 만료 | 409 RSV-006 |
| 5 | 재고 부족 | 409 INV-002 |
| 6 | guestInfo.name 빈 문자열 | 400 (@NotBlank) |
| 7 | lines 빈 배열 | 400 (@NotEmpty) |
| 8 | lines[].items 빈 배열 | 400 (@NotEmpty + @Valid cascading) |

**CancelReservation (4개)**

| # | 시나리오 | 기대 |
|---|---------|------|
| 1 | 사유 포함 취소 | 200 OK |
| 2 | 사유 없이 취소 (body 없음) | 200 OK |
| 3 | 존재하지 않는 예약 | 404 RSV-001 |
| 4 | 이미 취소된 예약 | 409 RSV-003 |

---

### Mapper 단위 테스트 (48개)

#### SearchPropertyApiMapper (20개)
- **toQuery**: 필수 필드 (checkIn, checkOut, guests), 선택 필드 (region, amenityType, starRating)
- Enum 변환: AmenityType, PropertySortKey, SortDirection
- Money 변환: BigDecimal → Money VO
- null/빈값 선택 필드 처리
- **toApiResponse**: 전체 필드 매핑, null lowestPrice 처리

#### RateApiMapper (10개)
- **toQuery**: PropertyId VO 변환, LocalDate/int 매핑
- **toApiResponse**: roomTypeName, freeCancellation, totalPrice, dailyRates[] 변환
- **toDailyRateResponse**: 정상/재고없음 케이스

#### ReservationApiMapper (18개)
- **toSessionCommand**: 멱등키, ID→VO 변환, 날짜, Money 변환
- **toConfirmCommand**: sessionId/customerId, GuestInfo VO, bookingSnapshot, lines→items 중첩 변환, 다중 라인
- **toCancelCommand**: ReservationId VO, cancelReason null 처리
- **toSessionResponse**: Money→BigDecimal, LocalDate→String, Instant→KST 포맷

---

### ErrorMapper 테스트 (21개)

#### CustomerPropertyErrorMapper (9개)
- **supports**: ACC-, PRC-, RT- 지원 / RSV-, INV- 미지원
- **map**: ErrorCategory → HttpStatus 매핑 (ParameterizedTest)

#### CustomerReservationErrorMapper (12개)
- **supports**: RSV-, INV- 지원 (모든 enum 값 ParameterizedTest) / ACC-, PRC- 미지원
- **map**: ErrorCategory → HttpStatus, 구체 에러코드별 검증 (RSV-001/003/006, INV-002/003/004)
- **mapDetail**: args 유무에 따른 detail 필드 결정

---

## 에러 핸들링 전략

### GlobalExceptionHandler 핸들러 목록

| 예외 | HTTP 상태 | 에러 코드 |
|------|----------|----------|
| `DomainException` | ErrorMapper 결정 (404/400/409/422) | 도메인 에러 코드 |
| `MethodArgumentNotValidException` | 400 | VALIDATION_FAILED |
| `BindException` | 400 | BINDING_FAILED |
| `MissingServletRequestParameterException` | 400 | MISSING_PARAMETER |
| `ServletRequestBindingException` | 400 | MISSING_HEADER |
| `HttpMessageNotReadableException` | 400 | INVALID_FORMAT |
| `MethodArgumentTypeMismatchException` | 400 | TYPE_MISMATCH |
| `IllegalArgumentException` | 400 | INVALID_ARGUMENT |
| `DateTimeParseException` | 400 | INVALID_DATETIME_FORMAT |
| `HttpRequestMethodNotSupportedException` | 405 | METHOD_NOT_ALLOWED |
| `Exception` (fallback) | 500 | INTERNAL_ERROR |

### ErrorMapper 모듈별 접두사

| 모듈 | ErrorMapper | 지원 접두사 |
|------|-----------|-----------|
| Extranet | ExtranetPropertyErrorMapper | ACC-, PTN-, PT-, PRC-, INV-, RT- |
| Customer (숙소) | CustomerPropertyErrorMapper | ACC-, PRC-, RT- |
| Customer (예약) | CustomerReservationErrorMapper | RSV-, INV- |

---

## 테스트 패턴 요약

| 패턴 | 적용 위치 | 설명 |
|------|----------|------|
| **MockMvc + @MockBean** | Controller 테스트 | UseCase Mock, HTTP 요청/응답 검증 |
| **Fixture 클래스** | Controller 테스트 | JSON 요청 본문, 도메인 결과 객체 생성 |
| **ParameterizedTest** | ErrorMapper 테스트 | 모든 에러 코드 접두사 전수 검증 |
| **AssertJ + Record** | Mapper 테스트 | Command/VO 필드 1:1 매핑 검증 |
| **경계값 검증** | Controller 테스트 | 100자/101자, 0/음수 등 Validation 경계 |
| **기본값 보정** | Customer Controller | guests=0→1, size=0→20 (compact constructor) |
