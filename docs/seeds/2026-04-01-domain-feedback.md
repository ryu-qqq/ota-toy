# 도메인 설계 피드백 시드 — 2026-04-01

## Property 도메인 피드백

### FB-1: Brand 정규화
- **문제**: brand가 Property의 단순 string 필드로 박혀있음
- **피드백**: Brand를 별도 엔티티로 분리. Brand가 공급자(Supplier) 또는 판매자 역할을 할 수 있으므로 관계 설계 필요
- **방향**: Brand 엔티티 → Property와 N:1 관계. Brand-Supplier 관계도 검토

### FB-2: 성급(star_rating)은 Property Type에 종속
- **문제**: star_rating이 Property에 직접 박혀있는데, 모텔/에어비엔비/펜션에는 성급 개념 자체가 없음
- **피드백**: 커머스의 "고시정보(노티스 카테고리)"처럼, Property Type별로 필요한 메타 정보가 다름. 타입별 메타 카테고리 구조 필요
- **방향**: PropertyType별 메타 스키마를 동적으로 관리하는 구조 검토 (예: PropertyTypeMeta 또는 PropertyAttribute 패턴)

### FB-3: 위치 필드 과도하게 구체적
- **문제**: nearest_station, walking_minutes, distance_from_center가 Property에 직접 박혀있음. 너무 specific
- **피드백**: Location을 별도 엔티티로 분리하고, 역/랜드마크/도심 거리 등은 매핑 테이블로 관리
- **방향**: Property → Location (1:1 또는 Value Object), LocationLandmark 매핑 테이블

### FB-4: Status 단순화
- **현재**: ACTIVE, INACTIVE 2개
- **검토 필요**: 실제 운영에서는 PENDING_REVIEW(등록 심사중), SUSPENDED(일시 정지), CLOSED(폐업) 등 더 세분화될 수 있음. 단, 선택과 집중 관점에서 일단 심플하게 가는 것도 방법

## RoomType 도메인 피드백

### FB-6: 층 정보 누락
- **문제**: 층 관련 필드가 없음. 크롤링에서 "고층 객실" 태그 발견 (해외 OTA)
- **피드백**: 구체적 층수가 아닌 "고층/저층" 수준. 뷰타입과 연관 있지만 별개 개념 (같은 시티뷰인데 고층/저층 가능)
- **방향**: RoomType의 부가 속성으로 관리. 타입별 메타 패턴에 포함 가능

### FB-7: 추가 인원 정책 부재
- **문제**: max_occupancy 하나로 퉁침. 실제로는 더 복잡
- **실제 데이터**: 해외 OTA "최대 투숙 인원: 2" + 엑스트라 베드, sample.json에 extraBed 필드, 해외 OTA B에 "아동 무료 투숙 가능" 필터
- **필요한 것**: 기본 인원, 최대 인원, 추가 인원 가능 여부, 추가 인원 요금, 엑스트라 베드 가능 여부, 아동 무료 투숙 기준
- **방향**: RoomType에 다 때려박으면 컬럼 폭발. 객실 부가 정보(층, 추가 인원 정책, 엑스트라 베드)를 별도 테이블로 분리 — Property의 타입별 메타 카테고리 패턴과 동일 접근

### FB-8: bed_type, view_type 정규화
- **문제**: string으로 때려박음
- **방향**: enum 또는 별도 코드 테이블로 정규화 검토

## RatePlan 도메인 피드백

### FB-9: 조식을 RatePlan 속성이 아닌 Add-on으로 분리
- **문제**: 현재 breakfast_included, breakfast_price가 RatePlan에 직접 박혀있음. 조식 포함/미포함이 별도 RatePlan 행으로 존재 → 조합 폭발 (취소정책 × 조식 × 결제방식)
- **실제 데이터**: 해외 OTA에서 같은 객실에 조식 유무만 다른 행이 별도 RatePlan으로 존재 (₩356,400 vs ₩423,500)
- **피드백**: 커머스의 상품-옵션 구조처럼, RatePlan은 핵심 정책(취소+결제)만 담고, 조식/패키지/시티투어 등은 Add-on(부가 옵션)으로 분리. 조식 외에도 다른 Add-on이 생길 수 있음
- **방향**: RatePlanAddOn 또는 RatePlanOption 엔티티 분리. RatePlan : AddOn = 1 : N

## Rate 도메인 피드백

