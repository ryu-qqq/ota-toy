# Persistence 레이어 테스트 시나리오

> Testcontainers 기반 통합 테스트로 실제 MySQL/Redis 환경에서 Adapter 동작을 검증한다.

---

## 테스트 환경

### MySQL (persistence-mysql)

| 항목 | 설정 |
|------|------|
| 컨테이너 | MySQL 8.0, Singleton (JVM 수명 동안 1회 기동) |
| 스키마 관리 | Flyway (`ddl-auto=none`) — 운영과 동일한 마이그레이션 |
| 슬라이스 | `@DataJpaTest` + `@Import` (필요 Bean만 명시 주입) |
| 커넥션 | HikariCP `max-pool-size=5`, MySQL `max-connections=300` |
| 문자셋 | utf8mb4 / utf8mb4_unicode_ci |
| 테스트 클래스 | 23개 (190개 테스트) |

### Redis (persistence-redis)

| 항목 | 설정 |
|------|------|
| 컨테이너 | Redis 7.2-alpine, Singleton |
| 클라이언트 | Redisson (StringCodec) |
| 격리 | `@BeforeEach`에서 테스트 키 삭제 |
| 테스트 클래스 | 3개 (23개 테스트) |

---

## 검증 카테고리

| 카테고리 | 설명 |
|----------|------|
| **매핑 정합성** | Domain ↔ Entity/Redis 필드 변환 정확성 |
| **CRUD 동작** | persist, findById, existsById, persistAll |
| **비즈니스 쿼리** | 상태 필터, 날짜 범위, 커스텀 QueryDSL |
| **Soft Delete** | deletedAt 기반 논리 삭제 후 조회 제외 |
| **Nullable** | 선택 필드 null 저장/조회 |
| **도메인 패턴** | diff, 번들, 멱등키, 좀비 세션, 원자적 저장 |
| **동시성** | Lua 원자적 차감, 멀티스레드 경합 |
| **캐시** | MGET/MSET Batch, TTL, BigDecimal 정밀도 |

---

## MySQL 테스트 시나리오

### Accommodation BC

#### Property + PropertyPhoto (9개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 매핑 정합성 — 전체 필드 | id, partnerId, brandId, name, description, location 등 |
| 2 | 매핑 정합성 — nullable 필드 | brandId, description, promotionText = null |
| 3 | persist + findById | 저장 → 조회 왕복 |
| 4 | persistAll | 복수 건 저장 |
| 5 | findById — 미존재 ID | Optional.empty() |
| 6 | existsById — 존재 | true |
| 7 | existsById — 미존재 | false |
| 8 | PropertyPhoto 저장/조회 | photoType, originUrl, cdnUrl, sortOrder |
| 9 | PropertyPhoto 필드 정합성 | 1:N 관계 매핑 |

#### PropertyAmenity (10개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 매핑 정합성 — 전체 필드 | amenityType, name, additionalPrice, sortOrder |
| 2 | 매핑 정합성 — 무료 편의시설 | additionalPrice = 0 |
| 3 | persistAll | 복수 편의시설 저장 |
| 4 | sortOrder 정렬 | 오름차순 조회 |
| 5 | 미존재 propertyId 조회 | 빈 컬렉션 |
| 6 | 빈 리스트 persistAll | 예외 없이 무시 |
| 7 | Soft Delete — 전체 | 삭제 후 조회 제외 |
| 8 | Soft Delete — 부분 | 일부 삭제, 나머지 정상 |
| 9 | **diff 패턴** | 기존 조회 → 새 추가 → 삭제 대상 제거 |
| 10 | AmenityType별 저장/조회 | 다양한 타입 검증 |

#### PropertyAttributeValue (10개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 매핑 정합성 — 전체 필드 | propertyTypeAttributeId, value |
| 2 | 매핑 정합성 — 긴 문자열 | 최대 길이(500) value |
| 3 | persistAll | 복수 속성값 저장 |
| 4 | 미존재 propertyId 조회 | 빈 컬렉션 |
| 5 | 빈 리스트 persistAll | 예외 없이 무시 |
| 6 | attributeId-value 정확성 | 매핑 일치 검증 |
| 7 | Soft Delete — 전체 | 삭제 후 조회 제외 |
| 8 | Soft Delete — 부분 | 일부 삭제, 나머지 정상 |
| 9 | **diff 패턴** | 기존 조회 → 새 추가 → 삭제 대상 제거 |
| 10 | Property 간 격리 | 서로 다른 Property 속성값 분리 조회 |

