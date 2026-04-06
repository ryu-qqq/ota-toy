package com.ryuqq.otatoy.domain.roomtype;

import com.ryuqq.otatoy.domain.property.PropertyId;

import java.util.List;
import java.util.stream.Stream;

/**
 * 객실 유형 일급 컬렉션.
 * ID 추출, 인원 필터링 등 컬렉션 연산을 캡슐화한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public class RoomTypes {

    private final List<RoomType> items;

    private RoomTypes(List<RoomType> items) {
        this.items = items;
    }

    public static RoomTypes from(List<RoomType> items) {
        if (items == null || items.isEmpty()) {
            return new RoomTypes(List.of());
        }
        return new RoomTypes(List.copyOf(items));
    }

    public List<RoomTypeId> roomTypeIds() {
        return items.stream()
                .map(RoomType::id)
                .toList();
    }

    public Stream<RoomType> stream() {
        return items.stream();
    }

    public List<RoomType> items() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
