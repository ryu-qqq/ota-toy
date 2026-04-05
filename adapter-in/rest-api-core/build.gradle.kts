plugins {
    `java-library`
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    api(project(":application"))
    api(project(":domain"))

    // Spring Web
    api(rootProject.libs.spring.boot.starter.web)
    api(rootProject.libs.spring.boot.starter.validation)

    // API 문서
    api(rootProject.libs.springdoc.openapi.starter.webmvc.ui)

    // 테스트
    testImplementation(rootProject.libs.spring.boot.starter.test)
    testImplementation(testFixtures(project(":domain")))
}
