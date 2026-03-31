plugins {
    `java-library`
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))

    // Redis (Redisson)
    implementation(rootProject.libs.redisson.spring.boot.starter)

    // 테스트
    testImplementation(rootProject.libs.spring.boot.starter.test)
    testImplementation(testFixtures(project(":domain")))
}
