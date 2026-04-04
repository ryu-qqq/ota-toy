# Reservation BC -- Code Review 보고서

> 리뷰 일시: 2026-04-04
> 모드: review (기존 코드 검증)
> 리뷰어: domain-code-reviewer

---

## 대상 파일 (12개)

| 파일 | 유형 |
|------|------|
| Reservation.java | Aggregate Root |
| ReservationItem.java | Entity |
| GuestInfo.java | VO (record) |
| ReservationNo.java | VO (record) |
| ReservationId.java | ID VO (record) |
| ReservationItemId.java | ID VO (record) |
| ReservationStatus.java | Enum VO |
| ReservationErrorCode.java | ErrorCode enum |
| ReservationException.java | 기반 예외 |
| InvalidReservationStateException.java | 구체 예외 |
| ReservationAlreadyCancelledException.java | 구체 예외 |
| ReservationAlreadyCompletedException.java | 구체 예외 |

---

## 체크리스트 결과

| # | 규칙 | 판정 | 심각도 |
|---|------|:----:|:------:|
| 1 | DOM-AGG-001: 팩토리 메서드 패턴 | PASS | - |
| 2 | DOM-AGG-002: Aggregate Root ID VO | PASS | - |
| 3 | DOM-AGG-004: Setter 금지 + 비즈니스 메서드 | PASS | - |
| 4 | DOM-AGG-010: equals/hashCode ID 기반 | PASS | - |
| 5 | DOM-VO-001: VO Record + of() + Compact Constructor | PASS | - |
| 6 | DOM-VO-002: Enum displayName() | PASS | - |
| 7 | DOM-ERR-001: ErrorCode enum 구조 | PASS | - |
| 8 | DOM-EXC-001: Exception 기본 구조 | PASS | - |
| 9 | DOM-CMN-002: 외부 레이어 의존 금지 | PASS | - |
| 10 | DOM-TIME: 시간 타입 적합성 | PASS | - |
| 11 | DOM-TIME: Instant.now() 직접 호출 금지 | PASS | - |

---

## MINOR 이슈 (FIX 불필요)

### M1: ReservationItemId -- 하위 엔티티에 ID VO 사용
- **위치**: ReservationItem.java, ReservationItemId.java
- **내용**: 컨벤션 DOM-AGG-002에서 "하위 엔티티는 Long 허용"으로 보일러플레이트 감소를 권장했으나, ID VO를 사용 중
- **영향**: 기능에 문제 없음. 일관성 측면에서는 긍정적

### M2: cancel() 상태 검증 중복 로직
- **위치**: Reservation.java:101-113
- **내용**: CANCELLED 체크 -> COMPLETED 체크 -> PENDING/CONFIRMED 아닌 경우 체크. NO_SHOW만 걸리는 마지막 분기가 존재하여 약간 장황
- **영향**: 로직 정확성에 문제 없음. 각 상태별 구체 예외를 던지기 위한 의도적 구현

---

## 총평

Reservation BC의 도메인 코드는 컨벤션을 **매우 잘 준수**하고 있다.
- Aggregate 패턴(forNew/reconstitute), ID VO, VO Record, ErrorCode/Exception 계층 모두 올바르게 구현
- 시간 필드(Instant vs LocalDate) 사용이 적절
- items 불변 복사본(List.copyOf) 적용
- 상태 전이 로직이 구체 예외와 함께 명확하게 표현

BLOCKER/MAJOR 이슈 없음. 코드 품질 우수.
