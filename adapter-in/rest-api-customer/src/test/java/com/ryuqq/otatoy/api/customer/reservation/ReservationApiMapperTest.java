package com.ryuqq.otatoy.api.customer.reservation;

import com.ryuqq.otatoy.api.customer.reservation.dto.request.ConfirmReservationApiRequest;
import com.ryuqq.otatoy.api.customer.reservation.dto.request.ConfirmReservationApiRequest.GuestInfoApiRequest;
import com.ryuqq.otatoy.api.customer.reservation.dto.request.ConfirmReservationApiRequest.ReservationItemApiRequest;
import com.ryuqq.otatoy.api.customer.reservation.dto.request.ConfirmReservationApiRequest.ReservationLineApiRequest;
import com.ryuqq.otatoy.api.customer.reservation.dto.request.CreateReservationSessionApiRequest;
import com.ryuqq.otatoy.api.customer.reservation.dto.response.ReservationSessionApiResponse;
import com.ryuqq.otatoy.api.customer.reservation.mapper.ReservationApiMapper;
import com.ryuqq.otatoy.application.reservation.dto.command.CancelReservationCommand;
import com.ryuqq.otatoy.application.reservation.dto.command.ConfirmReservationCommand;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationSessionCommand;
import com.ryuqq.otatoy.application.reservation.dto.result.ReservationSessionResult;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.inventory.InventoryId;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.reservation.ReservationId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReservationApiMapper 단위 테스트.
 * Request -> Command, Result -> Response 변환 로직을 검증한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
class ReservationApiMapperTest {

    // =========================================================================
    // toSessionCommand: (idempotencyKey, Request) -> CreateReservationSessionCommand
    // =========================================================================

    @Nested
    @DisplayName("toSessionCommand - 예약 세션 생성 요청 변환")
    class ToSessionCommand {

        @Test
        @DisplayName("멱등성 키가 올바르게 전달된다")
        void shouldPassIdempotencyKey() {
            var request = new CreateReservationSessionApiRequest(
                1L, 1L, 1L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 3),
                2,
                BigDecimal.valueOf(240000)
            );

            CreateReservationSessionCommand command =
                ReservationApiMapper.toSessionCommand("idem-key-123", request);

            assertThat(command.idempotencyKey()).isEqualTo("idem-key-123");
        }

        @Test
        @DisplayName("원시 ID 필드가 도메인 VO로 변환된다")
        void shouldConvertIdsToDomainVOs() {
            var request = new CreateReservationSessionApiRequest(
                10L, 20L, 30L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 3),
                2,
                BigDecimal.valueOf(240000)
            );

            CreateReservationSessionCommand command =
                ReservationApiMapper.toSessionCommand("key", request);

