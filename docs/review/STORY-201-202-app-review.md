# STORY-201/202 Application 리뷰 보고서

> 작성일: 2026-04-06
> 대상: 고객 숙소 검색(SearchPropertyUseCase) + 요금 조회(FetchRateUseCase)

## 리뷰 결과 요약

| 체크리스트 | 판정 | 비고 |
|-----------|:----:|------|
| APP-UC-001: UseCase 인터페이스 | PASS | 메서드 1개, 네이밍 준수 |
| APP-SVC-001: Service 트랜잭션 없음 | PASS | @Transactional 없음, Manager만 의존 |
| APP-MGR-001: Manager 메서드 단위 트랜잭션 | PASS | 모든 ReadManager readOnly=true 메서드 단위 |
| APP-BC-001: BC 간 경계 | PASS | ReadManager만 크로스 BC 호출 |
| APP-DTO-001: Record + Domain VO | PASS (FIX 후) | 인스턴스 메서드 제거 |
| APP-PRT-001: Port 분리 | PASS | CommandPort/QueryPort 분리, findAll 없음 |
| APP-PRT-002: Port 파라미터 | PASS (FIX 후) | 크로스 BC 검색 Port 예외 문서화 |
| APP-FAC-001: TimeProvider | PASS | 해당 없음 (조회 전용) |
| APP-EXC-001: 구체 예외 | PASS | PropertyNotFoundException 사용 |
| APP-OBX-001: Outbox 패턴 | PASS | Spring Event 미사용 |

## FIX 이력

### Round 1 (2건)
1. **APP-DTO-001 위반**: SearchPropertyQuery.toCriteria(), FetchRateQuery.toCriteria() 인스턴스 메서드
   - 수정: 인스턴스 메서드 제거, Service에서 직접 Criteria 생성
2. **APP-PRT-002 위반**: PropertySearchQueryPort가 Application DTO(PropertySummary) 반환
   - 수정: 크로스 BC 검색 Port임을 Javadoc에 명시 (CQRS Query 모델 예외)

## 설계 판단 기록

### PropertySearchQueryPort vs PropertyQueryPort.findByCondition
검색은 여러 BC(Property + RoomType + Inventory + Rate)를 조합하는 크로스 BC 쿼리이므로, 단일 BC용 PropertyQueryPort와 분리하여 PropertySearchQueryPort를 별도 선언했다. 이 Port는 Application DTO(PropertySummary)를 반환하며, CQRS의 Query 모델에 해당한다.
