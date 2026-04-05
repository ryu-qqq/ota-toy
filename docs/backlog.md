# 백로그

> 생성일: 2026-04-04
> 최종 갱신: 2026-04-05 (STORY-103 분할 — 단계별 별도 저장 구조 반영)
> 기준: 요구사항 필수 6개 + 선택 확장 + 권장 확장
> 현재 상태: Domain 모델 일부 구현 (accommodation, pricing, location, partner), Application/Adapter 전체 미구현

### 갱신 이력
- **2026-04-05**: STORY-103 분할 — OTA 리서치 근거로 숙소 등록을 단계별 별도 저장 구조로 분리 (기본정보/사진/편의시설/속성값/객실). STORY-103a~103d 신규 생성. STORY-106을 STORY-103d로 통합
- **2026-04-04**: Phase 2 수용기준 재확인 (STORY-103/104/105). 현재 도메인 코드(property 패키지 세분화, 일급 컬렉션, VO) 기준으로 수용기준 보강. Application/Persistence/API 컨벤션 규칙 번호 매핑 추가. 하위 엔티티(PropertyAmenity, PropertyPhoto, PropertyAttributeValue) 저장 전략, CQRS 분리, ErrorMapper/ApiMapper 패턴, PropertyTypeReadManager 검증 등 누락 항목 보완
- **2026-04-02**: ERD v2 반영 (PropertyLandmark, RateRule/RateOverride, Supplier 재설계), 동시성 제어 전략 변경 (비관적 락 → Redis 원자적 카운터), 요금 캐싱 3단 레이어 반영, Spring Event 금지 → Outbox 패턴 전환, Manager 레이어 컨벤션 반영, 도메인 컨텍스트 분리 (accommodation, pricing, location, partner) 반영

---

## Epic 1: 숙소 등록 및 관리 (Extranet)

> 요구사항 필수 1: "숙소 파트너가 Extranet을 통해 숙소 정보를 등록하고 관리할 수 있어야 한다"

### STORY-101: 숙소(Property) 도메인 모델 완성
- **우선순위**: P0
- **구현 상태**: 부분 완료
  - 완료: Property, RoomType, PropertyType, PropertyStatus, PropertyId, RoomTypeId, BrandId, PropertyTypeId, Brand, PropertyTypeAttribute, PropertyAttributeValue, RoomTypeAttribute, BedType, ViewType, RoomTypeBed, RoomTypeView, PropertyAmenity, RoomAmenity, PropertyPhoto, RoomPhoto, PropertyName, PropertyDescription, PromotionText, Location, Landmark, LandmarkType, PropertyLandmark
  - 미완료: Partner, PartnerMember 도메인 (PartnerId만 존재), PartnerErrorCode, PartnerStatus
- **수용기준**:
  - [x] AC-1: Property.forNew() 호출 시 name이 blank이면 IllegalArgumentException 발생
  - [x] AC-2: Property.reconstitute()는 검증 없이 모든 필드를 복원
  - [ ] AC-3: Partner 도메인 모델(Partner, PartnerMember)이 forNew/reconstitute 패턴으로 구현
  - [ ] AC-4: PartnerMember에 role(OWNER, MANAGER, STAFF)과 status(ACTIVE, INACTIVE) 관리 로직 존재
  - [ ] AC-5: ArchUnit 테스트 통과 (setter 금지, 외부 의존 금지, 생성자 비공개)
- **관련 레이어**: Domain
- **의존성**: 없음
- **담당 팀**: domain-team
- **추정 규모**: S

### STORY-102: 공통 도메인 VO 구현
- **우선순위**: P0
- **구현 상태**: 부분 완료 (ErrorCode, DomainException 존재. 나머지 미구현)
- **수용기준**:
  - [x] AC-1: ErrorCode 인터페이스가 core 또는 domain/common에 정의
  - [x] AC-2: DomainException이 RuntimeException 상속 + ErrorCode 필드 보유
  - [ ] AC-3: DateRange record 구현 (startDate > endDate 시 예외, nights(), dates() 메서드)
  - [ ] AC-4: Money record 구현 (int 기반 원화, add/multiply 메서드)
  - [ ] AC-5: DeletionStatus record 구현 (active(), deleted(Instant) 팩토리)
  - [ ] AC-6: PageRequest, CursorPageRequest, SliceMeta, PageMeta 구현
  - [ ] AC-7: SortKey, SortDirection, SearchField 인터페이스/enum 구현
  - [ ] AC-8: CacheKey, LockKey 인터페이스 구현
- **관련 레이어**: Domain (common)
- **의존성**: 없음
- **담당 팀**: domain-team
- **추정 규모**: M

### STORY-103: 숙소 기본정보 등록 UseCase 구현
- **우선순위**: P0
- **구현 상태**: 미구현
- **분할 근거**: OTA 리서치 결과 Booking.com/야놀자 모두 기본정보/사진/편의시설/속성값/객실을 단계별 별도 저장. 각각 독립 API, 독립 생명주기 (`docs/research/ota-extranet-registration-flow.md` 참조)
- **수용기준**:
  - [ ] AC-1: RegisterPropertyUseCase 호출 시 Property 기본정보만 저장되고 PropertyId(Long)가 반환됨
  - [ ] AC-2: partnerId가 존재하지 않으면 PartnerNotFoundException 발생 (PartnerReadManager.getById 경유)
  - [ ] AC-3: propertyTypeId가 존재하지 않으면 PropertyTypeNotFoundException 발생 (PropertyTypeReadManager.getById 경유)
  - [ ] AC-4: PropertyCommandPort, PropertyQueryPort 인터페이스가 Application 레이어(port/out/persistence/)에 정의됨. CommandPort는 persist/persistAll만 선언, QueryPort는 findById/findByCondition만 선언 (APP-PRT-001)
  - [ ] AC-5: PropertyFactory가 TimeProvider를 주입받아 Property.forNew() 호출로 도메인 객체 생성 (APP-FAC-001). Service/Manager에서 TimeProvider 직접 주입 금지
  - [ ] AC-6: RegisterPropertyService는 @Transactional 없이 Manager/Factory/Facade 조합으로 동작 (APP-SVC-001)
  - [ ] AC-7: PartnerReadManager를 통해 파트너 존재 확인 — 다른 BC ReadManager 호출 패턴 (APP-BC-001)
  - [ ] AC-8: PropertyPersistenceFacade가 Property만 @Transactional에서 저장 (APP-FCD-001). 사진/편의시설/속성값은 별도 UseCase에서 독립 저장
  - [ ] AC-9: RegisterPropertyCommand는 record로 선언. Property 기본정보 필드만 포함 (name, propertyTypeId, partnerId, brandId, address, latitude, longitude 등). 사진/편의시설/속성값 리스트 미포함 (APP-DTO-001)
  - [ ] AC-10: Port 직접 호출 금지 — Service는 Manager/Factory/Facade만 의존 (APP-BC-001)
  - [ ] AC-11: 초기 Property 상태는 DRAFT — 사진/편의시설/객실 등록 전까지 ACTIVE로 전환 불가
