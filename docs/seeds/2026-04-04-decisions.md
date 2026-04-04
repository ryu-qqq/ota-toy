# 의사결정 시드 — 2026-04-04

## Decision 5: Outbox를 BC별 전용 테이블로 분리
- **시점**: PL 정합성 점검에서 ERD Outbox 누락 발견 후
- **선택**: Reservation, Supplier 각 BC별 전용 Outbox 테이블 (ReservationOutbox, SupplierOutbox)
- **대안**:
  - (A) 공통 Outbox 1개 — 단순하지만 이벤트 유형별 파티셔닝 불가, 스키마가 범용적이라 타입 안전성 부족
  - (B) BC별 전용 Outbox ✅ 선택
- **이유**: 예약 이벤트와 공급자 이벤트는 소비 주기와 재처리 정책이 다름. 전용 테이블로 분리하면 각 BC의 스케줄러가 독립적으로 폴링할 수 있고, 장애 시 영향 범위가 격리됨. 향후 Kafka 확장 시 토픽 매핑도 자연스러움
- **출처**: PL 정합성 점검 MAJOR-1 (ERD Outbox 누락) → ERD 보강 시 설계 판단

## Decision 6: 금액 타입을 int 대신 BigDecimal로 통일
- **시점**: PL 정합성 점검에서 Money int vs BigDecimal 불일치 발견 후
- **선택**: 모든 금액 필드를 BigDecimal로 통일
- **대안**:
  - (A) int (원 단위 정수) — 국내 원화만 다루면 충분, 성능 유리
  - (B) BigDecimal ✅ 선택
- **이유**: Supplier 통합 시 해외 공급자(해외 OTA)의 다통화 요금이 유입됨. USD 등 소수점 이하 금액 표현이 필수. int로는 센트 단위 변환 로직이 추가되어 복잡도만 증가. BigDecimal은 정밀도와 반올림 정책을 명시적으로 제어 가능
- **출처**: PL 정합성 점검 MAJOR-2 (Money int vs BigDecimal)

## Decision 7: Manager 레이어 유지, Service @Transactional 금지
- **시점**: 도메인 온보딩 리뷰에서 트랜잭션 경계 논의 중
- **선택**: Manager 레이어를 UseCase(Application)와 Domain 사이에 유지. Service(UseCase) 레이어에는 @Transactional을 붙이지 않고 Manager에서만 트랜잭션 관리
- **대안**:
  - (A) Service에 @Transactional — 단순하지만 긴 트랜잭션 발생, 락 해제 지연
  - (B) Manager 레이어에서 트랜잭션 분리 ✅ 선택
- **이유**: 예약 생성 시 재고 차감과 예약 저장은 하나의 트랜잭션이지만, Supplier 알림이나 캐시 갱신은 별도 트랜잭션이어야 함. Service에 @Transactional을 걸면 전체가 하나의 긴 트랜잭션이 되어 비관적 락 보유 시간이 길어짐. Manager 레이어로 트랜잭션 단위를 쪼개면 DB 커넥션 점유를 최소화할 수 있음

## Decision 8: Entity setter 전면 금지 + create() 팩토리 + Lombok 금지
- **시점**: 도메인 온보딩 리뷰에서 Entity 일관성 논의 중
- **선택**: Entity에 setter 없이 create() 정적 팩토리 메서드로 생성, Lombok 사용 금지
- **대안**:
  - (A) Lombok @Builder — 편리하지만 불변 의도가 드러나지 않고 필수 필드 누락 가능
  - (B) setter + validation — setter가 존재하면 외부에서 상태 변경 가능
  - (C) create() 팩토리 + 명시적 메서드 ✅ 선택
- **이유**: 도메인 모델의 상태 변경은 반드시 비즈니스 의미가 있는 메서드(confirm(), cancel() 등)를 통해야 함. setter가 있으면 어디서든 상태를 바꿀 수 있어 불변성 보장 불가. Lombok을 금지하는 이유는 JPA 엔티티에서 @Data, @Setter 등이 관행적으로 사용되어 도메인 모델 오염을 유발하기 때문. marketplace 프로젝트의 Entity 패턴을 참조하여 결정

## Decision 9: Outbox + 스케줄러로 이벤트 발행 (Spring ApplicationEvent 미사용)
- **시점**: 이벤트 기반 설계 방향 결정 시
- **선택**: Outbox 테이블에 이벤트 저장 후 스케줄러 2개(Reservation, Supplier)가 폴링하여 발행
- **대안**:
  - (A) Spring ApplicationEvent — 단순하지만 트랜잭션 커밋 전 이벤트 발행 시 일관성 깨짐, 프로세스 장애 시 이벤트 유실
  - (B) Outbox + 스케줄러 ✅ 선택
- **이유**: 예약 생성 → 재고 차감이 원자적이어야 하는데, ApplicationEvent는 @TransactionalEventListener로 커밋 후 발행하더라도 발행 실패 시 복구 수단이 없음. Outbox 패턴은 이벤트를 DB에 저장하므로 트랜잭션과 같은 원자성을 보장하고, 스케줄러가 폴링하므로 장애 후 자동 재처리 가능
- **출처**: PO 백로그 재검증 (Outbox Story 추가) + PL 정합성 점검 결과를 종합
