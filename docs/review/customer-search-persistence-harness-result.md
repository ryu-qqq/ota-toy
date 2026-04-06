# Persistence 하네스 결과 보고서

## 대상
고객 숙소 검색 + 요금 조회를 위한 Persistence 레이어

## 실행 일시
2026-04-06

## 파이프라인 결과

| Phase | 항목 | 결과 |
|-------|------|------|
| Phase 0 | 전제조건 확인 | PASS |
| Phase 1 | Builder 실행 | Entity 1, Mapper 1, Repository 4, Adapter 6 생성, 컴파일 PASS |
| Phase 2 | 컨벤션 셀프 체크 9/9 | PASS |
| Phase 3 | 통합 테스트 전체 통과 | PASS (failures=0, errors=0) |
| Phase 4 | 결과 문서화 | 완료 |

---

## Phase 1: 생성 파일 매니페스트

### MySQL Adapter (신규 생성)

| 파일 | 역할 |
|------|------|
| `persistence/roomtype/entity/RoomTypeJpaEntity.java` | RoomType JPA Entity |
| `persistence/roomtype/mapper/RoomTypeEntityMapper.java` | RoomType Domain <-> Entity 변환 |
| `persistence/roomtype/repository/RoomTypeJpaRepository.java` | RoomType Command Repository |
| `persistence/roomtype/repository/RoomTypeQueryDslRepository.java` | RoomType Query Repository |
| `persistence/roomtype/adapter/RoomTypeCommandAdapter.java` | RoomTypeCommandPort 구현 |
| `persistence/roomtype/adapter/RoomTypeQueryAdapter.java` | RoomTypeQueryPort 구현 |
| `persistence/pricing/repository/RateQueryDslRepository.java` | Rate Query Repository |
| `persistence/pricing/adapter/RateQueryAdapter.java` | RateQueryPort 구현 |
| `persistence/inventory/repository/InventoryQueryDslRepository.java` | Inventory Query Repository |
| `persistence/inventory/adapter/InventoryQueryAdapter.java` | InventoryQueryPort 구현 |
| `persistence/property/repository/PropertySearchQueryDslRepository.java` | 크로스 BC 검색 쿼리 Repository |
| `persistence/property/adapter/PropertySearchQueryAdapter.java` | PropertySearchQueryPort 구현 |

### Redis Adapter (신규 생성)

| 파일 | 역할 |
|------|------|
| `persistence/redis/adapter/RateCacheAdapter.java` | RateCachePort 구현 (Redisson MGET/MSET) |

---

## Phase 2: 컨벤션 셀프 체크 상세

| 규칙 | 검증 | 결과 |
|------|------|------|
| PER-ENT-001 | JPA 관계 어노테이션 금지 | PASS |
| PER-ENT-004 | Lombok 금지 | PASS |
| PER-ENT-001 | create() 팩토리 존재 | PASS |
| PER-ENT-005 | Entity 비즈니스 로직 금지 | PASS |
| PER-ENT-001 | setter 금지 | PASS |
| PER-REP-001 | JpaRepository 커스텀 메서드 금지 | PASS |
| PER-ADP-001 | CQRS 분리 | PASS |
| PER-MAP-001 | reconstitute() 사용 | PASS |
| PER-FLY-001 | Flyway 마이그레이션 | PASS (기존 스키마 활용) |

---

## Phase 3: 테스트 결과

### 신규 테스트

| 테스트 클래스 | 테스트 수 | 통과 | 실패 |
|-------------|---------|------|------|
| RoomTypePersistenceAdapterTest | 6 | 6 | 0 |
| RateQueryAdapterTest | 3 | 3 | 0 |
| InventoryQueryAdapterTest | 4 | 4 | 0 |
| PropertySearchQueryAdapterTest | 5 | 5 | 0 |
| **합계** | **18** | **18** | **0** |

### 테스트 시나리오

**RoomType:**
- PT-1: 모든 필드 매핑 정합성 검증
- PT-2: persist/findById CRUD, existsById
- PT-3: findByPropertyId, findActiveByPropertyIdAndMinOccupancy QueryDSL 쿼리

**Rate:**
- 날짜 범위 기반 조회 (startDate 포함, endDate 미포함)
- 존재하지 않는 RatePlanId 조회 시 빈 결과
- 필드 정합성 검증

**Inventory:**
- 날짜 범위 기반 조회 (startDate 포함, endDate 미포함)
- 존재하지 않는 RoomTypeId 조회 시 빈 결과
- 필드 정합성 검증
- 여러 RoomTypeId 동시 조회

**PropertySearch (크로스 BC):**
- 기본 검색: 재고/인원 조건 충족 숙소 반환
- 커서 페이지네이션 동작
- 인원 필터: maxOccupancy >= guests
- 무료취소 필터: freeCancellation=true 숙소만
- 편의시설 필터: 지정 AmenityType 보유 숙소만
- 조건 불충족 시 빈 결과 반환

---

## 구현 결정 사항

### PropertySearchQueryDslRepository 설계
- **2단계 쿼리**: 1단계에서 조건을 만족하는 Property ID 목록을 조회하고, 2단계에서 상세 정보를 조회한다.
- **서브쿼리 활용**: RoomType 인원, Inventory 가용성, RatePlan 무료취소, PropertyAmenity 편의시설 조건을 서브쿼리로 처리하여 N+1을 방지한다.
- **재고 완전 가용성**: `GROUP BY + HAVING COUNT = nights` 로 숙박 기간 내 모든 날짜에 재고가 있는 경우만 통과.

### RateCacheAdapter 설계
- **Redisson Batch**: MGET/MSET을 Batch로 처리하여 Redis 네트워크 왕복을 1회로 줄인다.
- **키 형식**: `rate:{ratePlanId}:{date}` -> price 문자열
- **TTL 24시간**: Write-Through이므로 만료 전에 갱신되지만, 안전장치로 TTL 설정.
