plugins {
    `java-library`
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    api(project(":domain"))

    implementation(rootProject.libs.spring.context)
    implementation(rootProject.libs.spring.tx)

    // domain 테스트 픽스처 재사용
    testImplementation(testFixtures(project(":domain")))
}
