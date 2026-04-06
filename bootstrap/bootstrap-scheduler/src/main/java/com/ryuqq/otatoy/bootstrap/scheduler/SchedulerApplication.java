package com.ryuqq.otatoy.bootstrap.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Scheduler 서버.
 * 공급자 데이터 수집/동기화 등 배치 스케줄링을 담당한다.
 * REST API를 제공하지 않는 워커 프로세스.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = {
    "com.ryuqq.otatoy.scheduler",
    "com.ryuqq.otatoy.application",
    "com.ryuqq.otatoy.persistence"
})
public class SchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }
}
