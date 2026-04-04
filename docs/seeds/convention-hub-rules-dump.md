# Convention Hub 기존 규칙 참조

## Application Layer 규칙

### APP-PKG-001: Application DTO vs Internal 패키지 경계 규칙 [BLOCKER]

dto/ 패키지에는 UseCase 인터페이스 시그니처에 등장하는 타입만 위치한다. 내부 처리용 객체는 internal/ 패키지에 위치한다.

■ dto/ 패키지 (외부 계약)
- dto/command/: UseCase 메서드 파라미터 (record 필수)
- dto/query/: 조회 UseCase 파라미터 (record 필수)
- dto/response/: UseCase 반환 타입 (record 필수), Composite 응답 포함
- record 필수, 인스턴스 메서드 금지, 정적 팩토리 메서드만 허용
- composite/ 서브패키지는 사용하지 않고 response/에 통합

■ internal/ 패키지 (내부 처리)
- BatchResult: 배치 결과 수집기 (class 허용)
- PersistenceBundle: 영속화 파라미터 (record)
- PersistenceFacade: 트랜잭션 조율 (class)
- *Bundle: Factory/Coordinator 내부 번들
- *ReadBundle: ReadFacade→Assembler 중간 데이터
- OutboxRelayProcessor: Outbox 발행 (class)
- Coordinator, Helper: 업무 흐름 조율/보조 로직

■ 판별 기준
이 타입이 UseCase 인터페이스의 파라미터나 반환 타입으로 쓰이는가?
- YES → dto/
- NO → internal/

**근거:** dto/ 패키지에 내부 처리용 mutable 객체(BatchResult, Bundle 등)가 혼재하면 APP-DTO-001(record 필수) 규칙과 충돌하고, 패키지의 역할이 모호해진다. UseCase 시그니처 기준으로 분리하면 dto/는 순수한 외부 계약만 담당하고, internal/은 구현 세부사항을 캡슐화한다.

---

### APP-FCD-001: PersistenceFacade Bundle 패턴 [CRITICAL]

PersistenceFacade는 PersistenceBundle record를 파라미터로 받는 persistAll(Bundle) 단일 메서드만 제공한다. 저장 대상 조합별로 개별 메서드를 만들지 않는다. Bundle은 정적 팩토리 메서드(of, withOrderItems 등)로 빈 목록을 List.of()로 채워 null-safe를 보장한다.

**근거:** PersistenceFacade에 저장 대상 조합별로 persistWithOutbox, persistAllWithOutboxesAndHistories 같은 메서드를 개별로 만들면 조합이 추가될 때마다 메서드가 폭발적으로 늘어난다. Bundle 패턴으로 단일 메서드를 유지하면 Facade의 인터페이스가 안정적으로 유지되고, 새로운 저장 대상 추가 시 Bundle record만 확장하면 된다.

---

### APP-PRT-003: Port 유형별 적용 범위 명확화 [MAJOR]

Port는 용도에 따라 Persistence Port와 Client Port로 구분되며, 각각 다른 규칙이 적용된다.

■ Persistence Port (DB 영속화)
- CommandPort: persist/persistAll만 허용 (APP-PRT-001)
- QueryPort: findAll 금지, 페이징/조건부 조회만 (APP-PRT-001)
- 파라미터: Domain VO(OrderId 등) 또는 Domain Aggregate 허용 (DB 내부 저장이므로)

■ Client Port (외부 시스템 연동)
- SalesChannelProductClient, ExternalPaymentClient 등
- 메서드명 제약 없음 (registerProduct, updateProduct 등 비즈니스 요구에 따라 자유)
- 파라미터/반환 타입: Application DTO만 허용, Domain Aggregate 직접 전달 금지 (APP-PORT-001)

■ 규칙 매핑
| 규칙 | Persistence Port | Client Port |
|------|:---:|:---:|
| APP-PRT-001 (메서드 제약) | 적용 | 미적용 |
| APP-PRT-002 (네이밍/VO 파라미터) | 적용 | 네이밍 권장, 파라미터는 DTO |
| APP-PORT-001 (Aggregate 전달 금지) | 미적용 | 적용 |

