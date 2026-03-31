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
    testImplementation(rootProject.libs.bundles.testcontainers)
    testImplementation(testFixtures(project(":domain")))
}

// QueryDSL 생성 소스 설정
sourceSets {
    main {
        java {
            srcDir("build/generated/sources/annotationProcessor/java/main")
        }
    }
}