            assertThat(command.propertyId()).isEqualTo(PropertyId.of(10L));
            assertThat(command.roomTypeId()).isEqualTo(RoomTypeId.of(20L));
            assertThat(command.ratePlanId()).isEqualTo(RatePlanId.of(30L));
        }

        @Test
        @DisplayName("날짜와 인원수가 올바르게 매핑된다")
        void shouldMapDateAndGuestCount() {
            var request = new CreateReservationSessionApiRequest(
                1L, 1L, 1L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 3),
                3,
                BigDecimal.valueOf(360000)
            );

            CreateReservationSessionCommand command =
                ReservationApiMapper.toSessionCommand("key", request);

            assertThat(command.checkIn()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(command.checkOut()).isEqualTo(LocalDate.of(2026, 6, 3));
            assertThat(command.guestCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("totalAmount가 Money VO로 변환된다")
        void shouldConvertTotalAmountToMoney() {
            var request = new CreateReservationSessionApiRequest(
                1L, 1L, 1L,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 3),
                2,
                BigDecimal.valueOf(240000)
            );

            CreateReservationSessionCommand command =
                ReservationApiMapper.toSessionCommand("key", request);

            assertThat(command.totalAmount()).isEqualTo(Money.of(BigDecimal.valueOf(240000)));
        }
    }

    // =========================================================================
    // toConfirmCommand: ConfirmReservationApiRequest -> ConfirmReservationCommand
    // =========================================================================

    @Nested
    @DisplayName("toConfirmCommand - 예약 확정 요청 변환")
    class ToConfirmCommand {

        @Test
        @DisplayName("sessionId와 customerId가 올바르게 매핑된다")
        void shouldMapSessionAndCustomer() {
            var request = createConfirmRequest();

            ConfirmReservationCommand command = ReservationApiMapper.toConfirmCommand(request);

            assertThat(command.sessionId()).isEqualTo(1L);
            assertThat(command.customerId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("GuestInfo가 도메인 VO로 올바르게 변환된다")
        void shouldConvertGuestInfo() {
            var request = createConfirmRequest();

            ConfirmReservationCommand command = ReservationApiMapper.toConfirmCommand(request);

            assertThat(command.guestInfo().name()).isEqualTo("홍길동");
            assertThat(command.guestInfo().phoneValue()).isEqualTo("010-1234-5678");
            assertThat(command.guestInfo().emailValue()).isEqualTo("hong@test.com");
        }

        @Test
        @DisplayName("bookingSnapshot이 올바르게 전달된다")
        void shouldPassBookingSnapshot() {
            var request = createConfirmRequest();

            ConfirmReservationCommand command = ReservationApiMapper.toConfirmCommand(request);

            assertThat(command.bookingSnapshot()).isEqualTo("스냅샷 데이터");
        }

        @Test
        @DisplayName("예약 라인이 올바르게 변환된다")
        void shouldConvertLines() {
            var request = createConfirmRequest();

            ConfirmReservationCommand command = ReservationApiMapper.toConfirmCommand(request);

            assertThat(command.lines()).hasSize(1);

            var line = command.lines().get(0);
            assertThat(line.ratePlanId()).isEqualTo(RatePlanId.of(1L));
            assertThat(line.roomCount()).isEqualTo(1);
            assertThat(line.subtotalAmount()).isEqualTo(Money.of(BigDecimal.valueOf(240000)));
        }

        @Test
        @DisplayName("예약 라인의 항목(items)이 올바르게 변환된다")
        void shouldConvertLineItems() {
            var request = createConfirmRequest();

            ConfirmReservationCommand command = ReservationApiMapper.toConfirmCommand(request);

            var items = command.lines().get(0).items();
            assertThat(items).hasSize(2);

            assertThat(items.get(0).inventoryId()).isEqualTo(InventoryId.of(1L));
            assertThat(items.get(0).stayDate()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(items.get(0).nightlyRate()).isEqualTo(Money.of(BigDecimal.valueOf(120000)));

            assertThat(items.get(1).inventoryId()).isEqualTo(InventoryId.of(2L));
            assertThat(items.get(1).stayDate()).isEqualTo(LocalDate.of(2026, 6, 2));
        }

        @Test
        @DisplayName("다중 예약 라인이 올바르게 변환된다")
        void shouldConvertMultipleLines() {
            var items1 = List.of(
                new ReservationItemApiRequest(1L, LocalDate.of(2026, 6, 1), BigDecimal.valueOf(120000))
            );
            var items2 = List.of(
                new ReservationItemApiRequest(3L, LocalDate.of(2026, 6, 1), BigDecimal.valueOf(80000))
            );

            var lines = List.of(
                new ReservationLineApiRequest(1L, 1, BigDecimal.valueOf(120000), items1),
                new ReservationLineApiRequest(2L, 2, BigDecimal.valueOf(160000), items2)
            );

            var request = new ConfirmReservationApiRequest(
                1L, 100L,
                new GuestInfoApiRequest("홍길동", null, null),
                null, lines
            );

            ConfirmReservationCommand command = ReservationApiMapper.toConfirmCommand(request);

            assertThat(command.lines()).hasSize(2);
            assertThat(command.lines().get(0).ratePlanId()).isEqualTo(RatePlanId.of(1L));
            assertThat(command.lines().get(1).ratePlanId()).isEqualTo(RatePlanId.of(2L));
            assertThat(command.lines().get(1).roomCount()).isEqualTo(2);
        }
    }

    // =========================================================================
    // toCancelCommand: (reservationId, cancelReason) -> CancelReservationCommand
    // =========================================================================

    @Nested
    @DisplayName("toCancelCommand - 예약 취소 요청 변환")
    class ToCancelCommand {

        @Test
        @DisplayName("reservationId가 ReservationId VO로 변환된다")
        void shouldConvertReservationId() {
            CancelReservationCommand command =
                ReservationApiMapper.toCancelCommand(100L, "일정 변경");

            assertThat(command.reservationId()).isEqualTo(ReservationId.of(100L));
        }

        @Test
        @DisplayName("cancelReason이 올바르게 전달된다")
        void shouldPassCancelReason() {
            CancelReservationCommand command =
                ReservationApiMapper.toCancelCommand(100L, "일정 변경으로 취소합니다");

            assertThat(command.cancelReason()).isEqualTo("일정 변경으로 취소합니다");
        }

        @Test
        @DisplayName("cancelReason이 null이어도 올바르게 전달된다")
        void shouldHandleNullCancelReason() {
            CancelReservationCommand command =
                ReservationApiMapper.toCancelCommand(100L, null);

            assertThat(command.cancelReason()).isNull();
        }
    }

    // =========================================================================
    // toSessionResponse: ReservationSessionResult -> ReservationSessionApiResponse
    // =========================================================================

    @Nested
    @DisplayName("toSessionResponse - 세션 결과 -> API 응답 변환")
    class ToSessionResponse {

        @Test
        @DisplayName("sessionId가 올바르게 매핑된다")
        void shouldMapSessionId() {
            var result = createSessionResult();

            ReservationSessionApiResponse response =
                ReservationApiMapper.toSessionResponse(result);

            assertThat(response.sessionId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Money가 BigDecimal로 변환된다")
        void shouldConvertMoneyToBigDecimal() {
            var result = createSessionResult();

            ReservationSessionApiResponse response =
                ReservationApiMapper.toSessionResponse(result);

            assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(240000));
        }

        @Test
        @DisplayName("checkIn, checkOut이 문자열로 변환된다")
        void shouldConvertDatesToString() {
            var result = createSessionResult();

            ReservationSessionApiResponse response =
                ReservationApiMapper.toSessionResponse(result);

            assertThat(response.checkIn()).isEqualTo("2026-06-01");
            assertThat(response.checkOut()).isEqualTo("2026-06-03");
        }

        @Test
        @DisplayName("guestCount가 올바르게 매핑된다")
        void shouldMapGuestCount() {
            var result = createSessionResult();

            ReservationSessionApiResponse response =
                ReservationApiMapper.toSessionResponse(result);

            assertThat(response.guestCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("expiresAt이 KST yyyy-MM-dd HH:mm:ss 포맷으로 변환된다")
        void shouldFormatExpiresAtAsKST() {
            Instant expiresAt = Instant.parse("2026-06-01T01:00:00Z");
            var result = new ReservationSessionResult(
                1L, Money.of(240000),
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 3),
                2, expiresAt
            );

            ReservationSessionApiResponse response =
                ReservationApiMapper.toSessionResponse(result);

            // UTC 01:00 -> KST 10:00
            String expected = expiresAt.atZone(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            assertThat(response.expiresAt()).isEqualTo(expected);
        }
    }

    // =========================================================================
    // 헬퍼 메서드
    // =========================================================================

    private static ConfirmReservationApiRequest createConfirmRequest() {
        var items = List.of(
            new ReservationItemApiRequest(1L, LocalDate.of(2026, 6, 1), BigDecimal.valueOf(120000)),
            new ReservationItemApiRequest(2L, LocalDate.of(2026, 6, 2), BigDecimal.valueOf(120000))
        );

        var lines = List.of(
            new ReservationLineApiRequest(1L, 1, BigDecimal.valueOf(240000), items)
        );

        return new ConfirmReservationApiRequest(
            1L, 100L,
            new GuestInfoApiRequest("홍길동", "010-1234-5678", "hong@test.com"),
            "스냅샷 데이터",
            lines
        );
    }

    private static ReservationSessionResult createSessionResult() {
        return new ReservationSessionResult(
            1L,
            Money.of(240000),
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 3),
            2,
            Instant.parse("2026-06-01T01:00:00Z")
        );
    }
}
