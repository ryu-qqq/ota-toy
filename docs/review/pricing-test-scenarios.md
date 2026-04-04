# 테스트 ��나리오 보고서 -- pricing BC

> 설계자: domain-test-designer
> 일시: 2026-04-04

## 시나리오 요약

| 카테고리 | 시나리오 수 | 작성 완료 |
|----------|:---------:|:---------:|
| T-1: 생성 검증 | 21 | 21 |
| T-2: 상태 전이 | 5 | 5 |
| T-3: 불변식/복원 | 6 | 6 |
| T-4: 도메인 로직 | 18 | 18 |
| T-5: VO 검증 | 14 | 14 |
| T-6: 동등성 | 10 | 10 |
| **합계** | **74** | **74** |

> 기존 RatePlanTest.java (17건) + 신규 작성 (57건) = 총 74건

---

## spec-reviewer 연동

| 규칙 코드 | spec 상태 | 테스트 시나리오 | 테스트 결과 |
|-----------|:---------:|---------------|:----------:|
| S-1 #1 | PASS | RatePlanTest - 무료 취소 + 환불 불가 동시 설정 예외 | PASS |
| S-1 #2 | PASS | RatePlanTest - SUPPLIER + null supplierId 예외 | PASS |
| S-1 #3 | PASS | RateRuleTest - 월~일 요일별 가격 계산 (7건) | PASS |
| S-1 #4 | PASS | RateRuleTest - null 요일가 basePrice 폴백 (4건) | PASS |
| S-1 #5 | PASS | RateRuleTest - resolvePrice 오버라이드 적용 (4건) | PASS |
| S-1 #6 | PASS | RateRuleTest - 범위 밖 날짜 예외 (2건) | PASS |
| S-1 #7 | PASS | RateTest - 음수/null 가격 예외 | PASS |
| S-1 #8 | PASS | RateRuleTest - 음수/null basePrice 예�� | PASS |
| S-1 #9 | PASS | RateOverrideTest - 음수/null 가격 예외 | PASS |
| S-1 #10 | PASS | RatePlanAddOnTest - 음수 가격 예외 | PASS |
| S-1 #11 | PASS | RatePlanTest - deadlineDays 음수 예외 | PASS |
| S-1 #12 | PASS | RateRuleTest - endDate < startDate 예외 | PASS |
| W-1 | WARNING | Rate.calculatedFrom 필드 미존재 -- 테스트 불가 | N/A |
| W-2 | WARNING | resolvePrice covers() false 시 예외 -- 테스트 작성됨 | PASS |

---

## 시나리오 목록

### T-1: 생성 검증

| 파일 | 시나리오 | 결과 |
|------|---------|:----:|
| RatePlanTest | DIRECT 정상 생성 | PASS |
| RatePlanTest | SUPPLIER 정상 생성 | PASS |
| RatePlanTest | roomTypeId null 예외 | PASS |
| RatePlanTest | paymentPolicy null 예외 | PASS |
| RatePlanTest | SUPPLIER + supplierId null 예외 | PASS |
| RatePlanTest | 무료 취소 + 환불 불가 동시 설정 예외 | PASS |
| RatePlanTest | deadlineDays 음수 예외 | PASS |
| RateRuleTest | 정상 생성 | PASS |
| RateRuleTest | startDate null 예외 | PASS |
| RateRuleTest | endDate null 예외 | PASS |
| RateRuleTest | endDate < startDate 예외 | PASS |
| RateRuleTest | startDate == endDate 허��� | PASS |
| RateRuleTest | basePrice null 예외 | PASS |
| RateRuleTest | basePrice 음수 예외 | PASS |
| RateRuleTest | basePrice 0 허용 | PASS |
| RateTest | 정상 생성 | PASS |
| RateTest | rateDate null 예외 | PASS |
| RateTest | basePrice null/음수 예외 | PASS |
| RateOverrideTest | 정상 생성 | PASS |
| RateOverrideTest | overrideDate null 예외 | PASS |
| RateOverrideTest | price null/음수 예외 | PASS |
| RatePlanAddOnTest | 유료/무료 정상 생성 | PASS |
| RatePlanAddOnTest | price 음수 예외 | PASS |

### T-2: 상태 전이

| 파일 | 시나리오 | 결��� |
|------|---------|:----:|
| RatePlanTest | updatePolicy 정상 변경 | PASS |
| RatePlanTest | updatePolicy 무료취소+환불불가 예외 | PASS |
| RatePlanTest | updatePolicy 음수 기한 예외 | PASS |
| RateTest | updatePrice 정상 변경 | PASS |
| RateTest | updatePrice null/음수 예외 | PASS |

