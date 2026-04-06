package com.ryuqq.otatoy.application.roomtype.manager;

import com.ryuqq.otatoy.application.roomtype.port.out.RoomTypeQueryPort;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeFixture;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeNotFoundException;
import com.ryuqq.otatoy.domain.roomtype.RoomTypes;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;

/**
 * RoomTypeReadManager 단위 테스트.
 * RoomTypeQueryPort를 Mock으로 대체하여 조회/검증 로직을 확인한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@ExtendWith(MockitoExtension.class)
class RoomTypeReadManagerTest {

    @Mock
    RoomTypeQueryPort roomTypeQueryPort;

    @InjectMocks
    RoomTypeReadManager manager;

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 RoomType을 반환한다")
        void shouldReturnRoomTypeWhenFound() {
            // given
            RoomTypeId id = RoomTypeId.of(1L);
            RoomType expected = RoomTypeFixture.reconstitutedRoomType();
            given(roomTypeQueryPort.findById(id)).willReturn(Optional.of(expected));

            // when
            RoomType result = manager.getById(id);

            // then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 RoomTypeNotFoundException을 던진다")
        void shouldThrowWhenNotFound() {
            // given
            RoomTypeId id = RoomTypeId.of(999L);
            given(roomTypeQueryPort.findById(id)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> manager.getById(id))
                .isInstanceOf(RoomTypeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("verifyExists")
    class VerifyExists {

        @Test
        @DisplayName("존재하는 ID이면 예외 없이 통과한다")
        void shouldPassWhenExists() {
            // given
            RoomTypeId id = RoomTypeId.of(1L);
            given(roomTypeQueryPort.existsById(id)).willReturn(true);

            // when & then
            assertThatCode(() -> manager.verifyExists(id))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("존재하지 않는 ID이면 RoomTypeNotFoundException을 던진다")
        void shouldThrowWhenNotExists() {
            // given
            RoomTypeId id = RoomTypeId.of(999L);
            given(roomTypeQueryPort.existsById(id)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> manager.verifyExists(id))
                .isInstanceOf(RoomTypeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findByPropertyId")
    class FindByPropertyId {

        @Test
        @DisplayName("해당 숙소에 속한 객실 유형 목록을 반환한다")
        void shouldReturnRoomTypesForProperty() {
            // given
            PropertyId propertyId = PropertyId.of(1L);
            List<RoomType> expected = List.of(
                RoomTypeFixture.reconstitutedRoomTypeWithId(1L),
                RoomTypeFixture.reconstitutedRoomTypeWithId(2L)
            );
            given(roomTypeQueryPort.findByPropertyId(propertyId)).willReturn(expected);

            // when
            List<RoomType> result = manager.findByPropertyId(propertyId);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("객실이 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoRoomTypes() {
            // given
            PropertyId propertyId = PropertyId.of(999L);
            given(roomTypeQueryPort.findByPropertyId(propertyId)).willReturn(List.of());

            // when
            List<RoomType> result = manager.findByPropertyId(propertyId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findActiveByPropertyIdAndMinOccupancy")
    class FindActiveByPropertyIdAndMinOccupancy {

        @Test
        @DisplayName("조건에 맞는 활성 객실 유형 목록을 RoomTypes로 반환한다")
        void shouldReturnRoomTypesMatchingCriteria() {
            // given
            PropertyId propertyId = PropertyId.of(1L);
            int minOccupancy = 2;
            List<RoomType> roomTypeList = List.of(
                RoomTypeFixture.reconstitutedRoomTypeWithId(1L)
            );
            given(roomTypeQueryPort.findActiveByPropertyIdAndMinOccupancy(propertyId, minOccupancy))
                .willReturn(roomTypeList);

            // when
            RoomTypes result = manager.findActiveByPropertyIdAndMinOccupancy(propertyId, minOccupancy);

            // then
            assertThat(result).isNotNull();
        }
    }
}