#### PropertySearch — 크로스 BC 검색 (6개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 기본 조건 검색 | Property + RoomType + Inventory + Rate + RatePlan JOIN |
| 2 | 커서 페이지네이션 | nextCursor(), hasNext() |
| 3 | 인원 필터 | maxOccupancy ≥ 요청 인원 |
| 4 | 무료취소 필터 | CancellationPolicy.freeCancellation = true |
| 5 | 편의시설 필터 | AmenityType (WIFI) 매칭 |
| 6 | 조건 불충족 | 빈 결과 반환 |

#### PropertyType (7개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | existsById — 존재 | true |
| 2 | existsById — 미존재 | false |
| 3 | findById — 정상 조회 | code, name 매핑 |
| 4 | findById — 미존재 | Optional.empty() |
| 5 | 속성 목록 조회 | sortOrder 오름차순 정렬 |
| 6 | 속성 없는 PropertyType | 빈 리스트 |
| 7 | Soft Delete 필터 | deletedAt ≠ null → 조회 제외 |

#### RoomType (6개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 매핑 정합성 | areaSqm, baseOccupancy, maxOccupancy 등 전체 필드 |
| 2 | persist + findById | 저장 → 조회 |
| 3 | findById — 미존재 | Optional.empty() |
| 4 | existsById | true / false |
| 5 | findByPropertyId | 해당 숙소 전체 객실 |
| 6 | findActiveByPropertyIdAndMinOccupancy | ACTIVE + 인원 조건 필터 |

#### RoomTypeBed (8개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 매핑 정합성 — Adapter 경유 | bedType, bedCount 필드 |
| 2 | 매핑 정합성 — EntityMapper | toDomain() 변환 |
| 3 | persistAll — 단일 | 1개 침대 구성 |
| 4 | persistAll — 복수 | 다중 침대 구성 |
| 5 | persistAll — 빈 리스트 | 예외 없음 |
| 6 | **번들 패턴** — RoomType + Bed | RoomType 저장 후 ID로 Bed 연결 |
| 7 | **번들 패턴** — forPending → withRoomTypeId | Pending 리스트에 ID 할당 후 일괄 저장 |
| 8 | Flyway 마이그레이션 | room_type_bed 테이블 생성 |

#### RoomTypeView (8개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 매핑 정합성 — Adapter 경유 | viewType 필드 |
| 2 | 매핑 정합성 — EntityMapper | toDomain() 변환 |
| 3 | persistAll — 단일 | 1개 전망 |
| 4 | persistAll — 복수 | 다중 전망 |
| 5 | persistAll — 빈 리스트 | 예외 없음 |
| 6 | **번들 패턴** — RoomType + View | RoomType 저장 후 ID로 View 연결 |
| 7 | **번들 패턴** — forPending → withRoomTypeId | Pending 리스트에 ID 할당 후 일괄 저장 |
| 8 | Flyway 마이그레이션 | room_type_view 테이블 생성 |

---

### Pricing BC

#### RatePlan (10개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 매핑 정합성 — DIRECT | supplierId = null, CancellationPolicy, PaymentPolicy |
| 2 | 매핑 정합성 — SUPPLIER | supplierId ≠ null, 환불불가 정책 |
| 3 | persist + findById | 저장 → 조회 |
| 4 | findById — 미존재 | Optional.empty() |
| 5 | findByRoomTypeIds | 복수 객실유형 쿼리 |
| 6 | existsById — 존재 | true |
| 7 | existsById — 미존재 | false |
| 8 | Flyway 마이그레이션 | rate_plan 테이블 생성 |
| 9 | nullable — supplierId | DIRECT (null) / SUPPLIER (값) |
| 10 | nullable — cancellationPolicyText | null 허용 |

#### Rate (3개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 날짜 범위 조회 | startDate 포함, endDate 미포함 |
| 2 | 미존재 RatePlanId | 빈 리스트 |
| 3 | 필드 정합성 | id, ratePlanId, rateDate, basePrice (BigDecimal) |

#### RateRule (7개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 매핑 정합성 — 전체 요일가격 | 월~일 7개 가격 + basePrice |
| 2 | 매핑 정합성 — null 요일가격 | basePrice만 존재 |
| 3 | persist + ID 반환 | JpaRepository 조회 검증 |
| 4 | 동일 RatePlan 복수 저장 | 다중 Rule 저장 |
| 5 | BigDecimal 정밀도 — 소수점 | isEqualByComparingTo() |
| 6 | BigDecimal 정밀도 — 0원 | BigDecimal.ZERO |
| 7 | Flyway 마이그레이션 | rate_rule 테이블 생성 |

