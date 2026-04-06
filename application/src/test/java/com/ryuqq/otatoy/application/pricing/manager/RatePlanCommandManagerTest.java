package com.ryuqq.otatoy.application.pricing.manager;

import com.ryuqq.otatoy.application.pricing.port.out.RatePlanCommandPort;
import com.ryuqq.otatoy.domain.pricing.PricingFixtures;
import com.ryuqq.otatoy.domain.pricing.RatePlan;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * RatePlanCommandManager 단위 테스트.
 * Port 위임과 반환값 전달을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class RatePlanCommandManagerTest {

    @Mock RatePlanCommandPort ratePlanCommandPort;
    @InjectMocks RatePlanCommandManager manager;

    @Nested
    @DisplayName("persist")
    class Persist {

        @Test
        @DisplayName("RatePlan을 Port에 위임하고 생성된 ID를 반환한다")
        void shouldDelegateToPortAndReturnId() {
            // given
            RatePlan ratePlan = PricingFixtures.directRatePlan();
            given(ratePlanCommandPort.persist(ratePlan)).willReturn(42L);

            // when
            Long result = manager.persist(ratePlan);

            // then
            assertThat(result).isEqualTo(42L);
            then(ratePlanCommandPort).should().persist(ratePlan);
        }
    }
}
