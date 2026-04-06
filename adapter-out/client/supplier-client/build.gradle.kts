plugins {
    `java-library`
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))

    implementation(rootProject.libs.spring.boot.starter.web)
    implementation(rootProject.libs.resilience4j.circuitbreaker)
    implementation(rootProject.libs.resilience4j.retry)

    testImplementation(rootProject.libs.spring.boot.starter.test)
    testImplementation(testFixtures(project(":domain")))
}