- **관련 레이어**: Domain → Application
- **의존성**: STORY-101, STORY-102
- **담당 팀**: application-team
- **추정 규모**: M

### STORY-103a: 숙소 사진 관리 UseCase 구현
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: AddPropertyPhotosUseCase 호출 시 PropertyPhoto 리스트가 저장되고 저장된 건수가 반환됨
  - [ ] AC-2: propertyId가 존재하지 않으면 PropertyNotFoundException 발생 (PropertyReadManager.getById 경유)
  - [ ] AC-3: PropertyPhotoCommandPort, PropertyPhotoQueryPort 인터페이스가 Application 레이어에 정의됨 (APP-PRT-001)
  - [ ] AC-4: AddPropertyPhotosCommand는 record로 선언. propertyId + List<PhotoItem>(photoType, originUrl, cdnUrl, sortOrder) 구조 (APP-DTO-001)
  - [ ] AC-5: PropertyPhotoFactory가 Command를 받아 PropertyPhotos(일급 컬렉션)를 생성 (APP-FAC-001)
  - [ ] AC-6: 사진 추가는 기존 사진에 append — 기존 사진을 삭제하지 않음
  - [ ] AC-7: sortOrder 중복 시 기존 사진의 sortOrder를 자동 재정렬
  - [ ] AC-8: Port 직접 호출 금지 — Service는 Manager/Factory/Facade만 의존 (APP-BC-001)
- **관련 레이어**: Domain → Application
- **의존성**: STORY-103
- **담당 팀**: application-team
- **추정 규모**: S

### STORY-103b: 숙소 편의시설 설정 UseCase 구현
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: SetPropertyAmenitiesUseCase 호출 시 PropertyAmenity 리스트가 전체 교체(replace) 방식으로 저장됨
  - [ ] AC-2: propertyId가 존재하지 않으면 PropertyNotFoundException 발생 (PropertyReadManager.getById 경유)
  - [ ] AC-3: PropertyAmenityCommandPort, PropertyAmenityQueryPort 인터페이스가 Application 레이어에 정의됨 (APP-PRT-001)
  - [ ] AC-4: SetPropertyAmenitiesCommand는 record로 선언. propertyId + List<AmenityItem>(amenityType, name, additionalPrice, sortOrder) 구조 (APP-DTO-001)
  - [ ] AC-5: PropertyAmenityFactory가 Command를 받아 PropertyAmenities(일급 컬렉션)를 생성 (APP-FAC-001)
  - [ ] AC-6: 편의시설 설정은 전체 교체(replace) — 기존 편의시설 soft delete 후 신규 목록으로 교체
  - [ ] AC-7: Port 직접 호출 금지 — Service는 Manager/Factory/Facade만 의존 (APP-BC-001)
- **관련 레이어**: Domain → Application
- **의존성**: STORY-103
- **담당 팀**: application-team
- **추정 규모**: S

### STORY-103c: 숙소 속성값 설정 UseCase 구현
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: SetPropertyAttributesUseCase 호출 시 PropertyAttributeValue 리스트가 전체 교체(replace) 방식으로 저장됨
  - [ ] AC-2: propertyId가 존재하지 않으면 PropertyNotFoundException 발생 (PropertyReadManager.getById 경유)
  - [ ] AC-3: propertyTypeId에 정의된 PropertyTypeAttribute 기준으로 필수(isRequired=true) 속성 누락 시 예외 발생
  - [ ] AC-4: PropertyAttributeValueCommandPort, PropertyAttributeValueQueryPort 인터페이스가 Application 레이어에 정의됨 (APP-PRT-001)
  - [ ] AC-5: SetPropertyAttributesCommand는 record로 선언. propertyId + List<AttributeItem>(propertyTypeAttributeId, value) 구조 (APP-DTO-001)
  - [ ] AC-6: PropertyAttributeValueFactory가 Command를 받아 PropertyAttributeValues(일급 컬렉션)를 생성 (APP-FAC-001)
  - [ ] AC-7: 속성값 설정은 전체 교체(replace) — 기존 속성값 soft delete 후 신규 목록으로 교체
  - [ ] AC-8: Port 직접 호출 금지 — Service는 Manager/Factory/Facade만 의존 (APP-BC-001)
- **관련 레이어**: Domain → Application
- **의존성**: STORY-103
- **담당 팀**: application-team
- **추정 규모**: S

### STORY-103d: 객실 등록 UseCase 구현
- **우선순위**: P0
- **구현 상태**: 미구현 (도메인 모델 RoomType은 존재)
- **수용기준**:
  - [ ] AC-1: RegisterRoomTypeUseCase 호출 시 RoomType이 저장되고 RoomTypeId(Long)가 반환됨
  - [ ] AC-2: propertyId가 존재하지 않으면 PropertyNotFoundException 발생 (PropertyReadManager.getById 경유)
  - [ ] AC-3: baseOccupancy > maxOccupancy이면 도메인 검증 예외 발생
  - [ ] AC-4: RoomTypeCommandPort, RoomTypeQueryPort 인터페이스가 Application 레이어에 정의됨 (APP-PRT-001)
  - [ ] AC-5: RegisterRoomTypeCommand는 record로 선언. propertyId + 객실 기본정보(name, description, areaSqm, baseOccupancy, maxOccupancy, baseInventory, checkInTime, checkOutTime) + List<BedItem>(bedTypeId, quantity) + List<ViewItem>(viewTypeId) 구조 (APP-DTO-001)
  - [ ] AC-6: RoomTypeFactory가 Command를 받아 RoomType + RoomTypeBed 목록 + RoomTypeView 목록을 생성 (APP-FAC-001)
  - [ ] AC-7: RoomTypePersistenceFacade가 RoomType + RoomTypeBed + RoomTypeView를 하나의 @Transactional에서 원자적으로 저장 (APP-FCD-001)
  - [ ] AC-8: Port 직접 호출 금지 — Service는 Manager/Factory/Facade만 의존 (APP-BC-001)
