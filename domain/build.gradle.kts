plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    // 순수 도메인 모듈 — 프레임워크 의존성 없음

    // 테스트
    testImplementation(rootProject.libs.archunit.junit5)
}