#### RateOverride (8개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 매핑 정합성 — 전체 필드 | rateRuleId, overrideDate, overridePrice, reason |
| 2 | 매핑 정합성 — null reason | reason = null 허용 |
| 3 | persistAll — 다건 | 여러 Override 일괄 저장 |
| 4 | persistAll — 빈 리스트 | 에러 없음 |
| 5 | BigDecimal 정밀도 — 소수점 | isEqualByComparingTo() |
| 6 | BigDecimal 정밀도 — 0원 | BigDecimal.ZERO |
| 7 | Flyway 마이그레이션 | rate_override 테이블 생성 |
| 8 | rateRuleId 기반 필터링 | 다른 RateRule의 Override 분리 |

---

### Inventory BC (4개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 날짜 범위 조회 | startDate 포함, endDate 미포함 |
| 2 | 미존재 RoomTypeId | 빈 리스트 |
| 3 | 필드 정합성 | id, roomTypeId, inventoryDate, totalInventory, availableCount, isStopSell |
| 4 | 다중 RoomTypeId 조회 | 복수 ID 동시 검색 |

---

### Reservation BC

#### ReservationSession (14개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 매핑 정합성 — PENDING | 세션 전체 필드 |
| 2 | 매핑 정합성 — CONFIRMED | reservationId 매핑 |
| 3 | persist + findById | 저장 → 조회 |
| 4 | findById — 미존재 | Optional.empty() |
| 5 | 상태 변경 후 persist | 업데이트 반영 |
| 6 | **멱등키 조회** — 존재 | idempotencyKey 기반 검색 |
| 7 | **멱등키 조회** — 미존재 | Optional.empty() |
| 8 | **findPendingBefore** — 만료 세션 | cutoff 시각 기준 PENDING 조회 |
| 9 | **findPendingBefore** — CONFIRMED 제외 | 확정 세션 필터링 |
| 10 | reservationId 조회 — 존재 | CONFIRMED 세션 검색 |
| 11 | reservationId 조회 — 미존재 | Optional.empty() |
| 12 | Flyway 마이그레이션 | reservation_session 테이블 생성 |
| 13 | nullable — reservationId null | PENDING (null) |
| 14 | nullable — reservationId 존재 | CONFIRMED (값) |

#### Reservation + ReservationLine + ReservationItem (16개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 매핑 정합성 — PENDING | Reservation 전체 필드 |
| 2 | 매핑 정합성 — 다객실 | 복수 Line/Item 조합 |
| 3 | persist + findById | 저장 → 조회 |
| 4 | findById — 미존재 | Optional.empty() |
| 5 | reservationNo unique | unique 제약 검증 |
| 6 | 상태: CONFIRMED | 정상 저장/조회 |
| 7 | 상태: CANCELLED | cancelReason, cancelledAt 포함 |
| 8 | 상태: COMPLETED | 정상 저장/조회 |
| 9 | 상태: NO_SHOW | 정상 저장/조회 |
| 10 | Flyway 마이그레이션 | reservation + line + item 3개 테이블 |
| 11 | nullable — guestEmail | null 허용 |
| 12 | nullable — bookingSnapshot | null 허용 |
| 13 | nullable — cancelReason | PENDING에서 null |
| 14 | nullable — cancelledAt | PENDING에서 null |
| 15 | **원자적 저장** — Line + Item | persist 시 연관 엔티티 함께 저장 |
| 16 | **원자적 저장** — 다객실 | 복수 Line + 각 Line의 Item 전부 저장 |

---

### Partner BC (5개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | existsById — 존재 | true |
| 2 | existsById — 미존재 | false |
| 3 | findById — 정상 조회 | Domain 객체 변환, 필드 매핑 |
| 4 | findById — 미존재 | Optional.empty() |
| 5 | Soft Delete 필터 | deletedAt ≠ null → 조회 제외 |

---

### Supplier BC

#### Supplier (6개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 매핑 정합성 | 전체 필드 (name, type, status 등) |
| 2 | findByStatus — ACTIVE | 상태별 필터링 |
| 3 | findByStatus — 빈 결과 | 해당 상태 없으면 빈 리스트 |
| 4 | Soft Delete | 삭제된 Supplier 조회 제외 |
| 5 | Flyway 마이그레이션 | supplier 테이블 생성 |
| 6 | nullable 필드 | phone, email, termsUrl = null |