- **관련 레이어**: Domain → Application
- **의존성**: STORY-103
- **담당 팀**: application-team
- **추정 규모**: M

### STORY-104: 숙소 Persistence Adapter 구현
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: PropertyJpaEntity에 static create() 팩토리 메서드가 유일한 생성 진입점. Lombok 전면 금지(@Getter, @Setter, @NoArgsConstructor 등), setter 전면 금지 (PER-ENT-001, PER-ENT-004)
  - [ ] AC-2: Entity에 비즈니스 로직 금지 — create(), getter, isXxx()만 허용. updateXxx(), changeXxx() 등 상태 변경 메서드 금지 (PER-ENT-005)
  - [ ] AC-3: JPA 관계 어노테이션(@OneToMany, @ManyToOne, @OneToOne, @ManyToMany) Aggregate 내부 포함 전면 금지. Long FK 전략 사용 (PER-ENT-001)
  - [ ] AC-4: PropertyCommandAdapter(JpaRepository만 의존)와 PropertyQueryAdapter(QueryDslRepository만 의존)로 CQRS 분리 (PER-ADP-001)
  - [ ] AC-5: PropertyEntityMapper가 Domain → Entity 변환 시 create() 팩토리 사용, Entity → Domain 변환 시 reconstitute() 사용. VO(PropertyName, Location, PropertyStatus 등)와 일급 컬렉션(PropertyAmenities, PropertyPhotos, PropertyAttributeValues)의 변환 로직 포함 (PER-MAP-001)
  - [ ] AC-6: 하위 Entity도 독립 테이블로 관리 — PropertyAmenityJpaEntity, PropertyPhotoJpaEntity, PropertyAttributeValueJpaEntity 각각 독립 Entity + Mapper + Repository + Adapter
  - [ ] AC-7: PropertyJpaRepository는 save/saveAll만 허용. @Query, findBy*, deleteBy* 커스텀 메서드 금지 (PER-REP-001)
  - [ ] AC-8: PropertyQueryDslRepository의 모든 조회에 deleted.isFalse() soft delete 필터 포함 (PER-REP-001)
  - [ ] AC-9: persist 메서드에서 id == null이면 persist, 아니면 merge (PER-ADP-002)
  - [ ] AC-10: save() 후 DB에서 조회하면 모든 필드가 일치 (Property + 하위 엔티티 모두)
  - [ ] AC-11: Testcontainers(MySQL) 기반 통합 테스트 통과
  - [ ] AC-12: Flyway 마이그레이션 파일 — property, property_amenity, property_photo, property_attribute_value, partner 테이블 (PER-FLY-001)
  - [ ] AC-13: BaseAuditEntity(createdAt, updatedAt) / SoftDeletableEntity(deleted, deletedAt) 상속 구조 (PER-ENT-002)
- **관련 레이어**: Application → Adapter-out:mysql
- **의존성**: STORY-103
- **담당 팀**: persistence-mysql-team
- **추정 규모**: L

### STORY-105: 숙소 기본정보 등록 REST API (Extranet)
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: POST /api/v1/extranet/properties 요청 시 201 응답 + ApiResponse 래핑 + data에 propertyId 반환 (API-CTR-002)
  - [ ] AC-2: 필수 필드(name, propertyTypeId, partnerId, address) 누락 시 400 응답 + error.code="VALIDATION_ERROR" + error.userMessage + error.debugMessage 포함 (API-ERR-001)
  - [ ] AC-3: ExtranetPropertyController는 RegisterPropertyUseCase 인터페이스만 의존. 구체 Service 직접 주입 금지 (API-CTR-001)
  - [ ] AC-4: RegisterPropertyApiRequest는 record로 선언. Property 기본정보 필드만 포함 (사진/편의시설/속성값 미포함). Jakarta Validation(@NotNull, @NotBlank 등) 적용 (API-DTO-001, API-DTO-002)
  - [ ] AC-5: PropertyApiMapper.toCommand()로 Request → Command 변환. Controller에 인라인 변환 로직 금지 (API-DTO-001)
  - [ ] AC-6: DomainException 발생 시 GlobalExceptionHandler + ErrorMapper가 errorCode/userMessage/debugMessage로 변환하여 적절한 HTTP 상태 코드 반환 (API-ERR-001)
  - [ ] AC-7: @DeleteMapping 사용 금지 — soft delete는 PATCH로 처리 (API-CTR-001)
  - [ ] AC-8: Controller에 @Transactional 사용 금지. 비즈니스 로직 금지 (API-CTR-001)
  - [ ] AC-9: Swagger UI에서 API 명세 확인 가능 — @Tag, @Operation, @ApiResponses 어노테이션 적용 (API-DOC-001)
  - [ ] AC-10: partnerId 미존재 시 404 응답 (PartnerNotFoundException → GlobalExceptionHandler 경유)
- **관련 레이어**: Application → Adapter-in:rest-api
- **의존성**: STORY-103
- **담당 팀**: rest-api-team
- **추정 규모**: M

### STORY-106: 숙소 부속 정보 REST API (Extranet — 사진/편의시설/속성값/객실)
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: POST /api/v1/extranet/properties/{propertyId}/photos 요청 시 201 응답 + 저장 건수 반환. AddPropertyPhotosUseCase 의존 (API-CTR-001)
  - [ ] AC-2: PUT /api/v1/extranet/properties/{propertyId}/amenities 요청 시 200 응답. SetPropertyAmenitiesUseCase 의존. 전체 교체(replace) 방식 (API-CTR-001)
  - [ ] AC-3: PUT /api/v1/extranet/properties/{propertyId}/attributes 요청 시 200 응답. SetPropertyAttributesUseCase 의존. 필수 속성 누락 시 400 응답 (API-CTR-001)
  - [ ] AC-4: POST /api/v1/extranet/properties/{propertyId}/rooms 요청 시 201 응답 + roomTypeId 반환. RegisterRoomTypeUseCase 의존 (API-CTR-001)
  - [ ] AC-5: propertyId가 존재하지 않으면 모든 엔드포인트에서 404 응답
  - [ ] AC-6: baseOccupancy > maxOccupancy이면 객실 등록 시 400 응답
  - [ ] AC-7: 각 Request는 record로 선언. Jakarta Validation 적용 (API-DTO-001, API-DTO-002)
  - [ ] AC-8: 각 ApiMapper.toCommand()로 Request → Command 변환 (API-DTO-001)
  - [ ] AC-9: Swagger UI에서 모든 엔드포인트 명세 확인 가능 (API-DOC-001)
  - [ ] AC-10: Controller에 @Transactional 사용 금지. 비즈니스 로직 금지 (API-CTR-001)