**근거:** APP-PRT-001의 persist/findAll 제약은 JPA Dirty Checking 기반이므로 Persistence Port에만 유효하다. Client Port에 동일 제약을 적용하면 외부 시스템 연동 메서드명이 부자연스러워진다. 반면 APP-PORT-001의 Domain Aggregate 전달 금지는 레이어 경계 보호 목적이므로 Client Port에만 적용된다(Persistence Port는 DB 저장을 위해 Aggregate 전달이 자연스럽다). 규칙 간 적용 범위를 명시하여 혼동을 방지한다.

---

### APP-CRS-001: 크로스 도메인 명시적 호출 규칙 [BLOCKER]

이벤트 발행(Spring Event) 대신 명시적 호출을 사용한다. 이벤트는 메모리 기반으로 유실 위험이 있고 디버깅이 어렵다.

■ 허용 범위
1. 조회(ReadManager): 어떤 컴포넌트에서든 다른 도메인의 ReadManager 의존 허용. Validator에서 중복 클레임 확인 등.
2. 쓰기(CommandManager): 같은 Bounded Context 내에서만 다른 도메인의 CommandManager 허용. Order↔Shipment, Order↔OrderItem.
3. 다른 Bounded Context의 쓰기: 대상 도메인의 Factory + PersistenceFacade를 직접 사용. UseCase(Port-In)나 Service 호출은 금지(순환 의존성 위험).
4. 공유 로직(Helper): 여러 도메인에서 공통으로 쓰는 로직은 Helper(internal/)로 추출.

■ 금지 사항
- UseCase(Port-In) 인터페이스를 통한 다른 도메인 호출 (순환 의존성)
- Service 클래스 직접 의존 (순환 + 계층 위반)
- Spring Event 발행 (ApplicationEventPublisher 메모리 유실 위험)

**근거:** Spring Event는 메모리 기반으로 애플리케이션 장애 시 이벤트가 유실될 위험이 있다. UseCase(Port-In) 호출은 도메인 간 순환 의존성을 유발할 수 있다(A→B UseCase→B Service, B→A UseCase→A Service). Factory+PersistenceFacade 직접 사용은 순환 없이 대상 도메인의 생성/검증/저장 로직을 재사용할 수 있으며, 호출 흐름이 명시적이어서 디버깅이 용이하다.

---

### APP-DEP-001: Application 의존성 규칙 [BLOCKER]

Service→Internal(Coordinator)/Manager만 의존. Coordinator→같은 CQRS 범위 Manager만(CommandCoordinator→QueryManager 금지 = CQRS 위반). Internal 컴포넌트(Calculator, Resolver 등)→Manager만 의존(Port 직접 금지). Validator→단일 도메인 ReadManager만.

**근거:** CQRS 원칙과 트랜잭션 경계를 보장하기 위해 계층 간 의존성을 엄격히 제한한다. Port를 직접 호출하면 트랜잭션 없이 DB에 접근하게 되므로 Manager를 경유해야 한다.

---

### APP-DTO-001: Command/Query DTO 규칙 [BLOCKER]

Application 레이어의 Command/Query DTO는 반드시 Java record로 선언한다. 인스턴스 메서드는 금지하며 정적 팩토리 메서드만 허용한다. 데이터 컨테이너 순수성을 유지한다.

**근거:** Record는 불변성을 기본 보장하고 equals/hashCode/toString을 자동 생성하여 DTO에 최적이다. 인스턴스 메서드는 DTO의 역할 경계를 흐린다.

---

### APP-DTO-002: 조회 DTO 표준 패턴 [CRITICAL]

SearchParams record는 CommonSearchParams를 필수 포함(Offset 기반). CursorParams record는 CommonCursorParams를 필수 포함(Cursor 기반). Bundle DTO는 withId(parentId) 정적 팩토리로 상위-하위 ID를 연결한다.

**근거:** 페이지네이션 파라미터 표준화로 API 일관성을 보장하고, Bundle의 withId 패턴으로 상위-하위 관계를 캡슐화한다.

---

### APP-EVT-001: 이벤트 발행 규칙 [BLOCKER]

