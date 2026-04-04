# 비즈니스 스펙 리뷰 보고서 -- pricing BC

> 리뷰어: domain-spec-reviewer
> 대상: `domain/src/main/java/com/ryuqq/otatoy/domain/pricing/`
> 일시: 2026-04-04

## 요약
- 비즈니스 규칙 커버리지: 14/17 (검증한 규칙 / 필요한 규칙)
- 누락된 규칙: 0개
- 부분적 표현: 3개 (WARNING)
- 위험 사항: 0개 (BLOCKER 없음)

---

## 규칙 검증

### PASS -- 도메인에 표현됨 (14건)

| # | 규칙 | 위치 | 규칙 소스 |
|---|------|------|-----------|
| 1 | 무료 취소 + 환불 불가 동시 설정 방지 | RatePlan.forNew():59, updatePolicy():84 | docs/seeds/2026-04-01-domain-feedback.md (FB-9) |
| 2 | SUPPLIER 소스의 supplierId 필수 | RatePlan.forNew():56 | FB-26 |
| 3 | 요일별 가격 차등 적용 | RateRule.calculatePrice():68 | FB-10 |
| 4 | 요일별 가격 미설정 시 basePrice 폴백 | RateRule.calculatePrice():76-80 | FB-10 |
| 5 | RateOverride 특정일 가격 덮어쓰기 | RateRule.resolvePrice():88 | 2026-04-02-rate-caching-design.md |
| 6 | 날짜 범위 밖 가격 계산 방지 | RateRule.calculatePrice():69, covers():104 | 추론됨 |
| 7 | 가격 음수 방지 (Rate) | Rate.forNew():33, updatePrice():44 | 추론됨 |
| 8 | 가격 음수 방지 (RateRule) | RateRule.forNew():52 | 추론됨 |
| 9 | 가격 음수 방지 (RateOverride) | RateOverride.forNew():33 | 추론됨 |
| 10 | 가격 음수 방지 (AddOn) | RatePlanAddOn.forNew():31 | 추론됨 |
| 11 | 무료 취소 기한 음수 방지 | RatePlan.forNew():63 | 사용자 추가 |
| 12 | RateRule 종료일 >= 시작일 검증 | RateRule.forNew():48 | 추론됨 |
| 13 | DIRECT/SUPPLIER 구분 | SourceType enum, RatePlan.isDirect()/isSupplier() | FB-26, FB-29 |
| 14 | Add-on 분리 (조식 등) | RatePlanAddOn 엔티티 | FB-9 |

### WARNING -- 부분적으로 표현됨 (3건)

#### W-1: Rate.calculatedFrom 필드 누락 [MINOR]
- **현재**: Rate 엔티티에 `calculatedFrom` 필드 없음
- **비즈니스 요구**: Rate이 RateRule 기반 계산인지, Supplier 직접 입력인지 추적이 필요함 (FB-29: "DIRECT는 RateRule->Rate 계산, SUPPLIER는 Rate 직접 저장")
- **규칙 소스**: ERD (`calculated_from` 컬럼), docs/seeds/2026-04-01-domain-feedback.md (FB-29)
- **제안**: CalculatedFrom enum (RATE_RULE, SUPPLIER_DIRECT) 또는 String 필드 추가

#### W-2: RateOverride가 소속 RateRule 날짜 범위 내인지 검증 없음 [MINOR]
- **현재**: RateOverride.forNew()는 `overrideDate`가 null인지만 검증. 소속 RateRule의 startDate~endDate 범위 안에 있는지는 검증하지 않음
- **비즈니스 요구**: RateRule 범위(4/1~4/30) 밖의 날짜(5/1)에 대한 Override는 의미 없으며 데이터 오류
- **규칙 소스**: 추론됨
- **제안**: 현재 RateOverride는 RateRuleId만 알고 RateRule 자체는 모르므로, 이 검증은 Application 레이어(UseCase)에서 수행하는 것이 적절. 도메인에서 강제하려면 RateRule.addOverride(date, price, reason) 메서드를 만들어 Aggregate 내에서 검증.

