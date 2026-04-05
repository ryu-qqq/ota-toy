package com.ryuqq.otatoy.api.extranet;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * rest-api-extranet 모듈 테스트용 Spring Boot 설정 클래스.
 * 이 모듈에는 main @SpringBootApplication이 없으므로 @WebMvcTest가 탐색할 설정을 제공한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@SpringBootApplication(scanBasePackages = "com.ryuqq.otatoy.api")
class TestWebMvcConfig {
}
