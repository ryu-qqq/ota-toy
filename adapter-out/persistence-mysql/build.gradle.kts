plugins {
    `java-library`
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))

    // Spring Data JPA + QueryDSL
    implementation(rootProject.libs.spring.boot.starter.data.jpa)
    implementation(rootProject.libs.querydsl.jpa) {
        artifact {
            classifier = "jakarta"
        }
    }
    annotationProcessor(rootProject.libs.querydsl.apt) {
        artifact {
            classifier = "jakarta"
        }
    }
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // Database
    runtimeOnly(rootProject.libs.mysql.connector)
    implementation(rootProject.libs.flyway.core)
    implementation(rootProject.libs.flyway.mysql)

    // 테스트
    testImplementation(rootProject.libs.spring.boot.starter.test)
    testImplementation(platform(rootProject.libs.testcontainers.bom))
    testImplementation("org.testcontainers:testcontainers:2.0.3")
    testImplementation(rootProject.libs.bundles.testcontainers)
    testImplementation(testFixtures(project(":domain")))
}

// Testcontainers Docker 설정 (macOS Docker Desktop 호환)
tasks.test {
    environment("DOCKER_HOST", "unix://${System.getProperty("user.home")}/.docker/run/docker.sock")
    environment("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", "/var/run/docker.sock")
}

// QueryDSL 생성 소스 설정
sourceSets {
    main {
        java {
            srcDir("build/generated/sources/annotationProcessor/java/main")
        }
    }
}