### FB-10: rate_date 365일 행 구조 비현실적
- **문제**: 날짜별로 Rate 행이 존재하는 구조 — 365일치가 미리 있는 건 아님
- **피드백**: 실제로는 파트너가 기간+요일 단위로 요금 설정. "4/1~4/30 평일 10만, 주말 15만" 같은 규칙 기반
- **방향**: RateRule(요금 규칙) 엔티티 도입. 기간, 요일별 가격, 특정일 오버라이드. 특정 날짜 요금은 규칙에서 계산

### FB-11: tax_and_fees는 Rate가 아닌 결제/정산 레이어
- **문제**: 세금/수수료가 Rate에 박혀있음. 실제로는 나라별 세율, 결제 수단별 수수료, 카드사 프로모션 등 변수가 많음
- **피드백**: Rate는 순수 객실가(base_price)만 보유. 세금/수수료는 결제 시점에 계산
- **방향**: Rate에서 tax_and_fees, total_price 제거. 결제/정산 도메인에서 처리

### FB-12: 할인/쿠폰/멤버 가격 분리
- **문제**: original_price, discount_rate, member_price가 Rate에 때려박혀 있음
- **피드백**: 가격 정책(PricingPolicy) 레이어로 분리. Rate는 base_price만, 할인/쿠폰/멤버십은 별도 레이어
- **방향**: Rate는 base_price만. PricingPolicy 또는 Promotion 도메인에서 할인 규칙 관리 → 최종가 계산

## Inventory 도메인 피드백

### FB-13: 판매 재고 vs 물리적 객실 구분
- **핵심**: OTA는 "판매 재고"만 관리. 물리적 객실 배정(301호, 302호)은 호텔 PMS 영역 → 스코프 밖
- **근거**: 크롤링에서 "객실 랜덤 배정" 표시 발견. 고객은 RoomType을 예약하지 호수를 예약하지 않음

### FB-14: 재고 관리 — Redis 원자적 카운터 + 임시 홀드 패턴
- **문제 (v1)**: DB 날짜별 행 + 비관적 락 → 행이 없으면 락 불가, 대규모 동시 요청에 DB 부하
- **검토한 대안**:
  - (A) 미리 행 생성 + SELECT FOR UPDATE — 동작은 하지만 DB 락 경합이 병목
  - (B) INSERT 후 락 — UNIQUE 제약 + 재시도 로직 필요, 복잡
  - (C) Redis 원자적 카운터 ✅ 선택 — 락 불필요, 초당 10만+ 처리
- **최종 방향**: 
  - Redis: `inventory:{roomTypeId}:{date}` 카운터. DECR로 원자적 차감
  - 임시 홀드: `hold:{reservationId}` TTL 10분. 결제 완료 전까지 임시 확보
  - DB Inventory: 원본 기록. 확정된 예약만 DB에 반영
  - 초기화: 파트너 RateRule 설정 시 Redis 카운터도 세팅
  - 정합성: DB-Redis 간 주기적 배치 체크
- **흐름**: Redis DECR → 홀드 생성 → 결제 → DB 기록 / 타임아웃 → INCR 복구

## Reservation 도메인 피드백

### FB-15: 예약 변경은 전체 취소 후 재예약
- **문제**: 부분 취소/변경 지원 여부
- **피드백**: 실제 OTA들도 전체 취소 → 재예약 방식. 부분 수정은 재고 트랜잭션이 꼬임. 크롤링에서도 "변경" 버튼 없이 "취소"만 존재
- **방향**: 변경 = CANCELLED → 새 PENDING. 부분 취소 미지원

### FB-16: 스냅샷은 JSON으로 실용적 접근
- **문제**: 예약 후 원본(Property, RoomType, RatePlan)이 변경되면 고객 마이페이지 정보가 달라짐
- **피드백**: 각 도메인별 스냅샷 테이블 복제는 과잉. 실제로 마이페이지에 필요한 건 일부 필드뿐. JSON 스냅샷이 실용적
- **방향**: Reservation에 booking_snapshot JSON 필드. 예약 생성 시점에 마이페이지 노출용 정보 저장. rate_plan_id FK는 원본 참조로 유지 (관리자 조회, 통계용)

### FB-17: ReservationItem 역할 축소
- **피드백**: daily_price는 booking_snapshot에 포함 가능. ReservationItem은 순수하게 재고 차감 추적 용도로만 사용
- **방향**: ReservationItem = stay_date + inventory_id (재고 차감 매핑). 가격 정보는 snapshot으로 이동

