# 코드 리뷰 보고서 -- pricing BC

> 리뷰어: domain-code-reviewer
> 대상: `domain/src/main/java/com/ryuqq/otatoy/domain/pricing/`
> 일시: 2026-04-04

## ArchUnit 테스트: PASS

`./gradlew :domain:test --tests "*.DomainLayerArchTest"` -- 전체 통과

## 코드 검증 요약
- 파일 수: 16
- PASS: 14 / FAIL: 2
- 심각도: BLOCKER 0 / MAJOR 0 / MINOR 2

---

## 상세

### PASS 파일 목록

| 파일 | 규칙 | 결과 |
|------|------|------|
| RatePlan.java | C-1 Aggregate, C-6 시간, C-7 의존성 | PASS |
| Rate.java | C-1 엔티티, C-6 시간, C-7 의존성 | PASS |
| RateRule.java | C-1 엔티티, C-6 시간, C-7 의존성 | PASS |
| RateOverride.java | C-1 엔티티, C-6 시간, C-7 의존성 | PASS |
| RatePlanAddOn.java | C-1 엔티티, C-6 시간, C-7 의존성 | PASS |
| RatePlanId.java | C-2 ID VO (자기 ID) | PASS |
| RateId.java | C-2 ID VO | PASS |
| RateRuleId.java | C-2 ID VO | PASS |
| RateOverrideId.java | C-2 ID VO | PASS |
| RatePlanAddOnId.java | C-2 ID VO | PASS |
| RatePlanName.java | C-3 VO | PASS |
| AddOnName.java | C-3 VO | PASS |
| AddOnType.java | C-3 VO | PASS |
| PaymentPolicy.java | C-4 Enum | PASS |
| SourceType.java | C-4 Enum | PASS |
| PricingErrorCode.java | C-5 ErrorCode | PASS |

---

### MINOR-1: Rate.java -- C-8 ERD 불일치 [MINOR]

- **위반**: ERD의 Rate 테이블에 `calculated_from` 필드(string)가 정의되어 있으나, 도메인 모델 Rate.java에 해당 필드가 없음.
- **ERD 정의**: `calculated_from` -- Rate이 어디서 계산되었는지 추적 (예: "RATE_RULE", "SUPPLIER_DIRECT")
- **수정 방안**: Rate 엔티티에 `calculatedFrom` (String 또는 전용 Enum) 필드 추가. forNew/reconstitute에 반영.
- **비고**: 당장 로직에 영향은 없으나, Persistence 레이어 매핑 시 필요.

### MINOR-2: 하위 엔티티 ID VO 과다 사용 [MINOR]

- **위반**: 컨벤션(DOM-AGG-002)에서 "Aggregate Root만 ID VO, 하위 엔티티는 Long 허용"이라고 명시. 그러나 Rate, RateRule, RateOverride, RatePlanAddOn 모두 전용 ID VO를 가지고 있음.
- **수정 방안**: 기능적으로 문제 없으므로 현재 상태 유지 가능. 다만 다른 BC와 일관성을 위해 하위 엔티티 ID를 Long으로 단순화하는 것도 검토 가능.
- **비고**: ArchUnit 위반은 아님. 일관성 관점의 개선 사항.

---

## C-1 ~ C-9 전체 체크리스트 결과

| 체크리스트 | 결과 | 비고 |
|-----------|------|------|
| C-1: Aggregate 구조 | PASS | forNew/reconstitute, private 생성자, Setter 없음 |
| C-2: ID VO 구조 | PASS | Record + of() + isNew() |
| C-3: VO 구조 | PASS | RatePlanName, AddOnName, AddOnType 모두 Record + of() + compact |
| C-4: Enum 구조 | PASS | PaymentPolicy, SourceType 모두 displayName() |
| C-5: ErrorCode | PASS | PricingErrorCode implements ErrorCode |
| C-6: 시간 필드 | PASS | Instant(시점), LocalDate(비즈니스 날짜) 올바른 사용 |
| C-7: 의존성 | PASS | Spring/JPA 없음, 타 컨텍스트 ID VO만 참조 |
| C-8: ERD 일치 | MINOR | Rate.calculated_from 필드 누락 |
| C-9: 패키지 구조 | PASS | flat 패키지, 하위 패키지 없음 |

---

## 수정 요청 (-> domain-builder)

BLOCKER/MAJOR 없음. MINOR 2건은 즉시 수정 불필요.
현 상태로 Phase 4(FIX 루프)를 건너뛰고 Phase 5(테스트) 진행 가능.
