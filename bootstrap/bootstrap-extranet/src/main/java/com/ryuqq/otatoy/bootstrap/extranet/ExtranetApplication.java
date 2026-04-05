package com.ryuqq.otatoy.bootstrap.extranet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Extranet API 서버 (파트너용).
 * 숙소/객실/요금/재고 관리 기능을 제공한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@SpringBootApplication(scanBasePackages = {
    "com.ryuqq.otatoy.api.core",
    "com.ryuqq.otatoy.api.extranet",
    "com.ryuqq.otatoy.application",
    "com.ryuqq.otatoy.persistence"
})
public class ExtranetApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExtranetApplication.class, args);
    }
}