ApplicationEventPublisher 직접 주입 금지. 반드시 TransactionEventRegistry를 사용하여 트랜잭션 커밋 후 이벤트를 발행한다. 트랜잭션 롤백 시 이벤트가 발행되지 않아 데이터 일관성을 보장한다.

**근거:** ApplicationEventPublisher로 직접 발행하면 트랜잭션 롤백 시에도 이벤트가 전달되어 데이터 불일치가 발생한다.

---

### APP-EXC-001: 도메인별 구체 예외 클래스 + ErrorCode 사용 [CRITICAL]

범용 예외(IllegalArgumentException, IllegalStateException 등) 대신 도메인별 구체 예외 클래스(XXXException extends DomainException)를 만들어 도메인별 ErrorCode enum과 함께 사용한다. DomainException을 직접 던지지 않고, ShipmentException(ShipmentErrorCode.XXX), OrderException(OrderErrorCode.XXX) 등 도메인별 구체 예외를 생성하여 던진다. ErrorCode는 도메인별로 정의하여(OrderErrorCode, ShipmentErrorCode, ProductErrorCode) 에러 추적을 명확히 한다.

**근거:** 범용 예외는 에러 원인 추적이 어렵고 ErrorMapper에서 적절한 HTTP 응답으로 변환할 수 없다. DomainException을 직접 던지면 어떤 도메인에서 발생한 예외인지 구분이 불가능하다. 도메인별 구체 예외 클래스를 사용하면 catch 블록에서 도메인별 분기가 가능하고, ErrorMapper에서 도메인별 HTTP 상태 코드 매핑이 명확해진다.

---

### APP-FAC-001: Factory 사용 원칙 [CRITICAL]

외부 의존성(TimeProvider 등) 필요 시에만 Factory를 도입한다(단순 매핑은 불필요). Factory 메서드에는 DTO를 통째로 전달하여 변경 내성을 확보한다. UpdateContext 패턴으로 ID+UpdateData+changedAt을 한 번에 생성한다.

**근거:** Factory의 과도한 도입은 불필요한 추상화를 만든다. DTO 통째 전달은 필드 추가/변경 시 Factory 시그니처 수정을 최소화한다.

---

### APP-FAC-002: Factory 금지 사항 [CRITICAL]

Factory에서 null 체크/기본값 설정 금지(도메인 불변 조건은 Domain 계층 책임). Factory에서 의사결정 로직(if-else 분기로 다른 타입 생성) 금지(비즈니스 결정은 Domain Service 책임).

**근거:** Factory는 순수 조립(assembly) 책임만 갖는다. 검증과 의사결정을 Factory에 넣으면 Domain 계층의 책임이 유출된다.

---

### APP-FAC-003: Factory TimeProvider.now() 공개 노출 금지 [BLOCKER]

Factory에서 TimeProvider.now()를 public 메서드로 노출하지 않는다. Factory.now() 같은 메서드는 Service에서 시간을 직접 꺼내 쓰는 우회 경로가 되어 APP-TIM-002(StatusChangeContext 패턴)를 무력화한다. 시간이 필요한 경우 Factory는 StatusChangeContext/UpdateContext를 생성하여 반환하거나, 도메인 상태 변경을 Factory 내부에서 직접 수행해야 한다.

**근거:** Factory.now()가 존재하면 Service에서 commandFactory.now()로 시간을 획득하게 되어, TimeProvider가 Factory에 일원화된다는 원칙이 형식적으로만 지켜질 뿐 실질적으로 Service가 시간을 자유롭게 사용하는 것과 같다. 시간 생성 지점이 분산되어 테스트에서 시간 제어가 어려워지고, 동일 트랜잭션 내 시간 일관성이 깨질 수 있다.

---

### APP-PORT-001: Port-Out 시그니처에 Domain Aggregate 직접 전달 금지 [CRITICAL]

