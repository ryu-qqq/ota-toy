plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(project(":adapter-in:rest-api-core"))
    implementation(project(":adapter-in:rest-api-admin"))
    implementation(project(":adapter-out:persistence-mysql"))
    implementation(project(":adapter-out:persistence-redis"))
    implementation(project(":application"))
    implementation(project(":domain"))

    // 테스트
    testImplementation(rootProject.libs.spring.boot.starter.test)
    testImplementation(testFixtures(project(":domain")))
}
