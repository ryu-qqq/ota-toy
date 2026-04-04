# GitHub Issues 투두리스트

> GitHub Issues #1~#11 분석 결과. 우선순위별로 정리.
> 작성일: 2026-04-04

---

## P0: 아키텍처/컨벤션 위반 (즉시 수정)

- [ ] **ErrorCode에서 `getHttpStatus()` 제거** — 도메인에 HTTP 침투 (#11)
  - ErrorCode 인터페이스에서 getHttpStatus() 제거
  - 7개 BC ErrorCode Enum에서 httpStatus 필드/생성자 제거
  - API 레이어에 ErrorCodeHttpStatusMapper 생성
  - 컨벤션 DOM-ERR-001 갱신 완료
- [ ] **CacheKey/LockKey를 인프라 레이어로 이관** (#11)
  - domain/common/infra/ → adapter-out 또는 application으로 이동
  - 컨벤션 DOM-AGG-015 추가 완료
- [ ] **AccommodationErrorCode에서 Pricing 관련 코드 제거** (#8)
  - RATE_PLAN_NOT_FOUND, INVALID_RATE_RULE 등 pricing BC 코드 정리
  - 컨벤션 DOM-ERR-002 추가 완료

---

## P1: 도메인 모델 구조 개선

- [x] **상태 머신 Enum 분리** (#7, #9)
  - ReservationStatus에 전이 테이블 + transitTo() 구현
  - SupplierStatus에 전이 테이블 구현
  - PartnerStatus에 전이 테이블 구현 (선택)
  - 컨벤션 DOM-ENUM-001 추가 완료
- [x] **CancellationPolicy VO 추출** (#3)
  - RatePlan의 freeCancellation, nonRefundable, deadlineDays, policyText → 1 VO
  - CancellationPolicyText String VO (길이 검증)
  - RatePlan.forNew(), updatePolicy() 시그니처 단순화
- [ ] **Rate 역할 정의** (#4)
  - 캐시 vs 소스 결정 → ADR 작성
  - 결정에 따라 동기화 전략 설계
- [x] **RateOverride 날짜 범위 검증** (#5)
  - forNew()에 RateRule 날짜 범위 전달 또는 RateRule 내부 팩토리
- [x] **RatePlanAddOn included/price 모순 방지** (#5)
  - included=true이면 price는 null/0만 허용
  - included=false이면 price 필수
- [ ] **Location BC 경계 재설계** (#2)
  - Location → accommodation으로 이동 (Property의 VO)
  - Coordinate VO 도입 (위도/경도 중복 검증 제거)
  - neighborhood/region 역할 정의
- [x] **Collection VO Aggregate 포함 여부 결정** (#8) — **포함하지 않음 (결정 완료)**
  - 실제 OTA 조사 결과 사진/편의시설/객실이 별도 생명주기로 독립 관리됨 (`docs/research/ota-extranet-registration-flow.md`)
  - 현재 설계(ID 참조, 별도 Aggregate) 유지
  - 조회용 조립은 Application 레이어에서 PropertyDetail record로 처리

---

## P2: VO 검증 강화

- [x] **String VO 길이 검증 추가** (#1) — 17개 VO에 MAX_LENGTH 적용
- [x] **PhoneNumber/CdnUrl/OriginUrl/Email 검증 강화** (#11, #1)
- [x] **ID VO에 `forNew()` 팩토리 도입** (#1) — 31개 ID VO
- [x] **Money 비교 메서드 추가** (#11) — isGreaterThan, isLessThan, multiply scale 보존. Currency는 미추가 (국내 전용)
- [x] **DateRange.dates() → Stream 반환** (#11)
- [x] **Coordinate VO 도입** (#2) — Location, Landmark에서 위도/경도 중복 제거
- [x] **Supplier raw String VO화** (#9) — SupplierNameKr, CompanyTitle, OwnerName, BusinessNo
- [x] **Brand raw String VO화** (#8) — BrandNameKr, LogoUrl

---

## P3: 비즈니스 로직 보강

- [x] **RatePlan.rename()** (#6) — 이미 적용됨
- [x] **RateRule 가격 업데이트 메서드** (#6) — updatePrices() 추가
- [x] **resolvePrice() Map 기반 성능 개선** (#6) — O(n) → O(1)
- [x] **Inventory restore() 상한 정책** (#10) — 상한 없음 문서화 (호텔 운영 특성)
- [x] **InventoryNotFoundException 생성** (#10)
- [x] **version 낙관적 락 전략 문서화** (#10)
- [x] **SupplierMappingStatus 공통 Enum 분리** (#9) — SupplierPropertyStatus 삭제
- [x] **synced/unmap 상태 검증** (#9) — UNMAPPED에서 synced 차단, unmap 멱등
- [x] **RoomType.updateInfo() baseOccupancy 검증** (#8) — 이미 적용됨

---

## 이미 해결된 항목

| 이슈 | 상태 | 비고 |
|---|---|---|
| Flyway 마이그레이션 (#8, #10) | **해결** | Phase 1에서 DDL 10개 생성 |
| Aggregate 검증 로직 분리 (#1) | **해결** | validate() 메서드로 리팩토링 완료 |
| DeletionStatus 미사용 (#11) | **확인 필요** | soft delete는 Persistence 레이어에서 처리 |

---

## 추가된 컨벤션 규칙

| 규칙 | 문서 | 출처 |
|---|---|---|
| DOM-ERR-001 수정 | domain-convention.md | #11 — HTTP 분리 |
| DOM-ERR-002 신규 | domain-convention.md | #8 — BC별 ErrorCode 분리 |
| DOM-VO-003 신규 | domain-convention.md | #1 — String VO 길이 검증 |
| DOM-VO-004 신규 | domain-convention.md | #1 — ID VO forNew() |
| DOM-ENUM-001 신규 | domain-convention.md | #7, #9 — 상태 머신 Enum |
| DOM-AGG-015 신규 | domain-convention.md | #11 — 인프라 인터페이스 금지 |