#### W-3: RatePlan 활성/비활성 상태 없음 [MINOR]
- **현재**: RatePlan에 status 필드가 없음. 생성 후 항상 활성 상태.
- **비즈니스 요구**: 요금 정책을 일시 중지(판매 중지)하는 비즈니스 케이스가 존재 (예: 숙소 리노베이션, 시즌 오프)
- **규칙 소스**: 추론됨 (OTA 업계 공통 패턴)
- **제안**: 현재 프로젝트 스코프 내에서 필수는 아님. 필요 시 RatePlanStatus enum + activate()/suspend() 메서드 추가.

---

## 비즈니스 흐름 검증

| 흐름 | 가능 여부 | 비고 |
|------|:---------:|------|
| 파트너 요금 정책 생성 | PASS | RatePlan.forNew() |
| 요금 규칙 설정 (기간+요일별) | PASS | RateRule.forNew() |
| 특정일 가격 오버라이드 | PASS | RateOverride.forNew() |
| 날짜별 가격 계산 (규칙 기반) | PASS | RateRule.calculatePrice() |
| 오버라이드 반영 최종 가격 | PASS | RateRule.resolvePrice() |
| Add-on 추가 (조식 포함 등) | PASS | RatePlanAddOn.forNew() |
| 정책 변경 (취소/환불/결제) | PASS | RatePlan.updatePolicy() |
| DIRECT/SUPPLIER 가격 구분 | PASS | SourceType |
| Supplier 가격 직접 입력 | PASS | Rate.forNew() (RateRule 거치지 않고 직접) |

---

## 누락된 도메인 개념

| 개념 | 근거 | 중요도 | 제안 |
|------|------|--------|------|
| Rate.calculatedFrom | ERD 필드, FB-29 | MINOR | 필드 추가 |
| RatePlan 상태 관리 | OTA 공통 패턴 | MINOR | 스코프 외 가능 |
| RateRule 기간 중복 검증 | 추론됨 | MINOR | Application 레이어에서 처리 |

---

## 테스트 시나리오 요청 (-> domain-test-designer)

| 규칙 코드 | 상태 | 테스트 시나리오 힌트 |
|-----------|:----:|---------------------|
| S-1 #1 | PASS | "무료 취소 + 환불 불가 동시 설정 시 forNew/updatePolicy 모두 예외" |
| S-1 #2 | PASS | "SUPPLIER인데 supplierId null이면 예외" |
| S-1 #3 | PASS | "요일별 가격 계산 -- 월~목=weekday, 금=friday, 토=saturday, 일=sunday" |
| S-1 #4 | PASS | "요일별 가격 null이면 basePrice 폴백" |
| S-1 #5 | PASS | "resolvePrice -- Override 존재 시 오버라이드 가격, 미존재 시 기본가" |
| S-1 #6 | PASS | "범위 밖 날짜 계산 시 예외" |
| S-1 #7-10 | PASS | "각 엔티티 음수 가격 생성 시 예외" |
| S-1 #11 | PASS | "freeCancellationDeadlineDays 음수 시 예외" |
| S-1 #12 | PASS | "RateRule endDate < startDate 시 예외" |
| W-1 | WARNING | "Rate 생성 시 calculatedFrom 값이 올바르게 설정되는지" (도메인 수정 후) |
| W-2 | WARNING | "resolvePrice에서 covers() false인 날짜 시 예외 확인" |
| T-5 | -- | "RatePlanName, AddOnName, AddOnType VO의 null/blank 검증" |
| T-6 | -- | "equals/hashCode ID 기반 동등성 (동일 ID = 동등, 다른 ID = 비동등, null ID = 비동등)" |

---

## 수정 요청 (-> domain-builder)

BLOCKER/MAJOR 없음. WARNING 3건은 모두 MINOR 수준.
FIX 루프 불필요. Phase 5(테스트) 진행 가능.
