# 정합성 점검 보고서

> 점검일: 2026-04-02
> 점검 대상: ERD v2, 백로그, 도메인 코드, 4개 레이어 컨벤션, 설계 시드 문서

---

## 1. ERD ↔ 백로그 정합성

### 일치 항목

| ERD 엔티티 | 커버하는 Story | 상태 |
|------------|---------------|------|
| Property, RoomType, PropertyType, Brand | STORY-101 | 부분 완료 |
| PropertyTypeAttribute, PropertyAttributeValue | STORY-101 | 완료 |
| RoomTypeAttribute, BedType, ViewType, RoomTypeBed, RoomTypeView | STORY-101, STORY-106 | 도메인 완료, API 미구현 |
| PropertyAmenity, RoomAmenity | STORY-101 | 도메인 완료 |
| PropertyPhoto, RoomPhoto | STORY-101 | 도메인 완료 |
| Landmark, PropertyLandmark | STORY-101 | 도메인 완료 |
| RatePlan, RatePlanAddOn, RateRule, RateOverride, Rate | STORY-107 | 도메인 완료, UseCase 미구현 |
| Inventory | STORY-301, STORY-601 | 미구현 |
| Reservation, ReservationItem | STORY-301, STORY-302 | 미구현 |
| Partner, PartnerMember | STORY-101 | PartnerId만 존재, 모델 미구현 |
| Supplier, SupplierApiConfig, SupplierProperty, SupplierRoomType, SupplierSyncLog | STORY-401, STORY-402, STORY-403 | 미구현 |

### 불일치/누락 항목

| 항목 | 상세 | 심각도 | 조치 방향 |
|------|------|--------|-----------|
| Outbox 테이블 | ERD에 Outbox 엔티티가 없으나, 백로그 STORY-501에서 Outbox 도메인 모델 + 스케줄러 요구 | MAJOR | ERD에 Outbox 엔티티 추가 필요 |
| RateRule의 calculated_from | ERD Rate 테이블에 `calculated_from` 필드가 있으나, 도메인 코드 Rate에는 해당 필드 없음 | MINOR | Rate 도메인에 calculatedFrom 필드 추가 또는 ERD에서 제거 결정 필요 |

**결론**: ERD와 백로그는 거의 정합. Outbox만 ERD에 추가하면 완전 일치.

---

## 2. ERD ↔ 도메인 코드 정합성

### 구현 완료 (도메인 코드 존재)

| ERD 엔티티 | 도메인 클래스 | 패키지 | 필드 일치 |
|------------|-------------|--------|:---------:|
| Property | Property.java | accommodation | O |
| RoomType | RoomType.java | accommodation | O |
| PropertyType | PropertyType.java (enum) | accommodation | O (코드 테이블 → enum) |
| Brand | Brand.java | accommodation | O |
| PropertyTypeAttribute | PropertyTypeAttribute.java | accommodation | O |
| PropertyAttributeValue | PropertyAttributeValue.java | accommodation | O |
| RoomTypeAttribute | RoomTypeAttribute.java | accommodation | O |
| BedType | BedType.java | accommodation | O |
| ViewType | ViewType.java | accommodation | O |
| RoomTypeBed | RoomTypeBed.java | accommodation | O |
| RoomTypeView | RoomTypeView.java | accommodation | O |
| PropertyAmenity | PropertyAmenity.java | accommodation | O |
| RoomAmenity | RoomAmenity.java | accommodation | O |
| PropertyPhoto | PropertyPhoto.java | accommodation | O |
| RoomPhoto | RoomPhoto.java | accommodation | O |
| Landmark | Landmark.java | location | O |
| PropertyLandmark | PropertyLandmark.java | location | O |
| Location (VO) | Location.java | location | O |
| RatePlan | RatePlan.java | pricing | O |
| RatePlanAddOn | RatePlanAddOn.java | pricing | O |
| RateRule | RateRule.java | pricing | O |
| RateOverride | RateOverride.java | pricing | O |
| Rate | Rate.java | pricing | 부분 (calculated_from 누락) |

### 미구현 (ERD에 있으나 도메인 코드 없음)

| ERD 엔티티 | 담당 Story | 우선순위 |
|------------|-----------|---------|
| Inventory | STORY-301 | P0 |
| Reservation | STORY-301 | P0 |
| ReservationItem | STORY-301 | P0 |
| Partner | STORY-101 | P0 |
| PartnerMember | STORY-101 | P0 |
| Supplier | STORY-401 | P0 |
| SupplierApiConfig | STORY-401 | P0 |
| SupplierProperty | STORY-401 | P0 |
| SupplierRoomType | STORY-401 | P0 |
| SupplierSyncLog | STORY-401 | P0 |
| Outbox | STORY-501 | P0 |

### 패키지 구조 차이

