# 도메인 설계 근거 문서

> OTA 숙박 플랫폼의 도메인 모델을 **왜 이렇게 설계했는지**에 대한 근거 문서.
> 리서치 데이터, 설계 피드백, 의사결정 기록을 종합한다.

---

## 1. 도메인 리서치 방법론

### 왜 실제 OTA 플랫폼을 크롤링했는가

OTA 도메인을 처음 다루는 상황에서 "상상으로 도메인을 설계"하면 실제 업무 규칙을 놓칠 위험이 크다. 실제 OTA 플랫폼 3개(해외 글로벌, 해외 아시아, 국내)를 Playwright로 크롤링하여 **1차 자료에서 도메인 모델을 도출**했다.

### 리서치 결과 핵심 발견

**1. 가격 구조는 3단 레이어다** (docs/research/research-02-cross-platform-comparison.md)
- 같은 객실에 4~5개 요금 행이 존재 (환불가능/불가 × 조식유무 × 결제방식)
- 가격 = roomPrice + taxAndFees + 할인/쿠폰
- → RoomType : RatePlan = 1:N 관계가 필수, RatePlan이 핵심 복잡도

**2. 편의시설은 2레벨로 분리된다** (docs/research/research-01-platform-analysis.md)
- 해외 OTA: "숙소 시설"(13개 카테고리)과 "객실 시설"(25개 카테고리) 명확 분리
- 검색 필터에서도 별도 카테고리로 운영
- → PropertyAmenity와 RoomAmenity를 분리

**3. 국내/해외 OTA 차이가 도메인에 영향을 준다** (docs/research/research-02-cross-platform-comparison.md)
- 국내: 대실(시간제), 회원가/태그가, 역 도보 분, 세금 포함가
- 해외: Tax & Fees 별도, 멤버십 등급, 배리어프리
- → Location VO에 국내(역/도보분)와 해외(도심 거리) 모두 지원

**4. Supplier 통합은 B2B 도매 모델이다** (docs/research/research-02-cross-platform-comparison.md)
- 같은 숙소에 EPS, AGODA, DOTW 3개 공급자가 동시에 가격 제공
- 최저가를 고객에게 표시, 예약 시 해당 공급자 API로 요청
- → Supplier 도메인 + RatePlan.sourceType으로 가격 출처 구분

**5. 숙소 등록은 단계별 별도 저장이다** (docs/research/research-04-extranet-registration-flow.md)
- Booking.com: 8단계 각각 별도 저장, 중간 저장 가능
- 야놀자: 기본정보 → 객실 → 사진 → 요금 → 프로모션 별도
- 등록 후에도 사진/편의시설/객실을 독립적으로 수정
- → Property/RoomType/Photo/Amenity 각각 독립 UseCase

---

## 2. Bounded Context 분리 근거

### 왜 accommodation이 아닌 세분화된 BC로 나누었는가

초기에는 `accommodation` 하나에 모든 숙소 관련 코드를 넣었다. 리뷰 과정에서 다음 이유로 세분화:

| BC | 분리 근거 |
|---|---------|
| **property** | 숙소 기본정보. 변경 빈도 낮음. 파트너가 처음 등록할 때 주로 설정 |
| **roomtype** | 객실 유형. Property와 생명주기가 다름 (객실 추가/삭제가 독립적) |
| **brand** | 체인 호텔 브랜드. 여러 Property에서 참조. 관리자(Admin)가 관리 |
| **propertytype** | 숙소 유형 분류 + EAV 속성 정의. 메타 데이터 성격 |
| **roomattribute** | 침대/전망 유형 마스터. 코드 테이블 성격 |
| **pricing** | 요금 정책. 변경 빈도 매우 높음 (시즌별/요일별). Property와 생명주기 완전 다름 |
| **inventory** | 재고. 실시간 변동. 동시성 제어 핵심 대상 |
| **reservation** | 예약. 고객 행위. 상태 전이(PENDING→CONFIRMED→COMPLETED/CANCELLED) |
| **partner** | 파트너 계정. 인증/권한 주체. 숙소와 책임이 다름 |
| **member** | 파트너 멤버. 역할(OWNER/MANAGER/STAFF)별 권한 관리 |
| **supplier** | 외부 공급자. API 연동, 동기화. 완전히 다른 데이터 소스 |
| **location** | 랜드마크. 여러 Property에서 참조. 독립적 생명주기 |
| **accommodation** | 숙소/객실 공유 타입 (AmenityType, PhotoType 등). 크로스 BC 공유 enum |
| **common** | 전체 도메인 공유 VO (Money, DateRange, Coordinate 등) |

### BC 간 관계와 데이터 흐름

객체를 직접 참조하면 BC 간 결합도가 높아진다. 모든 BC 간 참조는 ID VO(PropertyId, RoomTypeId 등)를 통해 이루어지며, 컴파일 타임에 타입 안전성을 보장한다.

