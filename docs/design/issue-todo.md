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

- [ ] **String VO 길이 검증 추가** (#1) — 컨벤션 DOM-VO-003 추가 완료
  - PartnerName(200), MemberName(100), RatePlanName(200), AddOnName(200)
  - SupplierName(200), RoomTypeName(?), PropertyTypeName(?), AmenityName(?)
  - BedTypeName(?), ViewTypeName(?), LandmarkName(200)
  - RoomTypeDescription(?), Email(200)
- [ ] **PhoneNumber/CdnUrl/OriginUrl/Email 검증 강화** (#11, #1)
  - PhoneNumber: null/blank + 길이(30) + 형식
  - CdnUrl: nullable 처리, URL 형식
  - OriginUrl: URL 형식 추가
  - Email: RFC 준수 강화
- [ ] **ID VO에 `forNew()` 팩토리 도입** (#1) — 컨벤션 DOM-VO-004 추가 완료
  - 31개 ID VO에 forNew() 추가
- [ ] **Money에 Currency 추가 + 비교 메서드** (#11)
  - Currency 필드 추가 여부 결정 (국내 전용이면 불필요)
  - isGreaterThan, isLessThan 메서드 추가
  - multiply() scale 보존
- [ ] **DateRange.dates() → Stream 반환** (#11)
  - 장기 범위 메모리 방지
- [ ] **Coordinate VO** (#2)
  - latitude, longitude를 하나의 VO로 (Location, Landmark 중복 제거)
- [ ] **Supplier raw String VO화** (#9)
  - nameKr, companyTitle, ownerName, businessNo, address, termsUrl
- [ ] **Brand raw String VO화** (#8)
  - nameKr, logoUrl

---

## P3: 비즈니스 로직 보강

- [ ] **RatePlan.rename()** (#6)
- [ ] **RateRule 가격 업데이트 메서드** (#6)
- [ ] **resolvePrice() Map 기반 성능 개선** (#6)
- [ ] **Inventory restore() 상한 정책 결정** (#10)
- [ ] **InventoryNotFoundException 생성** (#10)
- [ ] **version 낙관적 락 전략 문서화** (#10)
- [ ] **SupplierMappingStatus 공통 Enum 분리** (#9)
- [ ] **synced/unmap 상태 검증 + 코드 중복 해소** (#9)
- [ ] **RoomType.updateInfo() baseOccupancy 검증 추가** (#8)

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
