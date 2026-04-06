package com.ryuqq.otatoy.application.roomtype.factory;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.roomtype.dto.RoomTypeBundle;
import com.ryuqq.otatoy.application.roomtype.dto.command.RegisterRoomTypeCommand;
import com.ryuqq.otatoy.application.roomtype.dto.command.RegisterRoomTypeCommandFixture;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomattribute.BedTypeId;
import com.ryuqq.otatoy.domain.roomattribute.ViewTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeDescription;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeName;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * RoomTypeFactory 단위 테스트.
 * TimeProvider를 Mock으로 대체하여 시간 일원화 및 번들 생성 로직을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class RoomTypeFactoryTest {

    @Mock
    TimeProvider timeProvider;

    @InjectMocks
    RoomTypeFactory factory;

    private static final Instant FIXED_NOW = Instant.parse("2026-04-06T00:00:00Z");

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("침대와 전망이 포함된 커맨드로 번들을 생성한다")
        void shouldCreateBundleWithBedsAndViews() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            var command = RegisterRoomTypeCommandFixture.aRegisterRoomTypeCommand();

            // when
            RoomTypeBundle bundle = factory.createBundle(command);

            // then
            assertThat(bundle.roomType()).isNotNull();
            assertThat(bundle.beds()).hasSize(2);
            assertThat(bundle.views()).hasSize(1);
        }

        @Test
        @DisplayName("TimeProvider에서 제공한 시간이 RoomType 생성에 사용된다")
        void shouldUseTimeProviderForCreation() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            var command = RegisterRoomTypeCommandFixture.aRegisterRoomTypeCommand();

            // when
            RoomTypeBundle bundle = factory.createBundle(command);

            // then
            assertThat(bundle.roomType().createdAt()).isEqualTo(FIXED_NOW);
        }

        @Test
        @DisplayName("생성된 Bed의 roomTypeId는 null(Pending)이다")
        void shouldCreateBedsWithPendingRoomTypeId() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            var command = RegisterRoomTypeCommandFixture.aRegisterRoomTypeCommand();

            // when
            RoomTypeBundle bundle = factory.createBundle(command);

            // then
            bundle.beds().forEach(bed ->
                assertThat(bed.roomTypeId()).isNull()
            );
        }

        @Test
        @DisplayName("생성된 View의 roomTypeId는 null(Pending)이다")
        void shouldCreateViewsWithPendingRoomTypeId() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            var command = RegisterRoomTypeCommandFixture.aRegisterRoomTypeCommand();

            // when
            RoomTypeBundle bundle = factory.createBundle(command);

            // then
            bundle.views().forEach(view ->
                assertThat(view.roomTypeId()).isNull()
            );
        }
    }

    @Nested
    @DisplayName("침대/전망이 null인 경우")
    class NullBedsAndViews {

        @Test
        @DisplayName("beds가 null이면 빈 리스트로 번들을 생성한다")
        void shouldCreateEmptyBedsWhenNull() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            var command = new RegisterRoomTypeCommand(
                PropertyId.of(1L),
                RoomTypeName.of("스탠다드"),
                RoomTypeDescription.of("기본 객실"),
                BigDecimal.valueOf(25.0),
                "8평",
                2, 2, 3,
                LocalTime.of(15, 0),
                LocalTime.of(11, 0),
                null,
                List.of(new RegisterRoomTypeCommand.ViewItem(ViewTypeId.of(1L)))
            );

            // when
            RoomTypeBundle bundle = factory.createBundle(command);

            // then
            assertThat(bundle.beds()).isEmpty();
            assertThat(bundle.views()).hasSize(1);
        }

        @Test
        @DisplayName("views가 null이면 빈 리스트로 번들을 생성한다")
        void shouldCreateEmptyViewsWhenNull() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            var command = new RegisterRoomTypeCommand(
                PropertyId.of(1L),
                RoomTypeName.of("스탠다드"),
                RoomTypeDescription.of("기본 객실"),
                BigDecimal.valueOf(25.0),
                "8평",
                2, 2, 3,
                LocalTime.of(15, 0),
                LocalTime.of(11, 0),
                List.of(new RegisterRoomTypeCommand.BedItem(BedTypeId.of(1L), 1)),
                null
            );

            // when
            RoomTypeBundle bundle = factory.createBundle(command);

            // then
            assertThat(bundle.beds()).hasSize(1);
            assertThat(bundle.views()).isEmpty();
        }

        @Test
        @DisplayName("beds와 views 모두 null이면 빈 리스트로 번들을 생성한다")
        void shouldCreateEmptyBundleWhenBothNull() {
            // given
            given(timeProvider.now()).willReturn(FIXED_NOW);
            var command = new RegisterRoomTypeCommand(
                PropertyId.of(1L),
                RoomTypeName.of("스탠다드"),
                RoomTypeDescription.of("기본 객실"),
                BigDecimal.valueOf(25.0),
                "8평",
                2, 2, 3,
                LocalTime.of(15, 0),
                LocalTime.of(11, 0),
                null,
                null
            );

            // when
            RoomTypeBundle bundle = factory.createBundle(command);

            // then
            assertThat(bundle.roomType()).isNotNull();
            assertThat(bundle.beds()).isEmpty();
            assertThat(bundle.views()).isEmpty();
        }
    }
}
