# 시간 필드 컨벤션 — 2026-04-03

## 원칙
- 시점(Timestamp)과 비즈니스 날짜는 용도가 다르므로 타입을 구분한다.
- DB에는 UTC 기준으로 저장하고, 표시 시 timezone 변환한다 (시점 필드만).
- 비즈니스 날짜는 숙소 현지 기준이므로 timezone 변환하지 않는다.

## 타입 구분

| 용도 | Java 타입 | DB 타입 | timezone 변환 | 예시 |
|------|-----------|---------|:-------------:|------|
| 시점 (언제 일어났는가) | Instant | TIMESTAMP (UTC) | O | createdAt, cancelledAt, lastSyncedAt |
| 비즈니스 날짜 (현지 기준) | LocalDate | DATE | X | checkInDate, rateDate, inventoryDate |
| 비즈니스 시간 (현지 기준) | LocalTime | TIME | X | checkInTime, checkOutTime |

## 이유

### 시점 → Instant (UTC)
"이 예약이 언제 생성되었는가"는 전 세계 어디서 봐도 같은 시점이어야 한다.
서버/DB가 어느 timezone에 있든 동일한 값을 보장하기 위해 UTC로 저장.
고객에게 표시할 때 고객의 timezone으로 변환.

### 비즈니스 날짜 → LocalDate
"4/2 체크인"은 숙소가 있는 곳의 4/2이다.
이걸 Instant로 저장하면 timezone 변환 시 날짜가 밀리는 문제 발생.
(예: 방콕 4/2 00:00 = UTC 4/1 17:00 → KST 변환 시 4/2 02:00이지만 다른 TZ에서는 4/1로 보임)
LocalDate는 timezone 개념 없이 "그냥 4/2"이므로 이 문제가 없다.

## 해외 확장 시
- Property에 timezone 필드 추가 (예: "Asia/Seoul", "Asia/Bangkok")
- 비즈니스 날짜+시간을 조합할 때 Property의 timezone 참조
- 예: 취소 마감 = checkInDate + checkInTime + Property.timezone → Instant로 변환하여 비교
