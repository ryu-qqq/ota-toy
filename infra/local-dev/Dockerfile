# 멀티스테이지 빌드 — 공통 Dockerfile
# ARG로 부트스트랩 모듈을 지정하여 하나의 Dockerfile로 모든 모듈을 빌드한다.

# Stage 1: 빌드
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .
ARG MODULE
RUN ./gradlew :bootstrap:${MODULE}:bootJar -x test --no-daemon

# Stage 2: 실행
FROM eclipse-temurin:21-jre
WORKDIR /app
ARG MODULE
COPY --from=builder /app/bootstrap/${MODULE}/build/libs/*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE="local"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
