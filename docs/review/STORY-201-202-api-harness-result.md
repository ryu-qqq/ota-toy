# REST API 하네스 결과 -- STORY-201/202 Customer API

## 대상
- STORY-201: 고객 숙소 검색 (`GET /api/v1/search/properties`)
- STORY-202: 고객 요금 조회 (`GET /api/v1/properties/{id}/rates`)

## 파이프라인 결과

| Phase | 항목 | 결과 |
|-------|------|------|
| Phase 0 | 전제조건 확인 | PASS -- API 컨벤션, UseCase, rest-api-core, rest-api-customer 모듈 모두 존재 |
| Phase 1 | 코드 생성 | PASS -- Controller 2개, Request 2개, Response 3개, ApiMapper 2개, 컴파일 성공 |
| Phase 2 | 컨벤션 셀프 체크 | PASS -- 8/8 규칙 통�� |
| Phase 3 | MockMvc 테스트 | PASS -- 8/8 테스트 통과 |

## 생성된 파일 (9개 + 테스트 3개)

### 메인 코드
| 파일 | 용도 |
|------|------|
| `search/SearchPropertyController.java` | 숙소 검색 Controller |
| `search/dto/SearchPropertyApiRequest.java` | 검색 요청 DTO (record, @NotNull, @Min) |
| `search/dto/PropertySummaryApiResponse.java` | 검색 결과 응답 DTO (record) |
| `search/mapper/SearchPropertyApiMapper.java` | Request->Query, Result->Response 변환 |
| `rate/RateController.java` | 요금 조회 Controller |
| `rate/dto/FetchRateApiRequest.java` | 요금 조회 요청 DTO (record, @NotNull, @Min) |
| `rate/dto/RoomRateApiResponse.java` | 객실별 요금 응답 DTO (record) |
| `rate/dto/DailyRateApiResponse.java` | 날짜별 요금 응��� DTO (record) |
| `rate/mapper/RateApiMapper.java` | Request->Query, Result->Response 변환 |

### 테스트 코드
| 파일 | 테스트 수 |
|------|----------|
| `search/SearchPropertyControllerTest.java` | 4개 (성공, 빈 결과, 체크인 누락, 체크아웃 누락) |
| `rate/RateControllerTest.java` | 4개 (성공, 빈 목록, 체크인 누락, 체크아웃 누락) |
| `TestApplication.java` | 테스트용 Spring Boot 설정 |

## 컨벤션 검증 상세

| 규칙 | 검증 | 결과 |
|------|------|------|
| API-CTR-001: @Transactional 금지 | grep 확인 | PASS |
| API-CTR-001: 비즈���스 로직 금지 | UseCase.execute() 호출 + 응답 래핑만 | PASS |
| API-CTR-001: UseCase 인터페이스만 의존 | Service import 없��� | PASS |
| API-CTR-001: @DeleteMapping 금지 | grep 확인 | PASS |
| API-DTO-001: Request/Response는 record | 5개 모두 record | PASS |
| API-DTO-002: Jakarta Validation | @NotNull, @Min 사용 | PASS |
| API-DTO-001: ApiMapper 사용 | Controller에서 Mapper 호출 확인 | PASS |
| API-DOC-001: Swagger 어노테이션 | @Tag, @Operation 존재 | PASS |

## FIX 루프 이력

| 루프 | 원인 | 조치 |
|------|------|------|
| 테스트 FIX 1 | Spring Boot 3.5에서 @MockBean 패키지 변경 | @MockitoBean으로 교체 |
| 테��트 FIX 2 | 라이브러리 모듈에 @SpringBootConfiguration 없음 | TestApplication.java 추가 |

## 일자
2026-04-06