```
[숙소 등록 흐름]
Partner ──owns──→ Property ──has──→ RoomType ──has──→ RatePlan
                    │                  │                  │
                    ├── PropertyPhoto   ├── RoomTypeBed    ├── RateRule
                    ├── PropertyAmenity ├── RoomAmenity    ├── RateOverride
                    └── Location        └── Inventory      └── RatePlanAddOn

[예약 흐름]
Customer → Reservation ──contains──→ ReservationItem
                                        ├── roomTypeId (참조)
                                        ├── ratePlanId (참조)
                                        └── inventoryId (참조, 재고 차감)

[외부 공급자 흐름]
Supplier ──maps──→ SupplierProperty ──→ Property (자사 숙소와 매핑)
                   └── SupplierRoomType ──→ RoomType
```

**핵심 원칙**: BC 간에는 ID VO로만 참조하고, 객체 그래프를 직접 탐색하지 않는다. 예를 들어 ReservationItem은 `RoomTypeId`를 들고 있지만 RoomType 객체를 알지 못한다. 크로스 BC 데이터 조합은 Application 레이어(UseCase)에서 수행한다.

---

## 3. 핵심 설계 패턴과 선택 근거

### Aggregate Root + forNew/reconstitute 패턴

**문제**: 도메인 객체 생성 시 "신규 생성"과 "DB 복원"의 의도가 구분되지 않으면 복원 시 검증이 실행되어 과거 규칙 변경 시 기존 데이터 복원이 실패한다.

**선택**: `forNew()` (검증 수행) / `reconstitute()` (검증 없이 복원) 분리.

**대안 검토**: 단일 생성자 + 검증 플래그 → 거부. 의도가 메서드명에서 드러나지 않으면 실수 위험.

### record → class 전환 (하위 엔티티)

**문제**: Long id를 가진 record 13개가 Aggregate 규칙을 통째로 우회.
- compact constructor가 모든 생성 경로에서 실행 → reconstitute()에서도 검증 돌아감
- equals/hashCode가 전체 필드 비교 → ID 기반이어야 하는데 불가
- private 생성자 불가 → 외부 생성 차단 불가

**선택**: 전부 class로 전환. ArchUnit(DOM-AGG-013)으로 "Long id record" 금지 규칙 추가.

**교훈**: record는 VO(Value Object)에만 사용. 엔티티(id가 있는 것)는 반드시 class.

### 일급 컬렉션 (PropertyAmenities 등)

**문제**: 편의시설의 sortOrder 중복 검증, 침대 유형 중복 검증 등 **컬렉션 수준의 불변식**을 개별 엔티티에서 검증할 수 없다.

**선택**: PropertyAmenities, PropertyPhotos, RoomTypeBeds 등 래핑 객체 도입.
- `forNew()` — 중복 검증 수행
- `reconstitute()` — 검증 없이 복원
- persistence에서 도메인 객체로 변환할 때 자동으로 컬렉션 검증

**근거**: OTA 리서치에서 사진/편의시설은 독립적 생명주기를 가지므로 Property Aggregate에 포함하지 않되, 컬렉션 자체의 규칙은 래핑 객체가 담당.

### ID VO (자기 ID vs 참조 ID)

**문제**: Long 타입만 쓰면 `propertyId`와 `roomTypeId`를 실수로 바꿔 넣어도 컴파일러가 못 잡는다.

**선택**: 
- 자기 ID (PropertyId, RoomTypeId 등): null 허용 + isNew() — DB 채번 전
- 참조 ID (PartnerId 등): null 불허 — 이미 존재하는 엔티티
- 예외: BrandId는 참조이지만 nullable — 모텔/펜션은 브랜드 없음 (리서치 근거: 모텔 769개, 펜션 21개 등 대다수 숙소가 브랜드 없음)

### EAV 패턴 (PropertyType → PropertyTypeAttribute → PropertyAttributeValue)

**문제**: 호텔에는 성급(star_rating)이 있지만 모텔/펜션에는 없다. 숙소 유형마다 필요한 메타 정보가 다르다.

**선택**: 커머스의 "고시정보(노티스 카테고리)" 패턴을 차용. PropertyType별로 필수/선택 속성을 동적으로 정의.

**근거**: OTA 크롤링에서 해외 OTA의 검색 필터가 22개 카테고리인데, 이 중 성급 필터는 호텔에만 적용. 정적 컬럼으로는 유형별 차이를 표현할 수 없다.

---

## 4. 요금 설계 근거

### RateRule + RateOverride + Rate 3단 구조

**문제**: 요금이 날짜별로 다른데, 365일치를 미리 만들어두는 건 비현실적.

**선택**:
1. **RateRule** (원본 규칙): "4/1~4/30, 평일 10만, 금 12만, 토 15만"
2. **RateOverride** (특정일 덮어쓰기): "4/5(공휴일)만 15만"
3. **Rate** (계산된 스냅샷): RateRule에서 계산된 날짜별 가격

**근거**: 파트너가 Extranet에서 설정하는 것은 "규칙"이다 (기간 + 요일별 가격). 고객이 조회하는 것은 "특정 날짜의 가격"이다. 이 둘을 분리하면 설정은 규칙 기반으로 간단하고, 조회는 미리 계산된 값으로 빠르다.

### Redis 3단 캐싱 (docs/seeds/2026-04-02-rate-caching-design.md)

