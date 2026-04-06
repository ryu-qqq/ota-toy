# Application 레이어 테스트 시나리오

> Mockito Mock 기반 단위 테스트로 UseCase 흐름, Factory 생성, Validator 검증, Facade 원자적 처리, Manager 위임을 검증한다.  
> 총 **61개 테스트 클래스** (Supplier 포함)

---

## 테스트 환경

| 항목 | 설정 |
|------|------|
| 프레임워크 | JUnit 5 + Mockito + AssertJ |
| Mock 방식 | `@Mock` + `@InjectMocks`, BDDMockito `given/then` |
| 구조 | `@Nested` + `@DisplayName` 계층화 |
| 외부 의존 | **없음** (Port는 모두 Mock) |

---

## 검증 카테고리

| 카테고리 | 코드 | 검증 내용 |
|----------|------|----------|
| 정상 흐름 | AT-1 | Manager/Validator/Factory/Facade 호출 순서, 반환값 |
| 실패 흐름 | AT-2 | 예외 발생 시 후속 호출 미실행 |
| 호출 순서 | AT-3 | InOrder로 Validator → Factory → Manager/Facade 순서 보장 |
| Facade 원자적 | AT-4 | 트랜잭션 내 persist 호출 검증 |
| 트랜잭션 보상 | AT-5 | 실패 시 Redis 재고 복구 등 보상 로직 |
| Validator | AT-6 | 유효성 검증 성공/실패, Manager 위임 |

---

## Property BC (35개 클래스)

### Service (7개)

| 클래스 | 시나리오 |
|--------|---------|
| RegisterPropertyService | 정상 등록, 검증 실패 시 후속 미호출, 호출 순서 |
| SetPropertyPhotosService | 정상 설정, diff 패턴 (기존 조회→변경→저장) |
| SetPropertyAmenitiesService | 정상 설정, diff 패턴 |
| SetPropertyAttributesService | 정상 설정, diff 패턴 |
| GetPropertyDetailService | 정상 조회, 빈 결과 처리 |
| CustomerSearchPropertyService | 정상 검색, 빈 결과, 호출 순서 |
| ExtranetSearchPropertyService | 정상 검색, 커서 페이지네이션 |

### Factory (5개)

| 클래스 | 시나리오 |
|--------|---------|
| PropertyFactory | Command → Property 변환, TimeProvider 사용 |
| PropertyAmenityFactory | Command → PropertyAmenities 변환, 빈 목록, propertyId 일관성 |
| PropertyAttributeValueFactory | Command → PropertyAttributeValues 변환, 빈 목록 |
| PropertyPhotoFactory | Command → PropertyPhotos 변환, 빈 목록 |
| PropertySearchCriteriaFactory | Extranet/Customer Query → Criteria 변환, null 처리 |

### Validator (4개)

| 클래스 | 시나리오 |
|--------|---------|
| PropertyRegistrationValidator | Partner/PropertyType 존재 검증, 미존재 시 예외 |
| PropertyAmenitiesValidator | Property 존재 검증, 미존재 시 예외 |
| PropertyAttributesValidator | Property 존재 + 필수속성 포함 검증, 누락 시 예외 (누락 ID 목록) |
| PropertyPhotosValidator | Property 존재 검증, 미존재 시 예외 |

### Assembler (1개)

| 클래스 | 시나리오 |
|--------|---------|
| PropertySearchResultAssembler | Customer/Extranet 결과 변환, Summary 필드 매핑, 빈 결과, Bundle→Detail |

---

## Pricing BC (18개 클래스)

### Service (3개)

| 클래스 | 시나리오 |
|--------|---------|
| RegisterRatePlanService | 정상 등록, 검증 실패 시 후속 미호출 |
| SetRateAndInventoryService | 정상 설정, 번들 생성, Redis 초기화 |
| CustomerGetRateService | 정상 조회, 빈 결과 조기 종료, Assembler 인자 검증 |

### Factory (3개)

| 클래스 | 시나리오 |
|--------|---------|
| RatePlanFactory | DIRECT/null 고정, 필드 매핑, TimeProvider |
| RateAndInventoryFactory | RateRule/Override/Rate/Inventory 번들 생성 |
| RateCriteriaFactory | Query→Criteria 필드 매핑, stayDates 계산 |

### Validator (2개)

| 클래스 | 시나리오 |
|--------|---------|
| RatePlanRegistrationValidator | RoomType 존재 검증, 미존재 시 예외 전파 |
| SetRateAndInventoryValidator | RatePlan 존재 검증, 미존재 시 예외 전파 |

### Facade (1개)

| 클래스 | 시나리오 |
|--------|---------|
| RateAndInventoryPersistenceFacade | rateRuleId 할당 후 저장, 저장 순서, 빈 컬렉션 스킵 |

### Assembler (1개)

| 클래스 | 시나리오 |
|--------|---------|
| PropertyRateAssembler | 빈 결과, 재고 미가용 제외, 정상 조립, 캐시 미스 0원 |

### Manager (4개)