| 컨벤션 정의 | 실제 코드 | 비고 |
|------------|----------|------|
| `domain/accommodation/` | `domain/accommodation/` | 일치 |
| `domain/inventory/` | 미존재 | Inventory는 별도 BC로 분리 예정 |
| `domain/reservation/` | 미존재 | 미구현 |
| `domain/partner/` | `domain/partner/` (PartnerId만) | 나머지 미구현 |
| `domain/supplier/` | 미존재 | 미구현 |
| — | `domain/pricing/` | ERD/컨벤션에는 별도 pricing BC 명시 없음. 현재 코드에서 분리됨 |
| — | `domain/location/` | ERD/컨벤션에는 별도 location BC 명시 없음. 현재 코드에서 분리됨 |

**발견 사항**: 실제 도메인 코드는 `pricing`, `location` 패키지를 별도로 분리했으나, CLAUDE.md와 domain-convention.md에서는 이 분리를 명시적으로 반영하지 않음. 코드 우선으로 컨벤션 문서 갱신이 필요하거나, 컨벤션의 패키지 구조를 현행화해야 함.

---

## 3. 도메인 코드 ↔ 도메인 컨벤션 정합성

### 준수 항목

| 규칙 코드 | 규칙 내용 | 준수 여부 |
|-----------|----------|:---------:|
| DOM-AGG-001 | forNew/reconstitute 팩토리 패턴 | O (Property, RoomType, RatePlan, RateRule, Rate 모두 준수) |
| DOM-AGG-002 | Aggregate Root ID는 ID VO | O (PropertyId, RoomTypeId, RatePlanId) |
| DOM-AGG-004 | Setter 금지 + 비즈니스 메서드 | O (rename, deactivate 등 비즈니스 메서드 사용) |
| DOM-AGG-010 | equals/hashCode ID 기반 | O (모든 Aggregate에 구현) |
| DOM-ID-001 | ID VO Record + of() | O (PropertyId, RoomTypeId, RatePlanId, PartnerId, BrandId, PropertyTypeId) |
| DOM-VO-001 | VO는 Record + Compact Constructor | O (Location, PropertyName 등) |
| DOM-ERR-001 | ErrorCode 인터페이스 + enum | O (AccommodationErrorCode, PricingErrorCode) |
| DOM-EXC-001 | DomainException RuntimeException 상속 | O |
| DOM-CMN-002 | 외부 레이어 의존 금지 | O (domain/build.gradle.kts에 프레임워크 의존 없음) |

### 미준수/개선 필요 항목

| 규칙 코드 | 내용 | 현재 상태 | 심각도 |
|-----------|------|----------|--------|
| DOM-VO-002 | Enum displayName() 권장 | PropertyType은 enum이지만 displayName() 미확인 | MINOR |
| DOM-CRI-001 | Criteria Record | PropertySliceCriteria 미구현 | MAJOR (검색 Story 선행) |
| DOM-CMN-010 | PageRequest, CursorPageRequest 등 | 미구현 | MAJOR (STORY-102) |
| DOM-CMN-011 | SortKey, SortDirection 등 | 미구현 | MAJOR (STORY-102) |
| DOM-CMN-012 | DateRange, Money, DeletionStatus | 미구현 | MAJOR (STORY-102) |
| DOM-CMN-013 | CacheKey, LockKey 인터페이스 | 미구현 | MAJOR (STORY-102) |
| — | Rate에 ID VO 없음 | Rate.id가 Long 타입 — Aggregate Root라면 ID VO 필요 | MINOR (Rate는 하위 엔티티 성격) |
| — | RateRule에 ID VO 없음 | RateRule.id가 Long 타입 | MINOR (하위 엔티티) |
| — | RatePlan.forNew()에서 IllegalArgumentException 사용 | 도메인별 구체 예외가 아닌 범용 예외 (APP-EXC-001 위반 가능성) | MINOR (Domain 내부 검증이므로 허용 범위) |

**핵심**: 이미 구현된 도메인 코드는 컨벤션을 잘 따르고 있음. 미구현 부분은 공통 VO(STORY-102)에 집중되어 있음.

---

## 4. 컨벤션 간 정합성 (4개 레이어)

### 일치 항목

| 검증 포인트 | 결과 | 상세 |
|------------|------|------|
| Domain ↔ Application: Port 위치 | 일치 | Domain에 Port 없음, Application에 Port 정의 |
| Application ↔ Persistence: CQRS 분리 | 일치 | CommandPort/QueryPort가 CommandAdapter/QueryAdapter로 구현 |
| Application ↔ API: UseCase 인터페이스 | 일치 | Controller → UseCase → Service 흐름 |
| Domain ↔ Persistence: Entity 분리 | 일치 | Domain 모델과 JpaEntity 분리, Mapper 변환 |
| Application BC 경계 ↔ Domain 패키지 구조 | 일치 | ReadManager 크로스 BC 허용, CommandManager 같은 BC만 |

### 잠재적 모순/주의 항목