**문제**: "대규모 요금 조회 요청이 동시에 들어오는 상황"에서 매번 RateRule에서 계산하면 DB 부하.

**선택**: RateRule → Rate(DB 스냅샷) → Redis(캐시) 3단 레이어.
- 캐시 스탬피드 방어: 분산 락(Redisson) + TTL Jittering

**대안 검토**:
- Rate만 두고 캐시 없이 → DB 부하
- RateRule만 두고 매번 계산 → 계산 비용
- 3단 레이어 → 설정은 규칙, 조회는 캐시, DB는 백업

### RatePlanAddOn (조식/패키지 분리)

**문제**: 같은 객실에 "조식 포함/미포함"으로 별도 RatePlan 행이 존재 → 조합 폭발 (취소정책 × 조식 × 결제방식).

**선택**: RatePlan은 핵심 정책(취소+결제)만 담고, 조식/패키지는 AddOn으로 분리.

**근거**: 크롤링에서 같은 객실에 조식 유무만 다른 행이 별도 존재 (₩356,400 vs ₩423,500). 커머스의 상품-옵션 구조와 동일한 패턴.

---

## 5. 재고 동시성 설계 근거 (docs/seeds/2026-04-02-inventory-concurrency-design.md)

### Redis 원자적 카운터 + 임시 홀드

**문제**: 동일 재고에 대해 동시 예약 요청이 발생할 때 정합성 보장.

**검토한 대안**:
| 방식 | 장점 | 단점 | 판단 |
|------|------|------|------|
| DB 비관적 락 (SELECT FOR UPDATE) | 구현 단순 | 행이 없으면 락 불가, DB 락 경합 | ✗ |
| DB 낙관적 락 (version) | 락 없음 | 동시성 높으면 재시도 폭발 | ✗ |
| Redis 원자적 카운터 | 초당 10만+, 락 불필요 | Redis-DB 정합성 관리 | ✓ |

**선택**: Redis DECR로 원자적 차감 + TTL 기반 임시 홀드(10분) + 주기적 Redis-DB 정합성 배치.

---

## 6. 이벤트 전략 — Outbox + 스케줄러

### Outbox 패턴 채택

**문제**: 비동기 처리가 필요한 상황에서 Spring ApplicationEventPublisher는 메모리 기반이라 장애 시 유실.

**선택**: Outbox 테이블에 저장 (같은 트랜잭션) + 스케줄러 2개 (메인 처리 + 좀비 복구).

**대안 검토**:
- 이벤트 발행 + 누락 보정 스케줄러 → 정상 경로가 2개 (이벤트 + 스케줄러), 디버깅 복잡
- Outbox + 스케줄러만 → 정상 경로 1개, 예측 가능

**BC별 전용 Outbox**: ReservationOutbox, SupplierOutbox로 분리. 공통 Outbox 1개 대신 BC별로 두면 독립 배포 가능, 스케줄러 주기 차별화 가능.

---

## 7. Extranet 등록 흐름과 UseCase 분리 (docs/research/research-04-extranet-registration-flow.md)

### 숙소 등록은 단계별 별도 저장

**리서치 결과**: Booking.com, 야놀자 모두 기본정보 → 객실 → 사진 → 편의시설 → 요금을 각각 별도 단계로 저장. 등록 후에도 각각 독립 수정.

**설계 반영**:
```
RegisterPropertyUseCase — 숙소 기본정보만
AddPropertyPhotosUseCase — 사진 독립 업로드
SetPropertyAmenitiesUseCase — 편의시설 독립 설정
RegisterRoomTypeUseCase — 객실 독립 등록
SetRateRuleUseCase — 요금 규칙 설정
```

**근거**: 사진 업로드 실패가 숙소 기본정보 롤백을 유발하면 안 됨. 각각 다른 트랜잭션 경계.

---

## 8. 참조 문서 목록

| 문서 | 경로 | 내용 |
|------|------|------|
| OTA 플랫폼 분석 | docs/research/research-01-platform-analysis.md | 해외 2 + 국내 1 크롤링 상세 |
| 크로스 플랫폼 비교 | docs/research/research-02-cross-platform-comparison.md | 공통/차별 필드 비교표 |
| 도메인 검증 | docs/research/research-03-domain-validation.md | 초안 vs 실제 데이터 대조 |
| Extranet 등록 흐름 | docs/research/research-04-extranet-registration-flow.md | 등록 단계별 분리 근거 |
| 요금 캐싱 설계 | docs/seeds/2026-04-02-rate-caching-design.md | 3단 캐싱 + 스탬피드 방어 |
| 재고 동시성 설계 | docs/seeds/2026-04-02-inventory-concurrency-design.md | Redis 카운터 + 홀드 |
| 도메인 피드백 29건 | docs/seeds/2026-04-01-domain-feedback.md | ERD 리뷰 과정 피드백 |
| 의사결정 기록 | docs/seeds/2026-04-01-decisions.md, 2026-04-04-decisions.md | 15개 설계 판단 |
| 도메인 README | domain/README.md | 도메인 전체 구조 상세 |
| ERD | docs/bonus/erDiagram.md | 전체 테이블 관계도 |