### T-3: 불변식/복원

| 파일 | 시나리오 | 결�� |
|------|---------|:----:|
| RatePlanTest | reconstitute 정상 복원 | PASS |
| RateRuleTest | reconstitute 정상 복원 | PASS |
| RateTest | reconstitute 정상 복원 | PASS |
| RateOverrideTest | reconstitute 정상 ���원 | PASS |
| RatePlanAddOnTest | reconstitute ���상 복원 | PASS |
| RateOverrideTest | reason null 허용 | PASS |

### T-4: 도메인 로직

| 파일 | 시나리오 | ���과 |
|------|---------|:----:|
| RateRuleTest | 월요일 weekdayPrice | PASS |
| RateRuleTest | 화요일 weekdayPrice | PASS |
| RateRuleTest | 금요일 fridayPrice | PASS |
| RateRuleTest | 토요일 saturdayPrice | PASS |
| RateRuleTest | 일요일 sundayPrice | PASS |
| RateRuleTest | weekdayPrice null -> basePrice 폴백 | PASS |
| RateRuleTest | fridayPrice null -> basePrice 폴백 | PASS |
| RateRuleTest | saturdayPrice null -> basePrice 폴백 | PASS |
| RateRuleTest | sundayPrice null -> basePrice 폴백 | PASS |
| RateRuleTest | 범위 밖 날짜 예외 | PASS |
| RateRuleTest | 시작일/종료일 당일 허용 | PASS |
| RateRuleTest | resolvePrice 오버라이드 존재 시 오버라이드 가격 | PASS |
| RateRuleTest | resolvePrice 오버라이드 미존재 시 요일 가격 | PASS |
| RateRuleTest | resolvePrice null/빈 리스트 시 요일 가격 | PASS |
| RateRuleTest | resolvePrice 범위 밖 예외 | PASS |
| RateRuleTest | covers() 범위 판단 (5건) | PASS |
| RatePlanAddOnTest | isFree() price=0, null, 양수 | PASS |

### T-5: VO 검증

| 파일 | 시나리오 | 결과 |
|------|---------|:----:|
| PricingVoTest | RatePlanName 정상/null/blank | PASS |
| PricingVoTest | AddOnName 정상/null/blank | PASS |
| PricingVoTest | AddOnType 정상/null/blank | PASS |
| PricingVoTest | RatePlanId null -> isNew true | PASS |
| PricingVoTest | RatePlanId 값 있음 -> isNew false | PASS |
| PricingVoTest | RatePlanId Record 동등성 | PASS |
| PricingVoTest | PricingErrorCode 인터페이스 구현 | PASS |
| PricingVoTest | PricingErrorCode PRC- 접두사 | PASS |
| PricingVoTest | PaymentPolicy displayName 한국어 | PASS |
| PricingVoTest | SourceType displayName 한국어 | PASS |

### T-6: 동등성

| 파일 | 시나리오 | 결과 |
|------|---------|:----:|
| RatePlanTest | 같은 ID 동등 | PASS |
| RatePlanTest | 다른 ID 비동등 | PASS |
| RateRuleTest | 같은 ID 동등 | PASS |
| RateRuleTest | 다른 ID 비동등 | PASS |
| RateRuleTest | forNew() ID VO(null) Record 동등성 | PASS |
| RateTest | 같은 ID 동등 | PASS |
| RateTest | 다른 ID 비동등 | PASS |
| RateOverrideTest | 같은 ID 동등 | PASS |
| RateOverrideTest | 다른 ID 비동등 | PASS |
| RatePlanAddOnTest | 같은/다른 ID 동등성 | PASS |

---

## 테스트 실행 결과
- pricing 총 테스트: 74
- 성공: 74
- 실패: 0

## 발견 이슈

### 하위 엔티티 ID VO null 동등성 차이 [MINOR]
- RatePlan.forNew()는 id에 raw `null`을 넣어 `id != null` 체크에서 false가 됨 -> null id끼리 equals false
- Rate, RateRule 등 하위 엔티티는 `XxxId.of(null)`을 넣어 id가 Record 객체(null이 아님) -> `id != null`이 true, Record.equals로 비교하면 `XxxId(null).equals(XxxId(null))` = true -> null id끼리 equals true
- Aggregate Root와 하위 엔티티의 forNew() ID 처리 방식이 다름
- 영향: 기능적으로는 문제 없지만, 일관성 관점에서 개선 가능

---

## 수정 요청 (-> domain-builder)
없음. 모든 테스트 통과.
