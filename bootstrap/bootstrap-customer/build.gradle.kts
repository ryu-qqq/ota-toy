plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(project(":adapter-in:rest-api-core"))
    implementation(project(":adapter-in:rest-api-customer"))
    implementation(project(":adapter-out:persistence-mysql"))
    implementation(project(":adapter-out:persistence-redis"))
    implementation(project(":application"))
    implementation(project(":domain"))

    // 모니터링 (Actuator + Prometheus)
    implementation(rootProject.libs.spring.boot.starter.actuator)
    implementation(rootProject.libs.micrometer.registry.prometheus)

    // 테스트
    testImplementation(rootProject.libs.spring.boot.starter.test)
    testImplementation(testFixtures(project(":domain")))
    testImplementation(platform(rootProject.libs.testcontainers.bom))
    testImplementation(rootProject.libs.bundles.testcontainers)
    testImplementation("org.testcontainers:testcontainers:2.0.3")
    testImplementation(rootProject.libs.spring.boot.starter.data.jpa)
    testRuntimeOnly(rootProject.libs.mysql.connector)
}
