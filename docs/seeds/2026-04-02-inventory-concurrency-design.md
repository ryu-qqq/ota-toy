# 재고 동시성 제어 설계 — 2026-04-02

## 문제
동일한 재고에 대해 동시 예약 요청이 발생할 수 있는 상황을 처리해야 함.

## 선택: Redis 원자적 카운터 + 임시 홀드

### 검토한 대안

| 방식 | 장점 | 단점 | 판단 |
|------|------|------|------|
| DB 비관적 락 (SELECT FOR UPDATE) | 구현 단순, 정합성 확실 | 행이 없으면 락 불가, DB 락 경합이 병목, 대규모 트래픽에 부적합 | ✗ |
| DB 낙관적 락 (version) | 락 경합 없음 | 충돌 시 재시도 필요, 동시성 높으면 재시도 폭발 | ✗ |
| Redis 원자적 카운터 | 락 불필요, 초당 10만+, 행 존재 여부 무관 | Redis-DB 정합성 관리 필요 | ✅ |

### 구조

```
Redis (실시간 재고)
├── inventory:{roomTypeId}:{date} = 10   ← 원자적 DECR/INCR
├── hold:{reservationId} EX 600          ← 임시 홀드 (10분 TTL)
    ↕ 동기화
DB Inventory (원본 기록)
├── 확정된 예약만 반영
├── 주기적 정합성 체크 (배치)
```

### 예약 흐름

```
1) 고객이 "스탠다드 더블, 4/2~4/4 2박" 예약 요청

2) Redis 재고 차감 (원자적)
   DECR inventory:123:2026-04-02 → 9 (0 이상 → OK)
   DECR inventory:123:2026-04-03 → 4 (0 이상 → OK)
   → 하나라도 0 미만이면 전부 INCR 복구 → 실패 응답

3) 임시 홀드 생성
   SET hold:{reservationId} "{roomTypeId, dates}" EX 600
   → 10분 안에 결제 완료해야 함

4) 결제 완료
   → DB에 Reservation + ReservationItem + Inventory 기록
   → DEL hold:{reservationId}
   → 상태: CONFIRMED

5) 결제 실패 / 10분 타임아웃
   → Redis INCR로 재고 복구
   → hold 키 TTL 만료 시 자동 복구 처리 (스케줄러 또는 키 만료 이벤트)
```

### 초기화
```
파트너가 RateRule 설정 (4/1~4/30)
→ Rate 스냅샷 30일치 생성
→ DB Inventory 30일치 생성 (available_count = base_inventory)
→ Redis 카운터 30일치 세팅 (SET inventory:{id}:{date} {base_inventory})
```

### Redis-DB 정합성

```
정상 흐름: Redis → 결제 확정 → DB 반영 (일치)
비정상: Redis 차감됐는데 서버 크래시 → DB 미반영 → 불일치

대응:
1) 홀드 TTL 만료 시 자동 복구 (Redis INCR)
2) 주기적 배치: DB Inventory와 Redis 카운터 비교 → 불일치 시 Redis 보정
3) Redis 장애 시: DB 비관적 락으로 폴백 (degradation)
```

## 의사결정

### 왜 Redis 카운터인가
- DB 비관적 락은 "행이 없으면 락 불가" 문제가 있음
- 미리 행을 만들어두면 해결은 되지만 DB 락 경합이 대규모 트래픽의 병목
- Redis DECR은 싱글 스레드 원자 연산이라 락 자체가 필요 없음
- 실제 대규모 OTA들이 이 패턴을 사용

### 왜 임시 홀드인가
- "재고 차감 → 결제" 사이에 시간 갭이 있음
- 결제 실패 시 재고 자동 복구 필요
- TTL 기반이라 구현 단순하고 누수 방지
