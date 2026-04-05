package com.ryuqq.otatoy.api.customer;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * rest-api-customer 모듈 테스트용 Spring Boot 설정.
 * 라이브러리 모듈이므로 @SpringBootApplication이 없어 테스트 시 필요하다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@SpringBootApplication(scanBasePackages = {
        "com.ryuqq.otatoy.api.customer",
        "com.ryuqq.otatoy.api.core"
})
public class TestApplication {
}