#### SupplierApiConfig (7개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 매핑 정합성 | baseUrl, authType, cronExpression 등 |
| 2 | findAllActive — ACTIVE | 활성 설정만 조회 |
| 3 | findAllActive — 빈 결과 | ACTIVE 없으면 빈 리스트 |
| 4 | findBySupplierId — 존재 | Optional 값 |
| 5 | findBySupplierId — 미존재 | Optional.empty() |
| 6 | Soft Delete — findBySupplierId | 삭제 제외 |
| 7 | Soft Delete — findAllActive | 삭제 제외 |

#### SupplierProperty (6개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 매핑 정합성 | supplierId, propertyId, externalId 등 |
| 2 | persist + findBySupplierId | 저장 → 조회 |
| 3 | Supplier 간 격리 | 다른 supplierId 데이터 분리 |
| 4 | 빈 결과 | 매핑 없으면 빈 리스트 |
| 5 | Soft Delete | 삭제된 매핑 조회 제외 |
| 6 | nullable — lastSyncedAt | null / 값 둘 다 검증 |

#### SupplierRawData (7개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 매핑 정합성 | supplierId, rawPayload, status, fetchedAt |
| 2 | findBySupplierIdAndStatus — FETCHED | 상태 필터 |
| 3 | 상태 격리 — SYNCED ≠ FETCHED | 다른 상태 조회 안 됨 |
| 4 | 빈 결과 | 해당 상태 없으면 빈 리스트 |
| 5 | 혼합 상태 필터 | FETCHED / PROCESSING / SYNCED 정확 분리 |
| 6 | nullable — processedAt null | FETCHED 상태 |
| 7 | nullable — processedAt 존재 | SYNCED 상태 |

#### SupplierTask — Outbox 수집 작업 (15개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 매핑 정합성 — PENDING | supplierId, supplierApiConfigId, taskType, status, payload, retryCount, maxRetries |
| 2 | 매핑 정합성 — COMPLETED | processedAt 매핑 |
| 3 | 매핑 정합성 — FAILED | failureReason, retryCount 증가분 |
| 4 | persist + findById | 저장 → 조회 왕복 |
| 5 | persistAll | 복수 건 일괄 저장 |
| 6 | 상태 변경 후 persist | PENDING → PROCESSING → COMPLETED 반영 |
| 7 | findByStatus — PENDING | PENDING만 조회, limit 제한 |
| 8 | findByStatus — PROCESSING | PROCESSING만 조회 |
| 9 | findByStatus — 빈 결과 | 해당 상태 없으면 빈 리스트 |
| 10 | findByStatus — limit 초과 | limit보다 많아도 limit만큼만 반환 |
| 11 | findFailedRetryable | FAILED + retryCount < maxRetries만 조회 |
| 12 | findFailedRetryable — 재시도 소진 | retryCount ≥ maxRetries 제외 |
| 13 | findFailedRetryable — COMPLETED 제외 | FAILED 아닌 상태 제외 |
| 14 | findFailedRetryable — 빈 결과 | 재시도 가능 건 없으면 빈 리스트 |
| 15 | nullable — payload, failureReason | null 허용 |

#### SupplierRoomType (7개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 매핑 정합성 | supplierPropertyId, roomTypeId, supplierRoomCode, status |
| 2 | findBySupplierId — 정상 | supplierId의 Property → RoomType 매핑 조회 |
| 3 | findBySupplierId — 다중 Property | 여러 Property에 걸친 RoomType 전부 반환 |
| 4 | findBySupplierId — 빈 결과 | 해당 Supplier의 Property 없으면 빈 리스트 |
| 5 | findBySupplierId — RoomType 없음 | Property는 있지만 RoomType 없으면 빈 리스트 |
| 6 | Supplier 간 격리 | 다른 supplierId 데이터 분리 |
| 7 | nullable — lastSyncedAt | null / 값 둘 다 검증 |

#### SupplierSyncLog (9개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | 매핑 정합성 — 성공 로그 | syncType, status, syncedAt |
| 2 | 매핑 정합성 — 실패 로그 | errorMessage 매핑 |
| 3 | findLastSuccessBySupplierId | 최신 성공 로그 조회 |
| 4 | 성공 로그 없음 | Optional.empty() |
| 5 | 미존재 supplierId | Optional.empty() |
| 6 | syncType별 필터 | FETCH / PROCESS 타입 독립 조회 |
| 7 | syncType 없는 경우 | Optional.empty() |
| 8 | SUCCESS만 반환 | FAILED 로그 제외 |
| 9 | Flyway 마이그레이션 | supplier_sync_log 테이블 생성 |

---

## Redis 테스트 시나리오