- **관련 레이어**: Application → Adapter-in:rest-api
- **의존성**: STORY-103a, STORY-103b, STORY-103c, STORY-103d, STORY-105
- **담당 팀**: rest-api-team
- **추정 규모**: L

### STORY-107: 재고/요금 설정 API (Extranet)
- **우선순위**: P0
- **구현 상태**: 미구현 (RatePlan, RateRule, RateOverride, Rate 도메인 모델 존재 — pricing 패키지)
- **수용기준**:
  - [ ] AC-1: PUT /api/v1/extranet/inventory 요청 시 RateRule 저장 + 해당 기간의 Rate 스냅샷 자동 생성
  - [ ] AC-2: 기간(startDate~endDate) 내 요일별 가격이 Rate 테이블에 정확히 반영 (weekdayPrice, fridayPrice, saturdayPrice, sundayPrice 기반 계산)
  - [ ] AC-3: RateOverride가 있으면 해당 날짜의 Rate가 override 가격으로 갱신
  - [ ] AC-4: Inventory가 해당 기간에 대해 base_inventory 값으로 생성
  - [ ] AC-5: Redis 캐시에 rate:{ratePlanId}:{date} 키로 캐싱
  - [ ] AC-6: Redis 재고 카운터 초기화 (SET inventory:{roomTypeId}:{date} {base_inventory})
  - [ ] AC-7: Rate 스냅샷 생성은 Outbox를 통해 비동기 처리 가능 (또는 동기 처리)
- **관련 레이어**: Domain → Application → Adapter-out:mysql → Adapter-out:redis → Adapter-in:rest-api
- **의존성**: STORY-103d, STORY-106
- **담당 팀**: application-team
- **추정 규모**: L

---

## Epic 2: 숙소 검색 및 요금 조회

> 요구사항 필수 2: "고객이 조건에 맞는 숙소를 검색하고 요금을 조회할 수 있어야 한다"
> 요구사항 필수 3: "대규모 요금 조회 요청이 동시에 들어오는 상황을 고려한 설계"

### STORY-201: 숙소 검색 API (Customer)
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: GET /api/v1/search/properties?region=서울&checkIn=2026-04-10&checkOut=2026-04-12&guests=2 요청 시 200 응답 + properties 배열 반환
  - [ ] AC-2: 해당 날짜에 Redis 재고가 0인 숙소는 결과에 포함되지 않음
  - [ ] AC-3: maxOccupancy < guests인 객실만 있는 숙소는 결과에 포함되지 않음
  - [ ] AC-4: 커서 기반 페이지네이션 지원 (size, cursor 파라미터) — CursorPageRequest 사용
  - [ ] AC-5: 응답에 숙소 기본 정보 + 최저 가격이 포함
- **관련 레이어**: Domain → Application → Adapter-out:mysql → Adapter-out:redis → Adapter-in:rest-api
- **의존성**: STORY-107
- **담당 팀**: application-team
- **추정 규모**: L

### STORY-202: 요금 조회 API + Redis 3단 캐싱 (Customer)
- **우선순위**: P0
- **구현 상태**: 미구현 (캐싱 설계 문서 완료 — seeds/2026-04-02-rate-caching-design.md)
- **수용기준**:
  - [ ] AC-1: GET /api/v1/properties/{id}/rates?checkIn=2026-04-10&checkOut=2026-04-12 요청 시 200 응답 + 날짜별 요금 배열 반환
  - [ ] AC-2: 3단 캐싱: RateRule(원본) → Rate(DB 스냅샷) → Redis 캐시(조회용). 조회 시 Redis → Rate(DB) 순서로 탐색
  - [ ] AC-3: 캐시 TTL은 60분 + 랜덤 오프셋(0~10분) 적용 (Jittering)
  - [ ] AC-4: 캐시 미스 시 Redisson 분산 락으로 동일 키에 대해 DB 요청 1회만 발생
  - [ ] AC-5: 100개 동시 요금 조회 요청 시 DB 쿼리 수가 10 이하 (캐시 효과 검증)
  - [ ] AC-6: RateCacheClientManager를 통한 Redis 캐시 접근 (ClientManager 패턴)
- **관련 레이어**: Domain → Application → Adapter-out:mysql → Adapter-out:redis → Adapter-in:rest-api
- **의존성**: STORY-107
- **담당 팀**: application-team
- **추정 규모**: L

---

## Epic 3: 예약 및 취소

> 요구사항 필수 4: "고객이 숙소를 예약하고 취소할 수 있어야 한다"
> 요구사항 필수 5: "동일한 재고에 대해 동시 예약 요청이 발생할 수 있는 상황을 처리"

### STORY-301: 예약/재고 도메인 모델 구현
- **우선순위**: P0
- **구현 상태**: 미구현 (ERD에 Reservation, ReservationItem, Inventory 정의됨. 도메인 코드 없음)
- **수용기준**:
  - [ ] AC-1: Reservation.forNew() 시 status가 PENDING으로 생성. createdAt은 외부에서 Instant를 받음 (Factory가 TimeProvider로 생성)
  - [ ] AC-2: Reservation.confirm() 호출 시 status가 CONFIRMED로 변경
  - [ ] AC-3: Reservation.cancel(reason, Instant cancelledAt) 호출 시 status가 CANCELLED로 변경 + cancelReason, cancelledAt 설정
  - [ ] AC-4: 이미 CANCELLED인 Reservation에 cancel() 호출 시 도메인 예외 발생 (ReservationAlreadyCancelledException)
  - [ ] AC-5: Inventory 도메인에 available_count 필드와 isStopSell 판단 로직 존재. InventoryId record VO 구현
  - [ ] AC-6: ReservationStatus enum에 상태 전이 규칙이 코드로 표현 (PENDING→CONFIRMED, PENDING→CANCELLED, CONFIRMED→CANCELLED)
  - [ ] AC-7: ReservationErrorCode, InventoryErrorCode enum이 ErrorCode 인터페이스 구현
  - [ ] AC-8: bookingSnapshot 필드에 예약 시점 숙소/객실/요금 정보를 JSON 문자열로 보존
- **관련 레이어**: Domain
- **의존성**: STORY-102
- **담당 팀**: domain-team
- **추정 규모**: M