| 항목 | 상세 | 심각도 |
|------|------|--------|
| Money VO int vs Rate BigDecimal | domain-convention에서 Money(int 원화)를 정의했으나, Rate/RateRule은 BigDecimal 사용 | MAJOR |
| Inventory BC 분리 여부 | domain-convention에서는 `domain/inventory/` 별도 BC, 실제 ERD에서 Inventory는 RoomType FK를 가짐. 도메인 코드에서 pricing과 inventory의 경계가 불명확 | MINOR |
| Outbox 패키지 위치 | Application 컨벤션에서 Outbox를 Application 레이어 scheduler/에 배치. domain/common에 OutboxStatus만 정의. Outbox 도메인 모델의 정확한 위치가 모호 | MINOR |

**Money vs BigDecimal 불일치 분석**: domain-convention의 DOM-CMN-012에서 `Money(int amount)`를 정의했지만, 현재 Rate, RateRule, RateOverride 등 가격 필드는 모두 `BigDecimal`을 사용 중. 두 가지 선택지가 있음:
1. **BigDecimal을 Money로 교체**: 원화 전용이므로 int 기반 Money VO로 통일 (소수점 불필요)
2. **Money를 BigDecimal 기반으로 변경**: 향후 다통화 대응 가능하도록 유지

**권장**: 현행 BigDecimal 유지하되, Money VO 내부를 int가 아닌 BigDecimal로 변경하거나, 가격 필드에 Money VO를 적용하지 않고 BigDecimal로 유지하는 것이 실용적. 이 트레이드오프를 ADR로 기록할 것.

---

## 5. 설계 문서 ↔ 백로그 정합성

### 캐싱 설계 (seeds/2026-04-02-rate-caching-design.md) ↔ STORY-202

| 설계 내용 | 백로그 수용기준 반영 | 상태 |
|----------|:------------------:|------|
| 3단 레이어 (RateRule → Rate → Redis) | AC-2에 반영 | 일치 |
| 분산 락 (Redisson) | AC-4에 반영 | 일치 |
| TTL 분산 (Jittering) | AC-3에 반영 | 일치 |
| 캐시 사전 갱신 | 백로그에 미반영 | MINOR (3차 선택 사항이므로 생략 가능) |
| Write Path: RateRule 변경 → Rate 재계산 → 캐시 invalidate | STORY-107 AC-1~AC-5에 분산 반영 | 일치 |

### 동시성 설계 (seeds/2026-04-02-inventory-concurrency-design.md) ↔ STORY-302

| 설계 내용 | 백로그 수용기준 반영 | 상태 |
|----------|:------------------:|------|
| Redis 원자적 카운터 (DECR/INCR) | AC-1, AC-3에 반영 | 일치 |
| 임시 홀드 (hold:{reservationId} TTL) | AC-4에 반영 | 일치 |
| 부분 실패 시 전부 INCR 복구 | AC-3에 반영 | 일치 |
| Redis-DB 정합성 배치 | STORY-602로 분리 반영 | 일치 |
| Redis 장애 시 DB 폴백 | STORY-602 AC-4에 설계만 반영 | 일치 |
| 초기화 흐름 (RateRule 설정 시 Redis 세팅) | STORY-107 AC-6, STORY-601 AC-1에 반영 | 일치 |

**결론**: 설계 문서와 백로그 수용기준이 잘 매핑되어 있음. 누락 없음.

---

## 6. 종합 점검 결과

### BLOCKER (즉시 해결 필요)
- 없음

### MAJOR (구현 전 해결 필요)
1. **ERD에 Outbox 테이블 추가** — STORY-501 구현 전에 ERD 보완
2. **Money VO vs BigDecimal 불일치** — 방향 결정 후 컨벤션 또는 코드 갱신
3. **domain-convention 패키지 구조 현행화** — pricing, location BC 분리 반영

### MINOR (구현 중 병행 해결 가능)
1. Rate.calculated_from 필드 결정
2. Enum displayName() 보강
3. Outbox 도메인 모델 패키지 위치 명확화
4. RatePlan.forNew() 범용 예외 → 도메인 예외 전환 검토

---

## 7. 현재 구현 상태 요약

```
구현 완료:
  Domain: accommodation (Property, RoomType, Brand, 각종 VO/Enum), 
          pricing (RatePlan, RateRule, RateOverride, Rate, RatePlanAddOn), 
          location (Location, Landmark, PropertyLandmark),
          partner (PartnerId만),
          common (ErrorCode, DomainException)

미구현:
  Domain: Partner, PartnerMember, Inventory, Reservation, ReservationItem, 
          Supplier 전체, Outbox, 공통 VO (DateRange, Money, DeletionStatus 등)
  Application: 전체 미구현 (UseCase, Service, Manager, Factory, Port, DTO)
  Adapter-out:persistence-mysql: 전체 미구현
  Adapter-out:persistence-redis: 전체 미구현
  Adapter-in:rest-api: 전체 미구현
  Infra: Docker Compose, Flyway 등 전체 미구현
```