### Inventory — Lua 스크립트 원자적 재고 연산 (8개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | initializeStock — 다중 날짜 | 각 키에 값 SET |
| 2 | initializeStock — 덮어쓰기 | 기존 값 → 새 값 |
| 3 | decrementStock — 정상 차감 | 모든 날짜 ≥ 1일 때 각 -1 |
| 4 | decrementStock — 재고 소진 | 0인 날짜 존재 → InventoryExhaustedException + **전체 롤백** |
| 5 | decrementStock — 경계값 | 1 → 0 정상 성공 |
| 6 | decrementStock — 연속 차감 | 0 → -1 시도 시 예외 + 롤백 |
| 7 | incrementStock — 복구 | 차감 후 복구 → 원래 값 |
| 8 | incrementStock — 재차감 | 복구 후 다시 차감 가능 |

### Inventory — 동시성 테스트 (3개 테스트)

> **과제 핵심 요구사항**: "동일한 재고에 대해 동시 예약 요청이 발생할 수 있는 상황을 처리해야 한다."

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | **재고 1개 + 10 동시 요청** | 정확히 1개 성공, 9개 InventoryExhaustedException, 최종 재고 0 |
| 2 | **재고 5개 + 10 동시 요청** | 정확히 5개 성공, 5개 실패, 최종 재고 0 |
| 3 | **차감 + 복구 혼합** | 5번 차감 + 3번 복구 동시 → 최종 재고 정합성 (10 - 5 + 3 = 8) |

**동시성 보장 원리**: Redis Lua 스크립트는 단일 스레드에서 원자적으로 실행된다. `inventory_decrement.lua`가 다중 날짜를 DECRBY 후 하나라도 음수면 전체 INCRBY로 복구하므로, 오버셀링이 원천 차단된다.

### Rate 캐시 — Batch MGET/MSET (12개 테스트)

| # | 시나리오 | 검증 내용 |
|---|---------|----------|
| 1 | multiSet → multiGet | 저장 후 조회 일치 |
| 2 | 다중 ratePlanId × 날짜 | 교차 조합 저장/조회 |
| 3 | 캐시 미스 — 전체 | 미존재 키 → 빈 Map |
| 4 | 캐시 미스 — 부분 | 일부만 존재 → 존재하는 것만 반환 |
| 5 | 빈 입력 — ratePlanIds | 빈 리스트 → 빈 Map |
| 6 | 빈 입력 — dates | 빈 리스트 → 빈 Map |
| 7 | 빈 입력 — null | null → 빈 Map |
| 8 | 빈 입력 — multiSet 빈 Map | 예외 없이 무시 |
| 9 | 빈 입력 — multiSet null | 예외 없이 무시 |
| 10 | BigDecimal 정밀도 — 소수점 | 99999.99 저장/복원 |
| 11 | BigDecimal 정밀도 — 0원 | BigDecimal.ZERO |
| 12 | TTL 검증 | 저장 후 remainTimeToLive > 0 |

---

## 도메인 패턴별 검증 요약

| 패턴 | 검증 위치 | 의미 |
|------|----------|------|
| **diff 패턴** | PropertyAmenity, PropertyAttributeValue | 기존 조회 → added 저장 + removed soft delete |
| **번들 패턴** | RoomTypeBed, RoomTypeView | forPending → withRoomTypeId → persistAll |
| **원자적 저장** | Reservation | Reservation + Line + Item 연쇄 persist |
| **멱등키** | ReservationSession | idempotencyKey 기반 중복 방지 |
| **좀비 세션** | ReservationSession | findPendingBefore(cutoff) 만료 세션 조회 |
| **상태 전이** | ReservationSession, Reservation | PENDING → CONFIRMED → CANCELLED |
| **크로스 BC 검색** | PropertySearch | 5개 BC JOIN + 커서 페이지네이션 |
| **Lua 원자적 차감** | Inventory Redis | DECRBY → 음수 체크 → 전체 INCRBY 롤백 |
| **2중 동시성 게이트키퍼** | Inventory Redis + DB | Redis 1차 차단 + DB WHERE available ≥ 1 최종 보장 |
| **Outbox Task** | SupplierTask | PENDING → PROCESSING → COMPLETED/FAILED 상태 전이 + 재시도 가능 조회 |
| **Supplier 매핑 체인** | SupplierRoomType | SupplierId → SupplierProperty → SupplierRoomType 2단 조인 |
| **Soft Delete** | Partner, PropertyType, Amenity, AttrValue, Supplier 계열 | deletedAt 기반 논리 삭제 |