Port-Out 인터페이스의 파라미터 또는 반환 타입에 Domain Aggregate(Entity, Aggregate Root)를 직접 사용하지 않는다. Port를 넘어 adapter-out으로 전달되는 데이터는 반드시 Application 레이어의 DTO(Result, Command 등 record 기반)로 변환한 후 전달해야 한다. Bundle 타입이 Domain Aggregate를 필드로 포함하는 경우, 해당 Bundle을 Port 시그니처에 사용하면 안 된다. 내부 조립용 Bundle과 외부 전달용 DTO의 책임을 명확히 분리한다.

**근거:** Domain Aggregate가 Port를 넘어 adapter-out까지 직접 전달되면: (1) adapter-out이 Domain 내부 구조에 강하게 결합되어 Domain 변경 시 adapter-out까지 영향이 전파됨, (2) 내부 조립용 데이터 구조와 외부 전달용 데이터 구조의 관심사가 혼합되어 변경 사유가 불명확해짐, (3) Domain Aggregate의 메서드가 adapter-out에서 호출 가능해져 레이어 경계가 무력화됨.

---

### APP-PRT-001: Port 메서드 제약 [BLOCKER]

CommandPort는 persist/persistAll만 허용(JPA Dirty Checking 활용, delete/update 메서드 불필요). QueryPort에 findAll 금지(OOM 위험). 반드시 페이징/조건부 조회만 선언한다.

**근거:** persist만 사용하면 JPA의 변경 감지로 update가 자동 처리되어 불필요한 인터페이스를 줄인다. findAll은 데이터 증가 시 OOM을 유발한다.

---

### APP-SVC-001: UseCase 1:1 Service 구현 [CRITICAL]

UseCase 인터페이스와 Service 구현체는 1:1로 매핑한다. Service는 UseCase 메서드를 구현하고 Internal(Coordinator)/Manager에 위임한다. 하나의 Service에 여러 UseCase를 구현하지 않는다.

**근거:** SRP를 강제하여 Service가 비대해지는 것을 방지한다. UseCase별 독립적인 테스트와 배포가 가능해진다.

---

### APP-TIM-001: TimeProvider Factory에서만 [BLOCKER]

Instant.now()/LocalDateTime.now() 직접 호출 금지. TimeProvider를 Factory에 주입하여 시간을 생성한다. Service/Manager에서 직접 시간을 생성하면 테스트에서 시간 제어가 불가능해진다.

**근거:** 시간 생성을 Factory로 일원화하면 테스트에서 TestTimeProvider로 교체하여 시간 의존 로직을 결정적(deterministic)으로 검증할 수 있다.

---

### APP-TIM-002: Service에서 TimeProvider 주입/호출 금지 [BLOCKER]

Service/Manager에서 TimeProvider를 주입하거나 timeProvider.now()를 호출하지 않는다. 시간이 필요한 상태 변경은 CommandFactory에서 StatusChangeContext<ID>(id, changedAt)를 생성하여 Service에 전달한다. 여러 필드 업데이트가 필요하면 UpdateContext<ID, UPDATE_DATA>(id, updateData, changedAt)를 사용한다. Service는 Context에서 changedAt()을 꺼내 도메인 메서드에 전달만 한다.

**근거:** TimeProvider를 Factory에 일원화하면 시간 생성 지점이 단일화되어 테스트에서 TestTimeProvider 교체가 용이하다. Service에 TimeProvider가 주입되면 Factory의 역할이 모호해지고, timeProvider.now()가 여러 곳에서 호출되어 시간 일관성이 깨질 수 있다. StatusChangeContext/UpdateContext 패턴으로 ID+시간(+업데이트데이터)을 한 번에 캡슐화하면 Service가 thin하게 유지된다.

---

### APP-TRX-001: 트랜잭션 경계 규칙 [BLOCKER]

Service(@Transactional 금지) → Manager(@Transactional 필수, 메서드 단위) → ClientManager(@Transactional 금지, 네트워크 지연 중 커넥션 점유 방지). Service는 조율만 담당하고 트랜잭션 관리는 Manager에 위임한다.

**근거:** Service에 트랜잭션을 걸면 호출 체인 전체가 단일 트랜잭션에 묶여 커넥션 점유 시간이 길어지고 장애 전파 위험이 커진다. ClientManager에 트랜잭션을 걸면 외부 API 호출 중 DB 커넥션을 점유하여 풀 고갈이 발생한다.

