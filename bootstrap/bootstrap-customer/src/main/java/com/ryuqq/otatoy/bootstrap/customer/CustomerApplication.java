package com.ryuqq.otatoy.bootstrap.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Customer API 서버 (고객용).
 * 숙소 검색, 요금 조회, 예약 생성/취소 기능을 제공한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@SpringBootApplication(scanBasePackages = {
    "com.ryuqq.otatoy.api.core",
    "com.ryuqq.otatoy.api.customer",
    "com.ryuqq.otatoy.application",
    "com.ryuqq.otatoy.persistence"
})
public class CustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerApplication.class, args);
    }
}
