# 로컬 개발 환경

Docker Compose로 MySQL, Redis, 애플리케이션 서버를 한 번에 실행합니다.

## 사용법

```bash
# 프로젝트 루트에서 실행

# 전체 실행 (MySQL + Redis + Extranet + Customer + Scheduler)
docker compose -f infra/local-dev/docker-compose.yml up -d

# 인프라만 실행 (MySQL + Redis) — IDE에서 직접 부트스트랩 실행 시
docker compose -f infra/local-dev/docker-compose.yml up -d mysql redis

# 특정 서비스만 실행
docker compose -f infra/local-dev/docker-compose.yml up -d extranet

# 로그 확인
docker compose -f infra/local-dev/docker-compose.yml logs -f extranet

# 전체 종료
docker compose -f infra/local-dev/docker-compose.yml down

# 볼륨 포함 종료 (데이터 초기화)
docker compose -f infra/local-dev/docker-compose.yml down -v
```

## 서비스 구성

| 서비스 | 포트 | 설명 |
|--------|------|------|
| mysql | 3306 | MySQL 8.4 (DB: ota, root/root) |
| redis | 6379 | Redis 7 |
| extranet | 8080 | 파트너용 API (Swagger: http://localhost:8080/swagger-ui.html) |
| customer | 8081 | 고객용 API (Swagger: http://localhost:8081/swagger-ui.html) |
| scheduler | 8083 | 스케줄러 (Outbox 처리, 좀비 세션 복구) |

## Dockerfile

멀티스테이지 빌드를 사용합니다. `MODULE` ARG로 부트스트랩 모듈을 지정하여 하나의 Dockerfile로 모든 모듈을 빌드합니다.

```
Stage 1 (builder): JDK 21 — Gradle bootJar
Stage 2 (runtime): JRE 21 — jar 실행
```

## 환경변수

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `DB_HOST` | localhost | MySQL 호스트 |
| `DB_PORT` | 3306 | MySQL 포트 |
| `DB_NAME` | ota | 데이터베이스명 |
| `DB_USER` | root | DB 사용자 |
| `DB_PASSWORD` | root | DB 비밀번호 |
| `REDIS_HOST` | localhost | Redis 호스트 |
| `REDIS_PORT` | 6379 | Redis 포트 |

IDE에서 직접 실행 시 환경변수 없이 기본값(localhost)이 사용됩니다.
Docker Compose에서는 컨테이너 네트워크 내의 서비스명(mysql, redis)을 주입합니다.