## Partner 도메인 피드백

### FB-18: Partner 하위에 멤버(직원) 구조 필요
- **문제**: Partner가 1명짜리 엔티티. 실제로는 업체(Partner) 하위에 여러 직원(PartnerMember)이 존재
- **방향**: Partner(업체) 1 : N PartnerMember(직원). 각 직원이 Extranet에 로그인하여 숙소 관리

### FB-19: 사업자/정산/계약/권한 정보는 현재 스코프 밖
- **피드백**: 사업자등록번호, 정산 계좌, 수수료율, 계약 기간, 숙소별 권한 등은 실제로 필요하지만 핵심 흐름(등록→검색→예약→취소)에 직접 관여하지 않음
- **방향**: 설계 문서에는 언급하되, 구현은 최소한으로 (Partner + PartnerMember 정도)

## Supplier 도메인 피드백

### FB-20: Supplier 구조 이해 — B2B 도매 모델
- **핵심**: Supplier는 다른 플랫폼(익스피디아, 아고다 등)이 도매로 방을 공급하는 구조. 같은 호텔이 여러 공급자에 동시에 방을 풀고, 우리 플랫폼이 이를 합쳐서 최저가를 고객에게 보여줌

### FB-21: 법적 정보 Supplier에 직접 보유
- **변경**: 별도 BusinessInfo 테이블 불필요. Supplier에 법적 정보 직접 포함

### FB-22: Supplier 구조 전면 재설계
- **변경 사항**:
  - SupplierPropertyMapping → SupplierProperty + SupplierRoomType 계층 구조
  - SupplierApiConfig 분리 (연동 메타)
  - SupplierSyncLog 추가 (동기화 기록)
  - RatePlan에 source_type + supplier_id 추가 (가격 출처 식별)

### FB-26: 가격 출처를 RatePlan 레벨에서 관리
- **문제**: Property에 source_type/supplier_id를 두면 내부 도메인이 오염됨
- **피드백**: Property/RoomType은 순수 내부 도메인. 출처 구분은 RatePlan에서 — source_type(DIRECT/SUPPLIER) + supplier_id
- **효과**: 같은 Property에 직접 입점 가격 + Supplier 가격이 공존 가능. 가격 경쟁이 RatePlan 레벨에서 일어남

### FB-27: Supplier 동기화 — Diff 패턴
- **키**: supplier_id + supplier_property_id (숙소), supplier_property_id + supplier_room_id (객실), rate_plan_id + rate_date (가격)
- **방식**: 전체 삭제 후 재등록이 아닌, 기존 대비 INSERT/UPDATE/DELETE Diff
- **이유**: 갱신 중 데이터 공백 방지
- **기록**: SupplierSyncLog로 동기화 이력 추적

### FB-28: 직접 입점 전환 시나리오
- **상황**: Supplier로 노출되던 숙소가 직접 입점하는 경우
- **처리**: Property는 그대로 유지. 파트너 연결 + DIRECT RatePlan 생성. Supplier RatePlan은 유지 또는 비활성화. 가격 경쟁 가능

### FB-29: DIRECT vs SUPPLIER 가격 갱신 방식 차이
- **DIRECT**: 파트너가 수동 설정 → RateRule → Rate 계산
- **SUPPLIER**: 스케줄러가 주기적 API 호출 → Rate 직접 저장 (RateRule 없음)

## Amenity & Photo 피드백

### FB-23: 편의시설에 추가요금 필드 필요
- **근거**: 해외 OTA B에서 "조식 ₩23,000", "사우나 무료 이용" 등 유료/무료가 섞여있음
- **방향**: Amenity에 additional_price (nullable) 필드 추가. null이면 무료

### FB-24: Photo 테이블 분리
- **문제**: property_id, room_type_id 둘 다 nullable FK → 둘 다 null인 행이 가능, 데이터 무결성 깨짐
- **방향**: PropertyPhoto, RoomPhoto로 테이블 분리. 각각 non-null FK

### FB-25: Photo에 원본/CDN URL 분리
- **문제**: url 하나만 있음
- **방향**: origin_url (원본 업로드), cdn_url (리사이즈/CDN) 분리. 또는 이미지 서비스가 별도라면 image_id로 참조

---

## 공통 피드백

### FB-5: 전반적 정규화 부족
- **문제**: Property 테이블에 너무 많은 컬럼이 직접 들어가 있음
- **방향**: Brand, Location, PropertyTypeMeta 등을 분리하여 정규화
