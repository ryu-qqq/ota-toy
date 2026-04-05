package com.ryuqq.otatoy.application.roomtype.dto;

import com.ryuqq.otatoy.domain.roomtype.RoomType;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeBed;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeView;

import java.util.List;

/**
 * RoomType + RoomTypeBed + RoomTypeView를 묶는 번들 객체.
 * Factory가 생성하고, PersistenceFacade가 소비한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record RoomTypeBundle(
    RoomType roomType,
    List<RoomTypeBed> beds,
    List<RoomTypeView> views
) {}
