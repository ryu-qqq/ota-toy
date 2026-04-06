package com.ryuqq.otatoy.application.pricing.manager;

import com.ryuqq.otatoy.application.pricing.port.out.RatePlanQueryPort;
import com.ryuqq.otatoy.domain.pricing.PricingFixtures;
import com.ryuqq.otatoy.domain.pricing.RatePlan;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.pricing.RatePlanNotFoundException;
import com.ryuqq.otatoy.domain.pricing.RatePlans;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

/**
 * RatePlanReadManager 단위 테스트.
 * Port 위임과 예외 발생 로직을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class RatePlanReadManagerTest {

    @Mock RatePlanQueryPort ratePlanQueryPort;
    @InjectMocks RatePlanReadManager manager;

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("존재하는 ID로 조회 시 RatePlan을 반환한다")
        void shouldReturnRatePlanWhenExists() {
            // given
            RatePlanId id = RatePlanId.of(1L);
            RatePlan expected = PricingFixtures.reconstitutedRatePlan(1L);
            given(ratePlanQueryPort.findById(id)).willReturn(Optional.of(expected));

            // when
            RatePlan result = manager.getById(id);

            // then
            assertThat(result).isSameAs(expected);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 RatePlanNotFoundException이 발생한다")
        void shouldThrowWhenNotFound() {
            // given
            RatePlanId id = RatePlanId.of(999L);
            given(ratePlanQueryPort.findById(id)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> manager.getById(id))
                .isInstanceOf(RatePlanNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("verifyExists")
    class VerifyExists {

        @Test
        @DisplayName("존재하는 ID로 검증 시 예외가 발생하지 않는다")
        void shouldPassWhenExists() {
            // given
            RatePlanId id = RatePlanId.of(1L);
            given(ratePlanQueryPort.existsById(id)).willReturn(true);

            // when & then
            assertThatCode(() -> manager.verifyExists(id)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("존재하지 않는 ID로 검증 시 RatePlanNotFoundException이 발생한다")
        void shouldThrowWhenNotExists() {
            // given
            RatePlanId id = RatePlanId.of(999L);
            given(ratePlanQueryPort.existsById(id)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> manager.verifyExists(id))
                .isInstanceOf(RatePlanNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findByRoomTypeIds")
    class FindByRoomTypeIds {

        @Test
        @DisplayName("RoomTypeId 목록에 해당하는 RatePlans를 반환한다")
        void shouldReturnRatePlansForRoomTypeIds() {
            // given
            List<RoomTypeId> roomTypeIds = List.of(RoomTypeId.of(1L), RoomTypeId.of(2L));
            RatePlan plan1 = PricingFixtures.reconstitutedRatePlan(1L);
            given(ratePlanQueryPort.findByRoomTypeIds(roomTypeIds)).willReturn(List.of(plan1));

            // when
            RatePlans result = manager.findByRoomTypeIds(roomTypeIds);

            // then
            assertThat(result.isEmpty()).isFalse();
            assertThat(result.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("일치하는 RatePlan이 없으면 빈 RatePlans를 반환한다")
        void shouldReturnEmptyRatePlansWhenNoneFound() {
            // given
            List<RoomTypeId> roomTypeIds = List.of(RoomTypeId.of(99L));
            given(ratePlanQueryPort.findByRoomTypeIds(roomTypeIds)).willReturn(List.of());

            // when
            RatePlans result = manager.findByRoomTypeIds(roomTypeIds);

            // then
            assertThat(result.isEmpty()).isTrue();
        }
    }
}
