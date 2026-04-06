package com.ryuqq.otatoy.api.customer.reservation.mapper;

import com.ryuqq.otatoy.api.core.DateTimeFormatUtils;
import com.ryuqq.otatoy.api.customer.reservation.dto.request.ConfirmReservationApiRequest;
import com.ryuqq.otatoy.api.customer.reservation.dto.request.CreateReservationSessionApiRequest;
import com.ryuqq.otatoy.api.customer.reservation.dto.response.ReservationSessionApiResponse;
import com.ryuqq.otatoy.application.reservation.dto.command.CancelReservationCommand;
import com.ryuqq.otatoy.application.reservation.dto.command.ConfirmReservationCommand;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationItemCommand;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationLineCommand;
import com.ryuqq.otatoy.application.reservation.dto.command.CreateReservationSessionCommand;
import com.ryuqq.otatoy.application.reservation.dto.result.ReservationSessionResult;
import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.inventory.InventoryId;
import com.ryuqq.otatoy.domain.pricing.RatePlanId;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.reservation.GuestInfo;
import com.ryuqq.otatoy.domain.reservation.ReservationId;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import java.util.List;

/**
 * 예약 API Request DTO → Application Command 변환 매퍼.
 * 원시 타입 → Domain VO 변환을 전담한다. Controller에 인라인 변환 금지.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public final class ReservationApiMapper {

    private ReservationApiMapper() {}

    /**
     * 예약 세션 결과를 API 응답 DTO로 변환한다.
     * Money → BigDecimal, Instant → "yyyy-MM-dd HH:mm:ss" (Asia/Seoul) 문자열로 변환.
     *
     * @param result Application 결과 DTO
     * @return API 응답 DTO
     */
    public static ReservationSessionApiResponse toSessionResponse(ReservationSessionResult result) {
        return new ReservationSessionApiResponse(
            result.sessionId(),
            result.totalAmount().amount(),
            result.checkIn().toString(),
            result.checkOut().toString(),
            result.guestCount(),
            DateTimeFormatUtils.formatDateTime(result.expiresAt())
        );
    }

    /**
     * 예약 취소 요청을 Command로 변환한다.
     *
     * @param reservationId 예약 ID (PathVariable)
     * @param cancelReason  취소 사유 (nullable)
     * @return Application Command
     */
    public static CancelReservationCommand toCancelCommand(Long reservationId, String cancelReason) {
        return new CancelReservationCommand(
            ReservationId.of(reservationId),
            cancelReason
        );
    }

    /**
     * 예약 세션 생성 요청을 Command로 변환한다.
     *
     * @param idempotencyKey 멱등성 키 (헤더에서 추출)
     * @param request        API 요청 DTO
     * @return Application Command
     */
    public static CreateReservationSessionCommand toSessionCommand(
            String idempotencyKey,
            CreateReservationSessionApiRequest request) {

        return new CreateReservationSessionCommand(
            idempotencyKey,
            PropertyId.of(request.propertyId()),
            RoomTypeId.of(request.roomTypeId()),
            RatePlanId.of(request.ratePlanId()),
            request.checkIn(),
            request.checkOut(),
            request.guestCount(),
            Money.of(request.totalAmount())
        );
    }

    /**
     * 예약 확정 요청을 Command로 변환한다.
     *
     * @param request API 요청 DTO
     * @return Application Command
     */
    public static ConfirmReservationCommand toConfirmCommand(ConfirmReservationApiRequest request) {
        return new ConfirmReservationCommand(
            request.sessionId(),
            request.customerId(),
            GuestInfo.of(
                request.guestInfo().name(),
                request.guestInfo().phone(),
                request.guestInfo().email()
            ),
            request.bookingSnapshot(),
            toLineCommands(request.lines())
        );
    }

    private static List<CreateReservationLineCommand> toLineCommands(
            List<ConfirmReservationApiRequest.ReservationLineApiRequest> lines) {

        return lines.stream()
            .map(line -> new CreateReservationLineCommand(
                RatePlanId.of(line.ratePlanId()),
                line.roomCount(),
                Money.of(line.subtotalAmount()),
                toItemCommands(line.items())
            ))
            .toList();
    }

    private static List<CreateReservationItemCommand> toItemCommands(
            List<ConfirmReservationApiRequest.ReservationItemApiRequest> items) {

        return items.stream()
            .map(item -> new CreateReservationItemCommand(
                InventoryId.of(item.inventoryId()),
                item.stayDate(),
                Money.of(item.nightlyRate())
            ))
            .toList();
    }
}