---

### APP-VAL-001: Validator 설계 규칙 [CRITICAL]

Validator는 단일 도메인 ReadManager만 의존한다(SRP). 검증 결과로 Domain 객체를 반환하여 후속 로직에서 재조회를 방지한다. 예외는 DomainException(ErrorCode)만 사용한다.

**근거:** Validator가 여러 도메인에 의존하면 복합 결합이 발생한다. Domain 반환으로 validateAndGet 패턴을 구현하여 효율성을 높인다.

---

### APP-ASM-001: Assembler 변환 규칙 [CRITICAL]

Assembler는 도메인별 구체 Result 클래스(OrderSliceResult 등)를 사용하여 타입 안전성을 확보한다. 생성 UseCase는 원시타입(Long) ID를 반환하여 불필요한 재조회를 방지한다.

**근거:** 구체 Result 타입으로 컴파일 타임 타입 체크를 활용하고, 생성 후 ID만 반환하면 추가 SELECT가 불필요하다.

---

### APP-LSN-001: EventListener 규칙 [CRITICAL]

EventListener는 @Async 사용을 권장하여 트랜잭션 격리와 장애 격리를 달성한다. EventListener는 Manager만 의존한다(Port/Repository 직접 접근 금지).

**근거:** @Async로 호출자 트랜잭션과 분리하여 리스너 실패가 원본 트랜잭션에 영향을 주지 않는다. Manager 경유로 트랜잭션 경계를 보장한다.

---

## API Layer 규칙

### API-CTR-001: Controller 아키텍처 원칙 [BLOCKER]

@RestController 필수. UseCase 인터페이스만 의존(구체 Service 직접 주입 금지). @Transactional 사용 금지(Application 계층 책임). @DeleteMapping 금지(Soft Delete → PATCH /{id}/delete). 비즈니스 로직 금지(Mapper에 위임, stream().map() 등 인라인 변환 불가).

**근거:** Controller는 Thin Layer로서 HTTP 요청/응답 변환만 담당한다. 헥사고날 아키텍처에서 Adapter-In은 UseCase Port에만 의존해야 한다.

---

### API-CTR-002: Controller 응답 형식 [BLOCKER]

모든 응답은 ResponseEntity<ApiResponse<T>>로 래핑한다(POST=201 CREATED, GET/PATCH=200 OK). List 직접 반환 금지 → SliceApiResponse/PageApiResponse로 페이지네이션 메타 정보를 포함한다.

**근거:** 일관된 응답 형식으로 클라이언트가 예측 가능한 API를 사용하고, List 직접 반환은 페이지네이션 메타데이터 부재로 클라이언트 구현을 어렵게 한다.

---

### API-DTO-001: API DTO 기본 규칙 [CRITICAL]

Request/Response DTO는 반드시 Java record로 선언한다. Nested Record 허용(CustomerInfo, OrderItemInfo 등 구조화). Optional 사용 금지 → @Nullable로 표현(직렬화 이슈 방지). List 필드는 Compact Constructor에서 List.copyOf()로 방어적 복사.

**근거:** Record로 불변성을 보장하고, Optional은 Jackson 직렬화에서 문제를 일으키므로 @Nullable이 적합하다.

---

### API-ERR-001: 에러 처리 아키텍처 [CRITICAL]

GlobalExceptionHandler(@RestControllerAdvice)에서 ErrorMapper로 위임하여 예외를 ProblemDetail(RFC 7807)로 변환한다. ErrorMapper는 supports()+map() 인터페이스로 예외 타입별 매핑을 캡슐화한다. Controller에서 try-catch 금지.

**근거:** 전역 예외 처리로 일관된 에러 응답을 보장하고, ErrorMapper 패턴으로 도메인별 예외 매핑을 캡슐화한다.

---

### API-MAP-001: ApiMapper 구조 규칙 [CRITICAL]

@Component 필수(Spring Bean DI). 양방향 변환 지원: Request→Command/Query, Result/Domain→Response. 순수 변환 로직만 담당(Repository/Service 의존 금지, 비즈니스 로직 금지).

