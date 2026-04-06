package com.ryuqq.otatoy.api.extranet.roomtype.mapper;

import com.ryuqq.otatoy.api.extranet.roomtype.dto.request.RegisterRoomTypeApiRequest;
import com.ryuqq.otatoy.application.roomtype.dto.command.RegisterRoomTypeCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RoomTypeApiMapper 단위 테스트.
 * 객실 유형 등록 Request -> Command 변환 정확성을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@DisplayName("RoomTypeApiMapper")
class RoomTypeApiMapperTest {

    @Nested
    @DisplayName("toCommand")
    class ToCommand {

        @Nested
        @DisplayName("전체 필드가 포함된 요청")
        class AllFields {

            @Test
            @DisplayName("모든 필드가 정확하게 변환된다")
            void shouldMapAllFieldsToCommand() {
                // given
                var request = new RegisterRoomTypeApiRequest(
                    "디럭스 더블", "넓은 객실",
                    new BigDecimal("33.5"), "10평",
                    2, 4, 5,
                    "15:00", "11:00",
                    List.of(
                        new RegisterRoomTypeApiRequest.BedItem(1L, 1),
                        new RegisterRoomTypeApiRequest.BedItem(2L, 2)
                    ),
                    List.of(
                        new RegisterRoomTypeApiRequest.ViewItem(1L),
                        new RegisterRoomTypeApiRequest.ViewItem(2L)
                    )
                );

                // when
                RegisterRoomTypeCommand command = RoomTypeApiMapper.toCommand(10L, request);

                // then
                assertThat(command.propertyId().value()).isEqualTo(10L);
                assertThat(command.name().value()).isEqualTo("디럭스 더블");
                assertThat(command.description().value()).isEqualTo("넓은 객실");
                assertThat(command.areaSqm()).isEqualByComparingTo(new BigDecimal("33.5"));
                assertThat(command.areaPyeong()).isEqualTo("10평");
                assertThat(command.baseOccupancy()).isEqualTo(2);
                assertThat(command.maxOccupancy()).isEqualTo(4);
                assertThat(command.baseInventory()).isEqualTo(5);
            }
        }

        @Nested
        @DisplayName("시간 파싱")
        class TimeParsing {

            @Test
            @DisplayName("checkInTime 문자열이 LocalTime으로 정확하게 파싱된다")
            void shouldParseCheckInTime() {
                var request = createMinimalRequest("15:00", "11:00");

                RegisterRoomTypeCommand command = RoomTypeApiMapper.toCommand(1L, request);

                assertThat(command.checkInTime()).isEqualTo(LocalTime.of(15, 0));
            }

            @Test
            @DisplayName("checkOutTime 문자열이 LocalTime으로 정확하게 파싱된다")
            void shouldParseCheckOutTime() {
                var request = createMinimalRequest("14:00", "11:30");

                RegisterRoomTypeCommand command = RoomTypeApiMapper.toCommand(1L, request);

                assertThat(command.checkOutTime()).isEqualTo(LocalTime.of(11, 30));
            }
        }

        @Nested
        @DisplayName("nullable 필드 처리")
        class NullableFields {

            @Test
            @DisplayName("description이 null이면 Command의 description도 null이다")
            void shouldMapNullDescriptionToNull() {
                var request = new RegisterRoomTypeApiRequest(
                    "스탠다드", null,
                    new BigDecimal("20"), null,
                    2, 2, 3,
                    "15:00", "11:00",
                    null, null
                );

                RegisterRoomTypeCommand command = RoomTypeApiMapper.toCommand(1L, request);

                assertThat(command.description()).isNull();
            }
        }

        @Nested
        @DisplayName("beds 리스트 변환")
        class BedsMapping {

            @Test
            @DisplayName("beds가 null이면 빈 리스트로 변환된다")
            void shouldMapNullBedsToEmptyList() {
                var request = new RegisterRoomTypeApiRequest(
                    "스탠다드", null,
                    new BigDecimal("20"), null,
                    2, 2, 3,
                    "15:00", "11:00",
                    null, null
                );

                RegisterRoomTypeCommand command = RoomTypeApiMapper.toCommand(1L, request);

                assertThat(command.beds()).isEmpty();
            }

            @Test
            @DisplayName("beds 항목이 BedTypeId VO와 quantity로 정확하게 변환된다")
            void shouldMapBedItems() {
                var request = new RegisterRoomTypeApiRequest(
                    "스탠다드", null,
                    new BigDecimal("20"), null,
                    2, 2, 3,
                    "15:00", "11:00",
                    List.of(
                        new RegisterRoomTypeApiRequest.BedItem(1L, 2),
                        new RegisterRoomTypeApiRequest.BedItem(3L, 1)
                    ),
                    null
                );

                RegisterRoomTypeCommand command = RoomTypeApiMapper.toCommand(1L, request);

                assertThat(command.beds()).hasSize(2);
                assertThat(command.beds().get(0).bedTypeId().value()).isEqualTo(1L);
                assertThat(command.beds().get(0).quantity()).isEqualTo(2);
                assertThat(command.beds().get(1).bedTypeId().value()).isEqualTo(3L);
                assertThat(command.beds().get(1).quantity()).isEqualTo(1);
            }
        }

        @Nested
        @DisplayName("views 리스트 변환")
        class ViewsMapping {

            @Test
            @DisplayName("views가 null이면 빈 리스트로 변환된다")
            void shouldMapNullViewsToEmptyList() {
                var request = new RegisterRoomTypeApiRequest(
                    "스탠다드", null,
                    new BigDecimal("20"), null,
                    2, 2, 3,
                    "15:00", "11:00",
                    null, null
                );

                RegisterRoomTypeCommand command = RoomTypeApiMapper.toCommand(1L, request);

                assertThat(command.views()).isEmpty();
            }

            @Test
            @DisplayName("views 항목이 ViewTypeId VO로 정확하게 변환된다")
            void shouldMapViewItems() {
                var request = new RegisterRoomTypeApiRequest(
                    "스탠다드", null,
                    new BigDecimal("20"), null,
                    2, 2, 3,
                    "15:00", "11:00",
                    null,
                    List.of(
                        new RegisterRoomTypeApiRequest.ViewItem(1L),
                        new RegisterRoomTypeApiRequest.ViewItem(2L)
                    )
                );

                RegisterRoomTypeCommand command = RoomTypeApiMapper.toCommand(1L, request);

                assertThat(command.views()).hasSize(2);
                assertThat(command.views().get(0).viewTypeId().value()).isEqualTo(1L);
                assertThat(command.views().get(1).viewTypeId().value()).isEqualTo(2L);
            }
        }
    }

    private RegisterRoomTypeApiRequest createMinimalRequest(String checkIn, String checkOut) {
        return new RegisterRoomTypeApiRequest(
            "스탠다드", null,
            new BigDecimal("20"), null,
            2, 2, 3,
            checkIn, checkOut,
            null, null
        );
    }
}