### STORY-302: 예약 생성 UseCase + Redis 원자적 카운터 재고 차감 (동시성 제어)
- **우선순위**: P0
- **구현 상태**: 미구현 (동시성 설계 문서 완료 — seeds/2026-04-02-inventory-concurrency-design.md)
- **수용기준**:
  - [ ] AC-1: POST /api/v1/reservations 요청 시 Redis DECR로 재고 원자적 차감 후 201 응답 + reservationId 반환
  - [ ] AC-2: Redis 재고(inventory:{roomTypeId}:{date})가 0인 날짜에 예약 요청 시 409 응답 + INVENTORY_EXHAUSTED 에러코드
  - [ ] AC-3: 2박 예약 시 두 날짜 모두 Redis DECR 실행. 하나라도 0 미만이면 전부 INCR 복구 후 실패
  - [ ] AC-4: 임시 홀드(hold:{reservationId}) 키가 Redis에 TTL 600초로 생성
  - [ ] AC-5: 10개 동시 예약 요청 중 재고가 1개일 때 정확히 1개만 성공하고 나머지 9개는 실패
  - [ ] AC-6: DB에 Reservation(CONFIRMED) + ReservationItem 저장. ReservationPersistenceFacade로 원자적 저장
  - [ ] AC-7: InventoryClientManager를 통한 Redis 재고 접근 (ClientManager 패턴)
  - [ ] AC-8: CreateReservationService는 @Transactional 없이 Redis 차감 → DB 저장 → 실패 시 INCR 복구 흐름
- **관련 레이어**: Domain → Application → Adapter-out:mysql → Adapter-out:redis → Adapter-in:rest-api
- **의존성**: STORY-301, STORY-107
- **담당 팀**: application-team
- **추정 규모**: L

### STORY-303: 예약 취소 API
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: DELETE /api/v1/reservations/{id} 요청 시 200 응답 + status가 CANCELLED
  - [ ] AC-2: 취소 시 Redis INCR로 해당 날짜 재고 복구 (InventoryClientManager 경유)
  - [ ] AC-3: DB Inventory의 available_count도 복구 (InventoryCommandManager 경유)
  - [ ] AC-4: 존재하지 않는 예약 ID로 요청 시 404 응답
  - [ ] AC-5: 이미 취소된 예약에 대해 재요청 시 400 응답 + ALREADY_CANCELLED 에러코드
  - [ ] AC-6: 취소 시 Outbox에 ReservationCancelled 메시지 저장 (같은 트랜잭션)
- **관련 레이어**: Domain → Application → Adapter-out:mysql → Adapter-out:redis → Adapter-in:rest-api
- **의존성**: STORY-302
- **담당 팀**: application-team
- **추정 규모**: M

### STORY-304: 동시성 제어 통합 테스트
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: Testcontainers(MySQL + Redis) 환경에서 ExecutorService 10개 스레드 동시 예약 실행
  - [ ] AC-2: 재고 1개인 객실에 대해 10개 동시 요청 시 성공 1개 + 실패 9개 (정확히)
  - [ ] AC-3: 테스트 후 Redis 재고(inventory:{roomTypeId}:{date})와 DB Inventory.available_count가 일치
  - [ ] AC-4: 예약 취소 후 Redis/DB 재고가 정확히 복구되는지 검증
  - [ ] AC-5: Redis DECR 원자 연산 기반이므로 분산 락 없이도 정합성 유지 검증
- **관련 레이어**: Application → Adapter-out:mysql → Adapter-out:redis
- **의존성**: STORY-302
- **담당 팀**: application-team
- **추정 규모**: M

---

## Epic 4: Supplier 통합

> 요구사항 필수 6: "외부 숙소 공급자(Supplier)의 상품을 자사 플랫폼에 통합할 수 있어야 한다"

### STORY-401: Supplier 도메인 모델 구현
- **우선순위**: P0
- **구현 상태**: 미구현 (ERD v2에 Supplier, SupplierApiConfig, SupplierProperty, SupplierRoomType, SupplierSyncLog 정의됨)
- **수용기준**:
  - [ ] AC-1: Supplier.forNew()로 공급자 생성 시 status가 ACTIVE
  - [ ] AC-2: SupplierId record VO 구현
  - [ ] AC-3: SupplierApiConfig에 apiBaseUrl, apiKey, authType, syncIntervalMinutes, status 관리
  - [ ] AC-4: SupplierProperty에 supplierId, propertyId, supplierPropertyId 매핑 + lastSyncedAt 추적
  - [ ] AC-5: SupplierRoomType에 supplierPropertyId, roomTypeId, supplierRoomId 매핑
  - [ ] AC-6: SupplierSyncLog에 동기화 결과(total/created/updated/deleted count) 기록 가능
  - [ ] AC-7: SupplierErrorCode enum이 ErrorCode 인터페이스 구현
- **관련 레이어**: Domain
- **의존성**: STORY-102
- **담당 팀**: domain-team
- **추정 규모**: M

### STORY-402: Supplier Anti-Corruption Layer 구현
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: SupplierClient 인터페이스(Port)가 Application 레이어에 정의 (Application DTO 사용, Domain Aggregate 직접 전달 금지)
  - [ ] AC-2: 외부 Supplier 모델 → 내부 Property/RoomType/RatePlan 모델 변환 로직 구현
  - [ ] AC-3: SupplierClientManager를 통한 외부 API 호출 (트랜잭션 없음)
  - [ ] AC-4: Supplier 응답을 Redis에 TTL 기반 캐싱 (동일 요청 반복 시 외부 API 호출 0회)
  - [ ] AC-5: Supplier API 실패 시 캐싱된 이전 데이터로 응답 (fallback)
- **관련 레이어**: Domain → Application → Adapter-out:supplier → Adapter-out:redis
- **의존성**: STORY-401
- **담당 팀**: application-team
- **추정 규모**: L

### STORY-403: Supplier Diff 동기화 UseCase
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: SyncSupplierUseCase 실행 시 SupplierSyncLog에 결과 기록 (total, created, updated, deleted count)
  - [ ] AC-2: 외부 숙소가 이미 매핑(SupplierProperty)된 경우 변경분만 update, 신규이면 create
  - [ ] AC-3: 외부에서 삭제된 숙소는 SupplierProperty.status를 INACTIVE로 변경 (soft delete)
  - [ ] AC-4: 동기화된 숙소의 RatePlan.sourceType이 SUPPLIER로 설정, supplier_id가 매핑됨
  - [ ] AC-5: 동기화된 숙소가 고객 검색 결과에 자사 숙소와 함께 노출
  - [ ] AC-6: SyncSupplierService는 SupplierClientManager(외부 호출) + PropertyFactory/PropertyPersistenceFacade(다른 BC 쓰기) 패턴 사용
