package com.ryuqq.otatoy.persistence.redis.support;

import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Inventory Redis 키 생성 유틸리티.
 * 키 형식: inventory:{roomTypeId}:{yyyy-MM-dd}
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public final class InventoryKeyResolver {

    private static final String PREFIX = "inventory";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private InventoryKeyResolver() {}

    public static String resolve(RoomTypeId roomTypeId, LocalDate date) {
        return PREFIX + ":" + roomTypeId.value() + ":" + date.format(DATE_FORMAT);
    }

    public static List<String> resolveAll(RoomTypeId roomTypeId, List<LocalDate> dates) {
        return dates.stream()
            .map(date -> resolve(roomTypeId, date))
            .toList();
    }
}
