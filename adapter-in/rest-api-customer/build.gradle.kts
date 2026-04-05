plugins {
    `java-library`
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation(project(":adapter-in:rest-api-core"))
    implementation(project(":application"))

    // 테스트
    testImplementation(rootProject.libs.spring.boot.starter.test)
    testImplementation(testFixtures(project(":domain")))
}
