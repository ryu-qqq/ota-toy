package com.ryuqq.otatoy.application.common.factory;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 시간 제공 인터페이스.
 * Factory에서만 주입받아 사용한다 (APP-FAC-001).
 * 테스트에서는 FixedTimeProvider로 교체하여 시간을 제어한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public interface TimeProvider {

    Instant now();

    LocalDate today();
}
