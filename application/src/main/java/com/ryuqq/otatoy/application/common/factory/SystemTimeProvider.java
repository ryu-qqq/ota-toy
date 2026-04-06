package com.ryuqq.otatoy.application.common.factory;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 시스템 시계 기반 TimeProvider 구현체.
 * 운영 환경에서 사용한다. 테스트에서는 고정 시각을 반환하는 Mock으로 대체 가능.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SystemTimeProvider implements TimeProvider {

    @Override
    public Instant now() {
        return Instant.now();
    }

    @Override
    public LocalDate today() {
        return LocalDate.now();
    }
}
