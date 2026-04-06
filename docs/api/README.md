# API 문서

## 문서 자동화 전략

이 프로젝트는 **두 가지 API 문서화** 도구를 사용합니다.

### 1. SpringDoc / Swagger UI (런타임)
서버 실행 시 자동으로 OpenAPI 명세를 생성하고 Swagger UI를 제공합니다.

| 모듈 | 포트 | Swagger UI | OpenAPI JSON |
|------|------|-----------|--------------|
| Extranet (파트너) | 8080 | `http://localhost:8080/swagger-ui.html` | `http://localhost:8080/api-docs` |
| Customer (고객) | 8081 | `http://localhost:8081/swagger-ui.html` | `http://localhost:8081/api-docs` |
| Admin (관리자) | 8082 | `http://localhost:8082/swagger-ui.html` | `http://localhost:8082/api-docs` |

**실행 방법:**
```bash
# Docker Compose로 MySQL + Redis 실행
docker compose up -d

# Extranet 서버 실행
./gradlew :bootstrap:bootstrap-extranet:bootRun

# Customer 서버 실행
./gradlew :bootstrap:bootstrap-customer:bootRun
```

### 2. Spring REST Docs (테스트 기반)
MockMvc 테스트에서 실제 요청/응답을 캡처하여 문서를 생성합니다.
**테스트가 통과해야 문서가 생성**되므로, 문서와 코드의 정합성이 보장됩니다.

**빌드 방법:**
```bash
./gradlew :adapter-in:rest-api-extranet:asciidoctor
./gradlew :adapter-in:rest-api-customer:asciidoctor
```

### 왜 두 가지를 모두 사용하는가

| 관점 | Swagger | REST Docs |
|------|---------|-----------|
| 생성 시점 | 런타임 (리플렉션) | 테스트 시 (실제 요청) |
| 정합성 보장 | 어노테이션 기반 (누락 가능) | 테스트 실패 시 문서 미생성 |
| 에러 응답 | 어노테이션으로 선언 | 실제 에러 발생 → 캡처 |
| 사용 편의성 | UI에서 바로 테스트 | 정적 HTML |
| 주 용도 | 개발 중 빠른 확인 | 배포용 공식 문서 |

Swagger는 개발 중 빠르게 API를 테스트하는 용도, REST Docs는 실제 동작이 검증된 공식 문서 용도로 구분합니다.

---

## 정적 문서 (REST Docs)

테스트 기반으로 생성된 API 명세서입니다.

- [Extranet API 문서](extranet-api.html) — 파트너용 9개 엔드포인트
- [Customer API 문서](customer-api.html) — 고객용 5개 엔드포인트

각 문서에 포함된 내용:
- 공통 응답 구조 (ApiResponse, RFC 7807 ProblemDetail)
- Enum 타입 레퍼런스 (PhotoType, AmenityType, PaymentPolicy 등)
- Validation 규칙 (필수/선택 필드, 제약조건)
- 엔드포인트별 정상 요청/응답 + 에러 케이스

---

## 엔드포인트 요약

### Extranet API (파트너용) — `localhost:8080`

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/v1/extranet/properties` | 숙소 목록 조회 |
| GET | `/api/v1/extranet/properties/{id}` | 숙소 상세 조회 |
| POST | `/api/v1/extranet/properties` | 숙소 기본정보 등록 |
| PUT | `/api/v1/extranet/properties/{id}/photos` | 숙소 사진 설정 |
| PUT | `/api/v1/extranet/properties/{id}/amenities` | 숙소 편의시설 설정 |
| PUT | `/api/v1/extranet/properties/{id}/attributes` | 숙소 속성값 설정 |
| POST | `/api/v1/extranet/properties/{id}/rooms` | 객실 유형 등록 |
| POST | `/api/v1/extranet/.../rooms/{id}/rate-plans` | 요금 정책 등록 |
| PUT | `/api/v1/extranet/rate-plans/{id}/rates` | 요금/재고 설정 |

### Customer API (고객용) — `localhost:8081`

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/v1/search/properties` | 숙소 검색 |
| GET | `/api/v1/properties/{id}/rates` | 요금 조회 |
| POST | `/api/v1/reservation-sessions` | 예약 세션 생성 (재고 선점) |
| POST | `/api/v1/reservations` | 예약 확정 |
| PATCH | `/api/v1/reservations/{id}/cancel` | 예약 취소 |

---

## 공통 규격

### 성공 응답
```json
{
  "data": { ... },
  "timestamp": "2026-04-06 15:30:00",
  "requestId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 에러 응답 (RFC 7807)
```json
{
  "type": "about:blank",
  "title": "Accommodation Error",
  "status": 404,
  "detail": "숙소를 찾을 수 없습니다",
  "instance": "/api/v1/extranet/properties/999",
  "code": "ACC-001",
  "timestamp": "2026-04-06T06:30:00.000Z",
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```