- **관련 레이어**: Domain → Application → Adapter-out:supplier → Adapter-out:mysql
- **의존성**: STORY-402
- **담당 팀**: application-team
- **추정 규모**: L

---

## Epic 5: Outbox + 비동기 처리

> 설계 결정: Spring ApplicationEvent 사용 금지. Outbox 테이블 + 스케줄러 2개로 비동기 처리

### STORY-501: Outbox 도메인 모델 + 스케줄러 구현
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: Outbox 도메인 모델에 OutboxStatus enum (PENDING, PROCESSING, COMPLETED, FAILED) 존재
  - [ ] AC-2: OutboxCommandPort, OutboxQueryPort가 Application 레이어에 정의
  - [ ] AC-3: OutboxCommandManager(@Transactional)에 persist, markProcessing, markCompleted, markFailed, recoverStaleProcessing 메서드
  - [ ] AC-4: OutboxReadManager(@Transactional(readOnly=true))에 findPending(limit) 메서드
  - [ ] AC-5: OutboxMainScheduler가 5초 주기로 PENDING → PROCESSING → 처리 → COMPLETED 실행
  - [ ] AC-6: OutboxZombieScheduler가 60초 주기로 PROCESSING 상태 + 5분 초과 건 → PENDING 복구
  - [ ] AC-7: OutboxProcessor가 outbox 타입별 처리 로직 라우팅
  - [ ] AC-8: OutboxFactory가 TimeProvider 주입받아 Outbox 생성
- **관련 레이어**: Domain → Application → Adapter-out:mysql
- **의존성**: STORY-102
- **담당 팀**: application-team
- **추정 규모**: L

### STORY-502: 예약 생성/취소 Outbox 연동
- **우선순위**: P1
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: 예약 생성 시 ReservationPersistenceFacade에서 Reservation + Outbox(RESERVATION_CREATED)를 같은 트랜잭션에서 저장
  - [ ] AC-2: 예약 취소 시 Outbox(RESERVATION_CANCELLED)를 같은 트랜잭션에서 저장
  - [ ] AC-3: OutboxProcessor가 RESERVATION_CREATED 타입 처리 시 DB Inventory.available_count 차감
  - [ ] AC-4: OutboxProcessor가 RESERVATION_CANCELLED 타입 처리 시 DB Inventory.available_count 복구
  - [ ] AC-5: Outbox 처리 실패 시 FAILED 상태로 기록 + 좀비 스케줄러가 재시도
- **관련 레이어**: Application → Adapter-out:mysql
- **의존성**: STORY-501, STORY-302
- **담당 팀**: application-team
- **추정 규모**: M

---

## Epic 6: Redis 재고 정합성

> 설계 결정: Redis 원자적 카운터 + 임시 홀드. Redis-DB 정합성 관리 필요

### STORY-601: Redis 재고 초기화 + 홀드 만료 처리
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: 재고/요금 설정 시 Redis 카운터 초기화 (SET inventory:{roomTypeId}:{date} {base_inventory})
  - [ ] AC-2: InventoryRedisPort에 initializeStock, getStock, decrementStock, incrementStock 메서드 정의
  - [ ] AC-3: 임시 홀드(hold:{reservationId}) TTL 만료 시 재고 자동 복구 처리
  - [ ] AC-4: 홀드 만료 복구 스케줄러 또는 Redis Keyspace Notification 기반 구현
- **관련 레이어**: Application → Adapter-out:redis
- **의존성**: STORY-107, STORY-301
- **담당 팀**: application-team
- **추정 규모**: M

### STORY-602: Redis-DB 재고 정합성 배치
- **우선순위**: P1
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: 주기적 배치가 DB Inventory.available_count와 Redis inventory:{roomTypeId}:{date} 값 비교
  - [ ] AC-2: 불일치 시 Redis 값을 DB 기준으로 보정
  - [ ] AC-3: 불일치 건수를 로그에 기록 (모니터링 연계 가능)
  - [ ] AC-4: Redis 장애 시 DB 비관적 락으로 폴백하는 degradation 전략 설계 (구현은 선택)
- **관련 레이어**: Application → Adapter-out:redis → Adapter-out:mysql
- **의존성**: STORY-601
- **담당 팀**: application-team
- **추정 규모**: M

---

## Epic 7: 공통 인프라 및 프로젝트 기반

> 모든 Epic의 선행 조건. Adapter 계층 구현의 기반.

### STORY-701: 프로젝트 멀티모듈 Gradle 세팅
- **우선순위**: P0
- **구현 상태**: 부분 완료 (domain, application, adapter-in/rest-api, adapter-out/persistence-mysql, adapter-out/persistence-redis 모듈 build.gradle.kts 존재)
- **수용기준**:
  - [ ] AC-1: ./gradlew build 성공 (core, domain, application, adapter-in/rest-api, adapter-out/persistence-mysql, adapter-out/persistence-redis, infra 모듈 포함)
  - [ ] AC-2: domain 모듈이 spring, jpa 등 외부 프레임워크에 의존하지 않음 (ArchUnit 검증)
  - [ ] AC-3: application 모듈이 adapter 모듈에 의존하지 않음 (단방향 의존)
  - [ ] AC-4: adapter-out-supplier 모듈 구조 존재
- **관련 레이어**: 전체
- **의존성**: 없음
- **담당 팀**: application-team
- **추정 규모**: M

### STORY-702: Docker Compose 환경 구성
- **우선순위**: P0
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: docker-compose up으로 MySQL 8.x + Redis 컨테이너 기동
  - [ ] AC-2: Spring Boot 애플리케이션이 docker-compose 환경에 연결되어 정상 기동
  - [ ] AC-3: README.md에 실행 방법 기술
- **관련 레이어**: infra
- **의존성**: 없음
- **담당 팀**: application-team
- **추정 규모**: S

### STORY-703: 공통 응답 포맷 및 예외 처리
- **우선순위**: P0
- **구현 상태**: 미구현 (domain/common에 ErrorCode, DomainException 존재)
- **수용기준**:
  - [ ] AC-1: 모든 API 성공 응답이 {"success": true, "data": {...}} 형식
  - [ ] AC-2: DomainException 발생 시 {"success": false, "error": {"code": "ACC-001", "message": "..."}} 형식 + 해당 httpStatus
  - [ ] AC-3: 검증 실패(MethodArgumentNotValidException) 시 400 응답 + 필드별 에러 메시지
  - [ ] AC-4: 예상치 못한 예외(Exception) 시 500 응답 + "INTERNAL_ERROR" 코드
