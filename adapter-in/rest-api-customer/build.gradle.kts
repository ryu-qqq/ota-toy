plugins {
    `java-library`
    id("org.asciidoctor.jvm.convert") version "3.3.2"
}

apply(plugin = "io.spring.dependency-management")

val snippetsDir = file("build/generated-snippets")

dependencies {
    implementation(project(":adapter-in:rest-api-core"))
    implementation(project(":application"))

    // 테스트
    testImplementation(rootProject.libs.spring.boot.starter.test)
    testImplementation(testFixtures(project(":domain")))
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
}

tasks.test {
    outputs.dir(snippetsDir)
}

val coreDocsDir = project(":adapter-in:rest-api-core").file("src/docs/asciidocs")

tasks.asciidoctor {
    inputs.dir(snippetsDir)
    dependsOn(tasks.test)
    setSourceDir(file("src/docs/asciidocs"))
    sources { include("index.adoc") }
    setOutputDir(file("build/docs/asciidoc"))
    attributes(mapOf(
        "snippets" to snippetsDir.absolutePath,
        "stylesheet" to "custom-style.css",
        "stylesdir" to coreDocsDir.absolutePath,
        "coreDocsDir" to coreDocsDir.absolutePath
    ))
}
