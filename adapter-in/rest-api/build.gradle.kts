plugins {
    `java-library`
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))

    implementation(rootProject.libs.spring.boot.starter.web)
    implementation(rootProject.libs.spring.boot.starter.validation)

    // API 문서
    implementation(rootProject.libs.springdoc.openapi.starter.webmvc.ui)

    // 테스트
    testImplementation(rootProject.libs.spring.boot.starter.test)
    testImplementation(testFixtures(project(":domain")))
}
