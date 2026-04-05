# STORY-201/202 Application 테스트 시나리오

> 작성일: 2026-04-06
> 대상: SearchPropertyService, FetchRateService

## SearchPropertyServiceTest

| 카테고리 | 시나리오 | 결과 |
|---------|---------|:----:|
| 정상 흐름 | 검색 쿼리를 Criteria로 변환하여 ReadManager에 위임하고 결과 반환 | PASS |
| 정상 흐름 | Query DTO 필드가 Criteria에 올바르게 매핑 | PASS |
| 빈 결과 | 검색 결과 없으면 빈 SliceResult 반환 | PASS |
| 페이지네이션 | 다음 페이지 있으면 hasNext=true + nextCursor 반환 | PASS |

## FetchRateServiceTest

| 카테고리 | 시나리오 | 결과 |
|---------|---------|:----:|
| 정상 흐름 | 객실별 날짜별 요금+재고 조회하여 RoomRateSummary 반환 | PASS |
| 숙소 미존재 | PropertyNotFoundException 전파 | PASS |
| 숙소 미존재 | 객실/요금 조회 미호출 검증 | PASS |
| 객실 필터링 | maxOccupancy < guests 객실 제외 | PASS |
| 객실 필터링 | 비활성 객실 제외 | PASS |
| 객실 필터링 | 적합한 객실 없으면 빈 결과 반환 | PASS |
| 재고 가용성 | 특정 날짜 재고 없으면 해당 RatePlan 결과 제외 | PASS |
| 호출 순서 | verifyExists -> findByPropertyId -> findByRoomTypeIds -> findRates -> findInventory 순서 | PASS |
| RatePlan 없음 | 객실에 RatePlan 없으면 빈 결과 반환 | PASS |

## 테스트 통계
- 전체: 13개
- 성공: 13개
- 실패: 0개