| 클래스 | 시나리오 |
|--------|---------|
| RateCacheManager | 전체 캐시 히트, 부분 미스 DB 폴백, 전체 미스, DB 미결과 |
| RatePlanReadManager | getById, verifyExists (성공/실패), findByRoomTypeIds |
| RatePlanCommandManager | Port 위임 persist |
| RateReadManager | Port 위임, 단일 ID 래핑 |

---

## Reservation BC (9개 클래스)

### Service (3개)

| 클래스 | 시나리오 |
|--------|---------|
| CreateReservationSessionService | 세션 생성, 멱등키 중복 시 기존 반환, 검증 실패, Redis 재고 보상 (Factory/DB 실패 시 복구) |
| ConfirmReservationService | PENDING 확정, CONFIRMED 멱등, 세션 미존재, 호출 순서 |
| CancelReservationService | Facade + Redis 복구, 예약 미존재, 세션 미존재, 호출 순서 |

### Validator (2개)

| 클래스 | 시나리오 |
|--------|---------|
| ReservationSessionValidator | Property/RoomType/RatePlan 존재 검증, 미존재 시 예외 |
| CreateReservationValidator | 성공/실패 검증 |

### Factory (2개)

| 클래스 | 시나리오 |
|--------|---------|
| ReservationSessionFactory | 필드 매핑, 상태 PENDING, id null, 멱등키, TimeProvider |
| ReservationFactory | create/createFromSession: 필드 매핑, 예약번호 접두사, 라인 생성, 상태 |

### Facade (2개)

| 클래스 | 시나리오 |
|--------|---------|
| ReservationPersistenceFacade | persist 성공/호출 순서/재고 부족, confirmReservation 성공/순서/재고 부족 |
| CancelReservationFacade | 취소 처리, 호출 순서, 이미 취소된 예약, 후속 미호출 |

---

## RoomType BC (5개 클래스)

| 클래스 | 시나리오 |
|--------|---------|
| RegisterRoomTypeService | 정상 등록, 검증 실패, 호출 순서 |
| RoomTypeFactory | TimeProvider, 번들 생성, Pending 상태, null beds/views |
| RoomTypeRegistrationValidator | Property 존재 검증, 미존재 시 예외 전파 |
| RoomTypePersistenceFacade | ID 할당 후 Bed/View 저장, 호출 순서, 빈 번들 |
| RoomTypeReadManager | getById, verifyExists, findByPropertyId, findActive |

---

## Inventory BC (3개 클래스)

| 클래스 | 시나리오 |
|--------|---------|
| InventoryClientManager | Redis 정상, DB 폴백 (Connection/Timeout), 비즈니스 예외 전파, increment/initialize 예외 삼킴 |
| InventoryCommandManager | 다중 날짜 차감, 부분 실패 시 롤백, 복구 |
| InventoryReadManager | Port 위임 조회, 빈 결과 |

---

## Partner BC (1개 클래스)

| 클래스 | 시나리오 |
|--------|---------|
| PartnerReadManager | getById, verifyExists (성공/실패) |

---

## PropertyType BC (1개 클래스)

| 클래스 | 시나리오 |
|--------|---------|
| PropertyTypeReadManager | getById, verifyExists, getRequiredAttributeIds (필수만 필터링, 빈 결과) |

---

## Supplier BC (11개 클래스)

### Service (3개)

| 클래스 | 시나리오 |
|--------|---------|
| CreateSupplierTaskService | 후보 Task 생성, 중복 제거, 빈 결과 |
| ExecuteSupplierTaskService | 정상 흐름 (Facade 호출), 실패 시 markFailed |
| ProcessSupplierRawDataService | RawData 처리 흐름 |

### Factory/Handler (2개)

| 클래스 | 시나리오 |
|--------|---------|
| SupplierTaskFactory | Task 생성, supplierId/taskType 매핑 |
| SupplierTaskFollowUpHandler | 후속 Task 생성 로직 |

### Manager (6개)

| 클래스 | 시나리오 |
|--------|---------|
| SupplierReadManager | Port 위임, 상태별 조회 |
| SupplierApiConfigReadManager | Port 위임, 활성 설정 조회 |
| SupplierRawDataReadManager | Port 위임, 상태별 조회 |
| SupplierRawDataTransactionManager | 트랜잭션 내 상태 전이 |
| SupplierTaskCommandManager | Port 위임, persist/update |
| SupplierTaskReadManager | Port 위임, 진행 중 Task 조회 |

---

## 핵심 검증 패턴 요약

| 패턴 | 적용 위치 | 설명 |
|------|----------|------|
| **호출 순서 보장** | 모든 Service | InOrder: Validator → Factory → Manager/Facade |
| **실패 시 미호출** | 모든 Service | 예외 발생 후 후속 단계 `never()` 검증 |
| **Redis 재고 보상** | CreateReservationSession, Cancel | Factory/DB 실패 시 `incrementStock` 복구 |
| **캐시 히트/미스** | RateCacheManager | 전체 히트 → DB 미호출, 부분 미스 → DB 폴백 |
| **diff 패턴** | SetPhotos/Amenities/Attributes | 기존 조회 → 새 생성 → update() → persist |
| **번들 패턴** | RoomType, RateAndInventory | 부모 ID 할당 후 자식 일괄 저장 |
| **Port 위임** | 모든 Manager | Port 메서드 호출 + 결과 래핑/변환 검증 |
