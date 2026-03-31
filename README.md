# OTA-TOY

가상의 OTA(Online Travel Agency) 숙박 플랫폼 백엔드 시스템

## 기술 스택

| 항목 | 선택 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.6 |
| Build | Gradle 8.14.3 (Kotlin DSL) |
| Database | MySQL 8.4.0 |
| ORM | Spring Data JPA + QueryDSL 5.1.0 |
| Migration | Flyway 10.10.0 |
| Cache / Lock | Redis (Redisson 3.27.2) |
| Test | JUnit 5 + AssertJ + Mockito + Testcontainers 2.0.3 |
| Architecture Test | ArchUnit 1.2.1 |
| API 문서 | SpringDoc OpenAPI 2.8.6 |

## 아키텍처

Hexagonal Architecture (Port & Adapter) 기반 멀티모듈 구조
