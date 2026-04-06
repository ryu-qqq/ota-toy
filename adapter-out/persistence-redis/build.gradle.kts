plugins {
    `java-library`
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))

    // Redis (Redisson)
    implementation(rootProject.libs.redisson.spring.boot.starter)

    // 모니터링 메트릭
    implementation(rootProject.libs.micrometer.core)

    // 테스트
    testImplementation(rootProject.libs.spring.boot.starter.test)
    testImplementation(testFixtures(project(":domain")))
    testImplementation(platform(rootProject.libs.testcontainers.bom))
    testImplementation("org.testcontainers:testcontainers:2.0.3")
    testImplementation(rootProject.libs.testcontainers.junit.jupiter)
}