- **관련 레이어**: Adapter-in:rest-api (GlobalExceptionHandler)
- **의존성**: STORY-102
- **담당 팀**: rest-api-team
- **추정 규모**: M

---

## Epic 8: Admin 관리 기능

> 선택 확장: "운영팀을 위한 Admin 관리 기능"

### STORY-801: Admin 숙소 목록/상세 조회 API
- **우선순위**: P1
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: GET /api/v1/admin/properties 요청 시 200 응답 + 페이지네이션된 숙소 목록
  - [ ] AC-2: status 필터 지원 (ACTIVE, INACTIVE, PENDING)
  - [ ] AC-3: GET /api/v1/admin/properties/{id} 요청 시 200 응답 + 숙소 상세 (객실, 요금 포함)
- **관련 레이어**: Application → Adapter-in:rest-api
- **의존성**: STORY-104
- **담당 팀**: rest-api-team
- **추정 규모**: M

### STORY-802: Admin 예약 모니터링 API
- **우선순위**: P1
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: GET /api/v1/admin/reservations 요청 시 200 응답 + 예약 목록 (status 필터, 날짜 범위 필터)
  - [ ] AC-2: 응답에 게스트 정보, 숙소명, 요금, 상태 포함
- **관련 레이어**: Application → Adapter-in:rest-api
- **의존성**: STORY-302
- **담당 팀**: rest-api-team
- **추정 규모**: S

---

## Epic 9: 인증/인가

> 선택 확장: "보안: 인증/인가 설계"

### STORY-901: API 인증/인가 설계 문서
- **우선순위**: P2
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: docs/design/auth-design.md에 Extranet/Customer/Admin 각 API의 인증 방식 기술
  - [ ] AC-2: 파트너는 자신의 숙소만 관리 가능한 인가 규칙 설계
  - [ ] AC-3: JWT 기반 인증 흐름 다이어그램 포함
- **관련 레이어**: 설계 문서
- **의존성**: 없음
- **담당 팀**: application-team
- **추정 규모**: S

---

## Epic 10: 테스트 코드

> 권장 확장: "테스트 코드 (단위 테스트, 통합 테스트 등)"

### STORY-1001: 도메인 단위 테스트
- **우선순위**: P1
- **구현 상태**: 부분 완료 (DomainLayerArchTest 1개 존재)
- **수용기준**:
  - [ ] AC-1: Property 도메인 모델 비즈니스 로직 테스트 (forNew 검증, reconstitute 복원, 상태 변경)
  - [ ] AC-2: Reservation 상태 전이 테스트 (PENDING→CONFIRMED, PENDING→CANCELLED, 불가능한 전이 시 도메인 예외)
  - [ ] AC-3: Rate 계산 로직 테스트 (요일별 가격 — weekday/friday/saturday/sunday, Override 적용)
  - [ ] AC-4: 공통 VO 테스트 (DateRange, Money, DeletionStatus)
  - [ ] AC-5: 순수 Java 테스트 (Spring Context 없이 실행)
  - [ ] AC-6: 테스트 커버리지: 도메인 모델 핵심 로직 80% 이상
- **관련 레이어**: Domain
- **의존성**: STORY-101, STORY-301, STORY-102
- **담당 팀**: domain-team
- **추정 규모**: M

### STORY-1002: Repository 통합 테스트
- **우선순위**: P1
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: Testcontainers(MySQL) 환경에서 Property CRUD 테스트 통과
  - [ ] AC-2: Reservation 저장/조회 테스트 (ReservationItem 포함)
  - [ ] AC-3: Inventory 조회 시 날짜 범위 필터링 정상 동작
  - [ ] AC-4: JPA 관계 어노테이션 없이 독립 Entity 매핑이 정상 동작 검증
- **관련 레이어**: Adapter-out:mysql
- **의존성**: STORY-104
- **담당 팀**: persistence-mysql-team
- **추정 규모**: M

### STORY-1003: API 통합 테스트 (MockMvc)
- **우선순위**: P1
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: 숙소 등록 → 객실 등록 → 요금 설정 → 검색 → 예약 → 취소 E2E 흐름 테스트
  - [ ] AC-2: 에러 응답 포맷 검증 (errorCode, message 필드 존재)
  - [ ] AC-3: MockMvc + Testcontainers(MySQL + Redis) 기반
- **관련 레이어**: Adapter-in:rest-api → Application → Adapter-out:mysql → Adapter-out:redis
- **의존성**: STORY-303
- **담당 팀**: rest-api-team
- **추정 규모**: L

---

## Epic 11: 문서화 및 기록

> 권장 확장: "과정 기록서, AI 활용 기록, API 문서 자동화"

### STORY-1101: API 문서 자동화 (SpringDoc/Swagger)
- **우선순위**: P1
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: /swagger-ui.html 접속 시 API 문서 페이지 표시
  - [ ] AC-2: 모든 API에 요청/응답 예시, 파라미터 설명 포함
  - [ ] AC-3: Extranet / Customer / Admin API가 태그로 그룹핑
- **관련 레이어**: Adapter-in:rest-api
- **의존성**: STORY-105
- **담당 팀**: rest-api-team
- **추정 규모**: S

### STORY-1102: 설계 문서 작성 (아키텍처, ERD, API 명세)
- **우선순위**: P1
- **구현 상태**: 부분 완료 (ERD v2, 도메인 컨벤션, Application 컨벤션, 의사결정 시드 존재)
- **수용기준**:
  - [ ] AC-1: docs/design/architecture.md에 전체 아키텍처 다이어그램 + 모듈 간 의존성 설명
  - [ ] AC-2: docs/design/api-spec.md에 주요 API 명세 (요청/응답 예시 포함)
  - [ ] AC-3: docs/design/concurrency-design.md에 Redis 원자적 카운터 기반 동시성 제어 전략 + 시퀀스 다이어그램
  - [ ] AC-4: docs/design/caching-design.md에 RateRule → Rate → Redis 3단 캐싱 전략
  - [ ] AC-5: 설계 문서에 적은 내용이 실제 코드에 반영되어 있음
- **관련 레이어**: 설계 문서
- **의존성**: 없음
- **담당 팀**: application-team
- **추정 규모**: M

