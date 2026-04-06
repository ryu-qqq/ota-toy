plugins {
    `java-library`
    `java-test-fixtures`
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    api(project(":domain"))

    implementation(rootProject.libs.spring.context)
    implementation(rootProject.libs.spring.tx)
    implementation(rootProject.libs.jackson.databind)
    implementation(rootProject.libs.jackson.datatype.jsr310)

    // domain 테스트 픽스처 재사용
    testImplementation(testFixtures(project(":domain")))
    testFixturesImplementation(project(":domain"))
    testFixturesImplementation(testFixtures(project(":domain")))

    // Spring Boot BOM이 JUnit 5.12.x로 upgrade하면서 platform-launcher 버전 불일치 해소
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
