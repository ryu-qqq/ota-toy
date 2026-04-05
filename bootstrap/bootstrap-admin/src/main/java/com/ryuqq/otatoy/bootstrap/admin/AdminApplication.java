package com.ryuqq.otatoy.bootstrap.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Admin API 서버 (관리자용).
 * 숙소 관리, 예약 모니터링 기능을 제공한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@SpringBootApplication(scanBasePackages = {
    "com.ryuqq.otatoy.api.core",
    "com.ryuqq.otatoy.api.admin",
    "com.ryuqq.otatoy.application",
    "com.ryuqq.otatoy.persistence"
})
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