**근거:** Mapper는 데이터 변환 책임만 가지며, 외부 의존성이 있으면 테스트가 어려워지고 순환 참조 위험이 생긴다.

---

### API-TST-001: 통합 테스트 규칙 [CRITICAL]

MockMvc 사용 금지. @SpringBootTest(webEnvironment=RANDOM_PORT) + TestRestTemplate으로 실제 HTTP 요청/응답을 검증한다. 제네릭 응답은 ParameterizedTypeReference로 타입 보존.

**근거:** MockMvc는 실제 서블릿 컨테이너를 사용하지 않아 필터/인터셉터/직렬화 동작이 실제와 다를 수 있다. TestRestTemplate은 실 환경과 동일한 HTTP 통신을 보장한다.

---

## Persistence Layer 규칙

### PER-ADM-001: 모듈 분리 규칙 [BLOCKER]

도메인 persistence-mysql 모듈에서 조인 쿼리 금지(단일 Aggregate 범위만 처리). 복잡한 크로스 도메인 조인은 persistence-mysql-admin(또는 composite 패키지)에서만 수행하며 반드시 DTO Projection으로 반환한다.

**근거:** OLTP(도메인)와 OLAP(admin/composite)를 분리하여 운영 안정성을 확보하고, DTO Projection으로 영속성 컨텍스트 오염을 방지한다.

---

### PER-ADP-001: Adapter CQRS 의존성 [BLOCKER]

CommandAdapter는 JpaRepository만 의존한다(QueryDslRepository 주입 금지). QueryAdapter는 QueryDslRepository만 의존한다(JpaRepository 주입 금지). 각 Adapter가 CQRS 경계를 넘지 않도록 강제한다.

**근거:** Adapter에서 CQRS 경계를 강제하면 Command와 Query의 데이터 접근 패턴이 명확히 분리된다.

---

### PER-CFG-001: JPA 설정 규칙 [BLOCKER]

OSIV(open-in-view) 반드시 false. 커넥션을 View 렌더링까지 점유하면 풀 고갈로 서비스 장애가 발생한다. ddl-auto는 validate만 허용. 스키마 변경은 Flyway로만 관리한다.

**근거:** OSIV=true는 API 서버에서 커넥션 점유 시간을 불필요하게 연장한다. ddl-auto=update는 운영 환경에서 예기치 않은 ALTER TABLE과 장시간 잠금을 유발한다.

---

### PER-CND-001: ConditionBuilder 규칙 [BLOCKER]

BooleanExpression 조건은 ConditionBuilder 클래스의 static 메서드로 분리한다(null 안전 처리 포함). 모든 조회 쿼리에 deletedAt IS NULL 조건을 반드시 포함한다(notDeleted() 메서드 활용).

**근거:** 조건 로직을 재사용하고 Soft Delete된 데이터 노출을 원천 방지한다. deletedAt 누락은 삭제된 데이터가 사용자에게 노출되는 치명적 결함이다.

---

### PER-ENT-001: JPA Entity 규칙 [BLOCKER]

JPA 관계 어노테이션(@OneToMany, @ManyToOne, @OneToOne, @ManyToMany, @JoinColumn, @JoinTable) 사용 금지. Long FK 전략으로 N+1 문제를 원천 차단한다. 모든 Entity는 BaseAuditEntity 또는 SoftDeletableEntity를 상속하여 audit 필드를 표준화한다.

**근거:** Long FK로 Entity 간 직접 참조를 제거하면 도메인 경계가 명확해지고 N+1이 불가능해진다. BaseAuditEntity 상속으로 감사 추적 데이터 누락을 방지한다.

---

### PER-REP-001: CQRS Repository 분리 [BLOCKER]

JpaRepository는 save/saveAll만 허용하며 @Query/findBy*/deleteBy* 등 커스텀 메서드를 추가하지 않는다. 모든 조회는 QueryDslRepository에서 JPAQueryFactory로 수행한다. JPQL, Native Query 사용 금지.

**근거:** CQRS 원칙에 따라 Command(JpaRepository save)와 Query(QueryDslRepository)를 완전 분리하여 각 경로의 최적화와 독립적 진화를 가능하게 한다.
