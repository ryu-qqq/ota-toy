# Admin API 모듈

> 운영팀을 위한 관리 기능 API. 현재 설계 단계.

## 필요 기능

### 숙소 관리
- `GET /api/v1/admin/properties` — 전체 숙소 목록 조회 (상태 필터, 페이징)
- `PATCH /api/v1/admin/properties/{id}/status` — 숙소 상태 변경 (승인/비활성화/정지)
- `GET /api/v1/admin/properties/{id}` — 숙소 상세 조회 (파트너 정보 포함)

### 예약 모니터링
- `GET /api/v1/admin/reservations` — 전체 예약 목록 조회 (상태별, 날짜별 필터)
- `GET /api/v1/admin/reservations/{id}` — 예약 상세 조회 (세션/결제 이력 포함)
- `GET /api/v1/admin/reservations/stats` — 예약 통계 (일별 건수, 취소율)

### 파트너 관리
- `GET /api/v1/admin/partners` — 파트너 목록 조회
- `PATCH /api/v1/admin/partners/{id}/status` — 파트너 상태 변경 (승인/정지)

### 재고/요금 모니터링
- `GET /api/v1/admin/inventory/alerts` — 재고 부족 알림 (Redis-DB 불일치 포함)
- `GET /api/v1/admin/rates/cache-stats` — 요금 캐시 히트율 모니터링

### Supplier 관리
- `GET /api/v1/admin/suppliers` — 공급자 목록 및 동기화 상태
- `POST /api/v1/admin/suppliers/{id}/sync` — 수동 동기화 트리거
- `GET /api/v1/admin/suppliers/{id}/sync-logs` — 동기화 이력 조회

## 설계 원칙
- Admin API는 조회 위주. 상태 변경은 PATCH로 제한적으로만 제공
- 모든 변경 작업은 감사 로그(Outbox)를 남김
- 운영자 인증/인가는 별도 미들웨어로 처리 (JWT + 역할 기반)
