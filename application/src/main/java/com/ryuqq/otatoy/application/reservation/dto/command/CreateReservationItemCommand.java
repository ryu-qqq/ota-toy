package com.ryuqq.otatoy.application.reservation.dto.command;

import com.ryuqq.otatoy.domain.common.vo.Money;
import com.ryuqq.otatoy.domain.inventory.InventoryId;

import java.time.LocalDate;

/**
 * 예약 항목(날짜별) 생성 Command.
 * 필드는 Domain VO를 사용한다 (APP-DTO-001).
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record CreateReservationItemCommand(
    InventoryId inventoryId,
    LocalDate stayDate,
    Money nightlyRate
) {}
