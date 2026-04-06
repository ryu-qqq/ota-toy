plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(project(":adapter-in:scheduler"))
    implementation(project(":adapter-out:persistence-mysql"))
    implementation(project(":adapter-out:persistence-redis"))
    implementation(project(":application"))
    implementation(project(":domain"))

    implementation(rootProject.libs.spring.boot.starter)

    // 테스트
    testImplementation(rootProject.libs.spring.boot.starter.test)
}
