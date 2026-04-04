# Reservation BC -- Spec Review 보고서

> 리뷰 일시: 2026-04-04
> 모드: review (기존 코드 검증)
> 리뷰어: domain-spec-reviewer
> 참조: ERD, OTA 리서치(domain-validation.md), 도메인 피드백(FB-15~17)

---

## 비즈니스 규칙 검증

| # | 비즈니스 규칙 | 판정 | 근거 |
|---|-------------|:----:|------|
| S1 | PENDING -> CONFIRMED 상태 전이 | PASS | confirm() 구현 |
| S2 | CONFIRMED -> COMPLETED 상태 전이 | PASS | complete() 구현 |
| S3 | CONFIRMED -> NO_SHOW 상태 전이 | PASS | noShow() 구현 |
| S4 | PENDING/CONFIRMED -> CANCELLED 전이 | PASS | cancel(reason, now) 구현 |
| S5 | 이미 취소된 예약 재취소 방지 (RSV-003) | PASS | ReservationAlreadyCancelledException |
| S6 | 이미 완료된 예약 취소 방지 (RSV-004) | PASS | ReservationAlreadyCompletedException |
| S7 | 과거 날짜 예약 금지 | PASS | stayPeriod.startDate < today 검증 |
| S8 | 당일 예약 허용 | PASS | isBefore만 체크 (== today 허용) |
| S9 | 최대 30박 제한 | PASS | MAX_STAY_NIGHTS = 30 |
| S10 | 투숙 인원 1명 이상 | PASS | guestCount <= 0 검증 |
| S11 | 예약 항목 1개 이상 필수 | PASS | items null/empty 검증 |
| S12 | ReservationItem = 재고 차감 추적 용도 (FB-17) | PASS | stayDate + inventoryId만 보유 |
| S13 | bookingSnapshot JSON 스냅샷 (FB-16) | PASS | String bookingSnapshot 필드 |
| S14 | 변경 = 전체 취소 후 재예약 (FB-15) | PASS | 부분 수정 메서드 없음 |
| S15 | ReservationNo 예약 번호 필수 | PASS | compact constructor blank/null 검증 |

---

## 상태 전이 매트릭스

| 현재 상태 \ 행위 | confirm() | cancel() | complete() | noShow() |
|:---|:---:|:---:|:---:|:---:|
| PENDING | -> CONFIRMED | -> CANCELLED | InvalidState | InvalidState |
| CONFIRMED | InvalidState | -> CANCELLED | -> COMPLETED | -> NO_SHOW |
| CANCELLED | InvalidState | AlreadyCancelled | InvalidState | InvalidState |
| COMPLETED | InvalidState | AlreadyCompleted | InvalidState | InvalidState |
| NO_SHOW | InvalidState | InvalidState | InvalidState | InvalidState |

모든 상태 전이가 적절하게 제어되고 있다.

---

## ERD 정합성

| ERD 컬럼 | 도메인 모델 | 정합 |
|----------|-----------|:----:|
| Reservation.id | ReservationId (ID VO) | O |
| Reservation.rate_plan_id | RatePlanId (cross-BC 참조) | O |
| Reservation.reservation_no | ReservationNo (VO record) | O |
| Reservation.guest_name/phone/email | GuestInfo (VO record) | O |
| Reservation.check_in_date/check_out_date | DateRange stayPeriod | O |
| Reservation.guest_count | int guestCount | O |
| Reservation.total_amount | Money totalAmount | O |
| Reservation.status | ReservationStatus enum | O |
| Reservation.cancel_reason | String cancelReason | O |
| Reservation.booking_snapshot | String bookingSnapshot | O |
| Reservation.created_at | Instant createdAt | O |
| Reservation.cancelled_at | Instant cancelledAt | O |
| ReservationItem.id | ReservationItemId (ID VO) | O |
| ReservationItem.reservation_id | ReservationId | O |
| ReservationItem.inventory_id | InventoryId (cross-BC 참조) | O |
| ReservationItem.stay_date | LocalDate stayDate | O |

**ERD 100% 정합**

---

## 총평

Reservation BC는 OTA 리서치와 도메인 피드백(FB-15~17)을 충실하게 반영했다.
- 예약 변경은 전체 취소 후 재예약 패턴 (FB-15)
- bookingSnapshot JSON 스냅샷 패턴 (FB-16)
- ReservationItem의 역할 축소 -- 재고 차감 추적 전용 (FB-17)
- 상태 전이 매트릭스가 완전하고 구체 예외가 적절

FAIL 항목 없음. 비즈니스 규칙 완전성 확인 완료.