### STORY-1103: 과정 기록서 (Progress Journal) 최종 정리
- **우선순위**: P1
- **구현 상태**: 부분 완료 (seeds/ 시드 데이터 존재)
- **수용기준**:
  - [ ] AC-1: docs/progress-journal.md에 일자별 작업 내용 + 의사결정 + AI 활용 기록 통합
  - [ ] AC-2: 각 의사결정에 "왜 이 방식인지, 대안은 무엇이었는지" 포함
  - [ ] AC-3: AI 활용 기록에 프롬프트 의도, 결과 활용 방식, 본인 판단 추가분 기술
- **관련 레이어**: 문서
- **의존성**: 없음
- **담당 팀**: application-team
- **추정 규모**: S

---

## Epic 12: 모니터링 설계

> 선택 확장: "모니터링 설계 (구현 불필요, 설계만으로도 가능)"

### STORY-1201: 모니터링 설계 문서
- **우선순위**: P2
- **구현 상태**: 미구현
- **수용기준**:
  - [ ] AC-1: docs/design/monitoring-design.md에 모니터링 대상 메트릭 정의 (예약 성공/실패율, 재고 정합성, Supplier 동기화 상태, 응답 시간)
  - [ ] AC-2: 알림 조건 정의 (예: 재고 불일치 > 5건, Supplier 동기화 실패 3회 연속)
  - [ ] AC-3: 로깅 전략 (구조화 로깅, 추적 ID, 로그 레벨 기준) 기술
- **관련 레이어**: 설계 문서
- **의존성**: 없음
- **담당 팀**: application-team
- **추정 규모**: S

---

## 구현 우선순위 요약

### P0 (필수 — 미구현 시 핵심 기능 누락)
| 순서 | 스토리 | 핵심 | 규모 |
|:----:|--------|------|:----:|
| 1 | STORY-701 | Gradle 멀티모듈 세팅 | M |
| 2 | STORY-702 | Docker Compose (MySQL + Redis) | S |
| 3 | STORY-102 | 공통 도메인 VO (DateRange, Money, CacheKey 등) | M |
| 4 | STORY-703 | 공통 응답 포맷 + 예외 처리 | M |
| 5 | STORY-101 | 숙소 도메인 모델 완성 (Partner 포함) | S |
| 6 | STORY-301 | 예약/재고 도메인 모델 | M |
| 7 | STORY-401 | Supplier 도메인 모델 | M |
| 8 | STORY-501 | Outbox 도메인 + 스케줄러 | L |
| 9 | STORY-103 | 숙소 기본정보 등록 UseCase | M |
| 10 | STORY-103a | 숙소 사진 관리 UseCase | S |
| 11 | STORY-103b | 숙소 편의시설 설정 UseCase | S |
| 12 | STORY-103c | 숙소 속성값 설정 UseCase | S |
| 13 | STORY-103d | 객실 등록 UseCase | M |
| 14 | STORY-104 | 숙소 Persistence Adapter | L |
| 15 | STORY-105 | 숙소 기본정보 등록 API (Extranet) | M |
| 16 | STORY-106 | 숙소 부속 정보 API (사진/편의시설/속성값/객실) | L |
| 17 | STORY-107 | 재고/요금 설정 API + Redis 초기화 | L |
| 18 | STORY-601 | Redis 재고 초기화 + 홀드 만료 처리 | M |
| 19 | STORY-201 | 숙소 검색 API (Customer) | L |
| 20 | STORY-202 | 요금 조회 API + 3단 캐싱 | L |
| 21 | STORY-302 | 예약 생성 + Redis 동시성 제어 | L |
| 22 | STORY-303 | 예약 취소 API | M |
| 23 | STORY-304 | 동시성 제어 통합 테스트 | M |
| 24 | STORY-402 | Supplier ACL 구현 | L |
| 25 | STORY-403 | Supplier Diff 동기화 | L |

### P1 (확장-높음 — 차별화 요소)
| 스토리 | 핵심 | 규모 |
|--------|------|:----:|
| STORY-502 | 예약 생성/취소 Outbox 연동 | M |
| STORY-602 | Redis-DB 재고 정합성 배치 | M |
| STORY-801 | Admin 숙소 관리 API | M |
| STORY-802 | Admin 예약 모니터링 API | S |
| STORY-1001 | 도메인 단위 테스트 | M |
| STORY-1002 | Repository 통합 테스트 | M |
| STORY-1003 | API E2E 통합 테스트 | L |
| STORY-1101 | Swagger API 문서 자동화 | S |
| STORY-1102 | 설계 문서 작성 | M |
| STORY-1103 | 과정 기록서 최종 정리 | S |

### P2 (확장-낮음 — 여유 시 진행)
| 스토리 | 핵심 | 규모 |
|--------|------|:----:|
| STORY-901 | 인증/인가 설계 문서 | S |
| STORY-1201 | 모니터링 설계 문서 | S |

---

## 의존성 그래프 (핵심 흐름)

```
STORY-701 (Gradle 세팅)
    ├── STORY-702 (Docker Compose)
    └── STORY-102 (공통 VO)
        ├── STORY-703 (공통 응답/예외)
        ├── STORY-101 (숙소 도메인) ─── STORY-301 (예약 도메인) ─── STORY-401 (Supplier 도메인)
        │       │                              │                            │
        │       ▼                              │                            │
        │   STORY-103 (기본정보 UseCase)       │                            │
        │       ├── STORY-103a (사진 UseCase)  │                            │
        │       ├── STORY-103b (편의시설 UseCase)                           │
        │       ├── STORY-103c (속성값 UseCase)│                            │
        │       └── STORY-103d (객실 UseCase)  │                            │
        │       │                              │                            │
        │       ▼                              │                            │
        │   STORY-104 (Persistence)            │                            │
        │       │                              │                            │
        │       ▼                              │                            │
        │   STORY-105 (기본정보 등록 API)      │                            │
        │   STORY-106 (부속 정보 API)          │                            │
        │       │                              │                            │
        │       ▼                              │                            │
        │   STORY-107 (재고/요금 설정)         │                            │
        │       │                              │                            │
        │       ├──────────────────────────────┤                            │
        │       ▼                              ▼                            ▼
        │   STORY-201 (검색 API)          STORY-302 (예약 생성)        STORY-402 (Supplier ACL)
        │   STORY-202 (요금 3단 캐싱)          │                            │
        │                                      ▼                            ▼
        │                                 STORY-303 (예약 취소)        STORY-403 (Supplier Diff 동기화)
        │                                 STORY-304 (동시성 테스트)
        │
        └── STORY-501 (Outbox + 스케줄러)
                │
                └── STORY-502 (예약 Outbox 연동)
        
        └── STORY-601 (Redis 재고 초기화)
                │
                └── STORY-602 (Redis-DB 정합성 배치)
```
