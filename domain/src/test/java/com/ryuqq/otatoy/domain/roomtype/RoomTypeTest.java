package com.ryuqq.otatoy.domain.roomtype;

import com.ryuqq.otatoy.domain.property.PropertyId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomTypeTest {

    private static final Instant NOW = Instant.now();
    private static final PropertyId PROPERTY_ID = PropertyId.of(1L);
    private static final RoomTypeName NAME = RoomTypeName.of("스탠다드");
    private static final RoomTypeDescription DESCRIPTION = RoomTypeDescription.of("기본 객실");

    private RoomType createActiveRoomType() {
        return RoomType.forNew(
                PROPERTY_ID, NAME, DESCRIPTION,
                new BigDecimal("33.5"), "10평",
                2, 4, 10,
                LocalTime.of(15, 0), LocalTime.of(11, 0),
                NOW
        );
    }

    @Nested
    @DisplayName("생성 검증")
    class Creation {

        @Test
        @DisplayName("RoomType forNew() 정상 생성")
        void shouldCreateRoomTypeSuccessfully() {
            // when
            RoomType roomType = createActiveRoomType();

            // then
            assertThat(roomType).isNotNull();
            assertThat(roomType.id()).isNull();
            assertThat(roomType.propertyId()).isEqualTo(PROPERTY_ID);
            assertThat(roomType.name()).isEqualTo(NAME);
            assertThat(roomType.status()).isEqualTo(RoomTypeStatus.ACTIVE);
            assertThat(roomType.baseOccupancy()).isEqualTo(2);
            assertThat(roomType.maxOccupancy()).isEqualTo(4);
        }

        @Test
        @DisplayName("baseOccupancy > maxOccupancy인 RoomType 생성 실패 (회귀 방지)")
        void shouldFailWhenBaseOccupancyExceedsMaxOccupancy() {
            // when & then
            assertThatThrownBy(() -> RoomType.forNew(
                    PROPERTY_ID, RoomTypeName.of("디럭스"), RoomTypeDescription.of("설명"),
                    new BigDecimal("40"), "12평",
                    5, 3, 10,
                    LocalTime.of(15, 0), LocalTime.of(11, 0),
                    NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("최대 인원은 기본 인원 이상이어야 합니다");
        }

        @Test
        @DisplayName("areaSqm이 0인 RoomType 생성 실패")
        void shouldFailWhenAreaSqmIsZero() {
            assertThatThrownBy(() -> RoomType.forNew(
                    PROPERTY_ID, NAME, DESCRIPTION,
                    BigDecimal.ZERO, "0평",
                    2, 4, 10,
                    LocalTime.of(15, 0), LocalTime.of(11, 0),
                    NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("객실 면적은 0보다 커야 합니다");
        }

        @Test
        @DisplayName("areaSqm이 음수인 RoomType 생성 실패")
        void shouldFailWhenAreaSqmIsNegative() {
            assertThatThrownBy(() -> RoomType.forNew(
                    PROPERTY_ID, NAME, DESCRIPTION,
                    new BigDecimal("-10"), "0평",
                    2, 4, 10,
                    LocalTime.of(15, 0), LocalTime.of(11, 0),
                    NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("객실 면적은 0보다 커야 합니다");
        }

        @Test
        @DisplayName("객실명이 빈 값이면 생성 실패")
        void shouldFailWhenNameIsBlank() {
            assertThatThrownBy(() -> RoomTypeName.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("객실 유형명은 필수입니다");
        }

        @Test
        @DisplayName("객실명이 null이면 생성 실패")
        void shouldFailWhenNameIsNull() {
            assertThatThrownBy(() -> RoomTypeName.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("객실 유형명은 필수입니다");
        }

        @Test
        @DisplayName("기본 재고가 음수이면 생성 실패")
        void shouldFailWhenBaseInventoryIsNegative() {
            assertThatThrownBy(() -> RoomType.forNew(
                    PROPERTY_ID, NAME, DESCRIPTION,
                    new BigDecimal("33.5"), "10평",
                    2, 4, -1,
                    LocalTime.of(15, 0), LocalTime.of(11, 0),
                    NOW
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("기본 재고는 0 이상이어야 합니다");
        }

        @Test
        @DisplayName("areaSqm이 null이면 생성 성공 (선택 값)")
        void shouldSucceedWhenAreaSqmIsNull() {
            // when
            RoomType roomType = RoomType.forNew(
                    PROPERTY_ID, NAME, DESCRIPTION,
                    null, null,
                    2, 4, 10,
                    LocalTime.of(15, 0), LocalTime.of(11, 0),
                    NOW
            );

            // then
            assertThat(roomType.areaSqm()).isNull();
        }
    }

    @Nested
    @DisplayName("상태 전이")
    class StateTransition {

        @Test
        @DisplayName("ACTIVE → INACTIVE → ACTIVE 양방향 전이")
        void shouldToggleRoomTypeStatus() {
            // given
            RoomType roomType = createActiveRoomType();
            assertThat(roomType.isActive()).isTrue();

            // when — 비활성화
            Instant t1 = NOW.plusSeconds(60);
            roomType.deactivate(t1);

            // then
            assertThat(roomType.isActive()).isFalse();
            assertThat(roomType.status()).isEqualTo(RoomTypeStatus.INACTIVE);
            assertThat(roomType.updatedAt()).isEqualTo(t1);

            // when — 재활성화
            Instant t2 = NOW.plusSeconds(120);
            roomType.activate(t2);

            // then
            assertThat(roomType.isActive()).isTrue();
            assertThat(roomType.status()).isEqualTo(RoomTypeStatus.ACTIVE);
            assertThat(roomType.updatedAt()).isEqualTo(t2);
        }
    }
}
