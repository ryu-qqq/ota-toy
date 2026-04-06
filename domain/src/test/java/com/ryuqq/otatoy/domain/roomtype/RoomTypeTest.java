package com.ryuqq.otatoy.domain.roomtype;

import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomtype.InvalidRoomTypeException;

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
                    .isInstanceOf(InvalidRoomTypeException.class)
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
                    .isInstanceOf(InvalidRoomTypeException.class)
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
                    .isInstanceOf(InvalidRoomTypeException.class)
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
                    .isInstanceOf(InvalidRoomTypeException.class)
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

        @Test
        @DisplayName("이미 INACTIVE인 객실을 deactivate해도 상태 유지")
        void shouldStayInactiveWhenAlreadyInactive() {
            // given
            RoomType roomType = createActiveRoomType();
            Instant t1 = NOW.plusSeconds(60);
            roomType.deactivate(t1);

            // when
            Instant t2 = NOW.plusSeconds(120);
            roomType.deactivate(t2);

            // then
            assertThat(roomType.isActive()).isFalse();
            assertThat(roomType.updatedAt()).isEqualTo(t2);
        }
    }

    @Nested
    @DisplayName("정보 수정")
    class InfoUpdate {

        @Test
        @DisplayName("updateInfo로 이름, 설명, 면적, 인원 수정 성공")
        void shouldUpdateInfoSuccessfully() {
            // given
            RoomType roomType = createActiveRoomType();
            RoomTypeName newName = RoomTypeName.of("프리미엄 스위트");
            RoomTypeDescription newDesc = RoomTypeDescription.of("최고급 객실");
            Instant updateTime = NOW.plusSeconds(60);

            // when
            roomType.updateInfo(newName, newDesc, new BigDecimal("55.0"), "16평", 2, 6, updateTime);

            // then
            assertThat(roomType.name()).isEqualTo(newName);
            assertThat(roomType.description()).isEqualTo(newDesc);
            assertThat(roomType.areaSqm()).isEqualByComparingTo(new BigDecimal("55.0"));
            assertThat(roomType.areaPyeong()).isEqualTo("16평");
            assertThat(roomType.maxOccupancy()).isEqualTo(6);
            assertThat(roomType.updatedAt()).isEqualTo(updateTime);
        }

        @Test
        @DisplayName("updateInfo에서 baseOccupancy > maxOccupancy이면 실패")
        void shouldFailUpdateInfoWhenOccupancyInvalid() {
            // given
            RoomType roomType = createActiveRoomType();

            // when & then
            assertThatThrownBy(() -> roomType.updateInfo(
                    NAME, DESCRIPTION, new BigDecimal("33.5"), "10평",
                    5, 3, NOW.plusSeconds(60)
            ))
                    .isInstanceOf(InvalidRoomTypeException.class)
                    .hasMessageContaining("최대 인원은 기본 인원 이상이어야 합니다");
        }

        @Test
        @DisplayName("updateInfo에서 areaSqm이 음수이면 실패")
        void shouldFailUpdateInfoWhenAreaNegative() {
            // given
            RoomType roomType = createActiveRoomType();

            // when & then
            assertThatThrownBy(() -> roomType.updateInfo(
                    NAME, DESCRIPTION, new BigDecimal("-5"), "10평",
                    2, 4, NOW.plusSeconds(60)
            ))
                    .isInstanceOf(InvalidRoomTypeException.class)
                    .hasMessageContaining("객실 면적은 0보다 커야 합니다");
        }

        @Test
        @DisplayName("updateInventory로 기본 재고 수정 성공")
        void shouldUpdateInventorySuccessfully() {
            // given
            RoomType roomType = createActiveRoomType();
            Instant updateTime = NOW.plusSeconds(60);

            // when
            roomType.updateInventory(20, updateTime);

            // then
            assertThat(roomType.baseInventory()).isEqualTo(20);
            assertThat(roomType.updatedAt()).isEqualTo(updateTime);
        }

        @Test
        @DisplayName("updateInventory에서 음수 재고이면 실패")
        void shouldFailUpdateInventoryWhenNegative() {
            // given
            RoomType roomType = createActiveRoomType();

            // when & then
            assertThatThrownBy(() -> roomType.updateInventory(-1, NOW.plusSeconds(60)))
                    .isInstanceOf(InvalidRoomTypeException.class)
                    .hasMessageContaining("기본 재고는 0 이상이어야 합니다");
        }

        @Test
        @DisplayName("updateInventory에서 0 재고는 허용")
        void shouldAllowZeroInventory() {
            // given
            RoomType roomType = createActiveRoomType();

            // when
            roomType.updateInventory(0, NOW.plusSeconds(60));

            // then
            assertThat(roomType.baseInventory()).isZero();
        }

        @Test
        @DisplayName("updateCheckInOut으로 체크인/아웃 시간 수정 성공")
        void shouldUpdateCheckInOutSuccessfully() {
            // given
            RoomType roomType = createActiveRoomType();
            Instant updateTime = NOW.plusSeconds(60);

            // when
            roomType.updateCheckInOut(LocalTime.of(14, 0), LocalTime.of(12, 0), updateTime);

            // then
            assertThat(roomType.checkInTime()).isEqualTo(LocalTime.of(14, 0));
            assertThat(roomType.checkOutTime()).isEqualTo(LocalTime.of(12, 0));
            assertThat(roomType.updatedAt()).isEqualTo(updateTime);
        }
    }

    @Nested
    @DisplayName("VO 경계값 검증")
    class VoBoundary {

        @Test
        @DisplayName("RoomTypeName 200자 경계값 생성 성공")
        void shouldSucceedWhenNameIsExactly200Chars() {
            String name = "가".repeat(200);
            RoomTypeName roomTypeName = RoomTypeName.of(name);
            assertThat(roomTypeName.value()).hasSize(200);
        }

        @Test
        @DisplayName("RoomTypeName 201자 생성 실패")
        void shouldFailWhenNameExceeds200Chars() {
            String name = "가".repeat(201);
            assertThatThrownBy(() -> RoomTypeName.of(name))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("200자 이하여야 합니다");
        }

        @Test
        @DisplayName("RoomTypeDescription null 허용")
        void shouldAllowNullDescription() {
            RoomTypeDescription desc = RoomTypeDescription.of(null);
            assertThat(desc.value()).isNull();
        }

        @Test
        @DisplayName("RoomTypeDescription 2000자 경계값 생성 성공")
        void shouldSucceedWhenDescriptionIsExactly2000Chars() {
            String desc = "가".repeat(2000);
            RoomTypeDescription description = RoomTypeDescription.of(desc);
            assertThat(description.value()).hasSize(2000);
        }

        @Test
        @DisplayName("RoomTypeDescription 2001자 생성 실패")
        void shouldFailWhenDescriptionExceeds2000Chars() {
            String desc = "가".repeat(2001);
            assertThatThrownBy(() -> RoomTypeDescription.of(desc))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("2000자 이하여야 합니다");
        }

        @Test
        @DisplayName("RoomTypeId null 허용 (신규 엔티티)")
        void shouldAllowNullRoomTypeId() {
            RoomTypeId id = RoomTypeId.of(null);
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("RoomTypeId forNew()는 null ID")
        void shouldCreateNewRoomTypeId() {
            RoomTypeId id = RoomTypeId.forNew();
            assertThat(id.isNew()).isTrue();
        }

        @Test
        @DisplayName("baseOccupancy가 0이면 생성 실패")
        void shouldFailWhenBaseOccupancyIsZero() {
            assertThatThrownBy(() -> RoomType.forNew(
                    PROPERTY_ID, NAME, DESCRIPTION,
                    new BigDecimal("33.5"), "10평",
                    0, 4, 10,
                    LocalTime.of(15, 0), LocalTime.of(11, 0),
                    NOW
            ))
                    .isInstanceOf(InvalidRoomTypeException.class)
                    .hasMessageContaining("기본 인원은 1명 이상이어야 합니다");
        }
    }

    @Nested
    @DisplayName("동등성 검증")
    class Equality {

        @Test
        @DisplayName("같은 ID의 RoomType은 동등하다")
        void shouldBeEqualWithSameId() {
            RoomType a = RoomType.reconstitute(
                    RoomTypeId.of(1L), PROPERTY_ID, NAME, DESCRIPTION,
                    new BigDecimal("33.5"), "10평", 2, 4, 10,
                    LocalTime.of(15, 0), LocalTime.of(11, 0),
                    RoomTypeStatus.ACTIVE, NOW, NOW
            );
            RoomType b = RoomType.reconstitute(
                    RoomTypeId.of(1L), PROPERTY_ID, RoomTypeName.of("다른이름"), DESCRIPTION,
                    new BigDecimal("50"), "15평", 3, 6, 20,
                    LocalTime.of(14, 0), LocalTime.of(12, 0),
                    RoomTypeStatus.INACTIVE, NOW, NOW
            );
            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("다른 ID의 RoomType은 동등하지 않다")
        void shouldNotBeEqualWithDifferentId() {
            RoomType a = RoomType.reconstitute(
                    RoomTypeId.of(1L), PROPERTY_ID, NAME, DESCRIPTION,
                    new BigDecimal("33.5"), "10평", 2, 4, 10,
                    LocalTime.of(15, 0), LocalTime.of(11, 0),
                    RoomTypeStatus.ACTIVE, NOW, NOW
            );
            RoomType b = RoomType.reconstitute(
                    RoomTypeId.of(2L), PROPERTY_ID, NAME, DESCRIPTION,
                    new BigDecimal("33.5"), "10평", 2, 4, 10,
                    LocalTime.of(15, 0), LocalTime.of(11, 0),
                    RoomTypeStatus.ACTIVE, NOW, NOW
            );
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("ID가 null인 RoomType 두 개는 동등하지 않다")
        void shouldNotBeEqualWhenBothIdsAreNull() {
            RoomType a = createActiveRoomType();
            RoomType b = createActiveRoomType();
            assertThat(a).isNotEqualTo(b);
        }
    }

    @Nested
    @DisplayName("reconstitute 검증")
    class Reconstitute {

        @Test
        @DisplayName("reconstitute로 DB 복원 성공")
        void shouldReconstituteSuccessfully() {
            // when
            RoomType roomType = RoomType.reconstitute(
                    RoomTypeId.of(1L), PROPERTY_ID, NAME, DESCRIPTION,
                    new BigDecimal("33.5"), "10평", 2, 4, 10,
                    LocalTime.of(15, 0), LocalTime.of(11, 0),
                    RoomTypeStatus.INACTIVE, NOW, NOW
            );

            // then
            assertThat(roomType.id().value()).isEqualTo(1L);
            assertThat(roomType.id().isNew()).isFalse();
            assertThat(roomType.status()).isEqualTo(RoomTypeStatus.INACTIVE);
        }
    }
}
