plugins {
    `java-library`
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    api(project(":application"))
    api(project(":domain"))

    implementation(rootProject.libs.spring.boot.starter)
}
