package com.ryuqq.otatoy.domain.roomtype;

import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.roomattribute.BedTypeId;
import com.ryuqq.otatoy.domain.roomattribute.ViewTypeId;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

/**
 * RoomType BC 테스트용 Fixture.
 * 다양한 상태의 RoomType, RoomTypeBed, RoomTypeView 객체를 생성한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class RoomTypeFixture {

    private RoomTypeFixture() {}

    // === 기본 상수 ===
    public static final Instant DEFAULT_NOW = Instant.parse("2026-04-04T00:00:00Z");
    public static final PropertyId DEFAULT_PROPERTY_ID = PropertyId.of(1L);
    public static final RoomTypeName DEFAULT_NAME = RoomTypeName.of("디럭스 더블");
    public static final RoomTypeDescription DEFAULT_DESCRIPTION = RoomTypeDescription.of("넓은 객실");
    public static final BigDecimal DEFAULT_AREA_SQM = BigDecimal.valueOf(33.0);
    public static final String DEFAULT_AREA_PYEONG = "10평";
    public static final int DEFAULT_BASE_OCCUPANCY = 2;
    public static final int DEFAULT_MAX_OCCUPANCY = 4;
    public static final int DEFAULT_BASE_INVENTORY = 5;
    public static final LocalTime DEFAULT_CHECK_IN = LocalTime.of(15, 0);
    public static final LocalTime DEFAULT_CHECK_OUT = LocalTime.of(11, 0);

    // === RoomType Fixture ===

    /**
     * 기본 신규 ACTIVE 객실 유형
     */
    public static RoomType aRoomType() {
        return RoomType.forNew(
            DEFAULT_PROPERTY_ID, DEFAULT_NAME, DEFAULT_DESCRIPTION,
            DEFAULT_AREA_SQM, DEFAULT_AREA_PYEONG,
            DEFAULT_BASE_OCCUPANCY, DEFAULT_MAX_OCCUPANCY, DEFAULT_BASE_INVENTORY,
            DEFAULT_CHECK_IN, DEFAULT_CHECK_OUT, DEFAULT_NOW
        );
    }

    /**
     * 지정 이름의 신규 객실 유형
     */
    public static RoomType aRoomTypeWithName(String name) {
        return RoomType.forNew(
            DEFAULT_PROPERTY_ID, RoomTypeName.of(name), DEFAULT_DESCRIPTION,
            DEFAULT_AREA_SQM, DEFAULT_AREA_PYEONG,
            DEFAULT_BASE_OCCUPANCY, DEFAULT_MAX_OCCUPANCY, DEFAULT_BASE_INVENTORY,
            DEFAULT_CHECK_IN, DEFAULT_CHECK_OUT, DEFAULT_NOW
        );
    }

    /**
     * 지정 시간의 신규 객실 유형
     */
    public static RoomType aRoomTypeWithTime(Instant now) {
        return RoomType.forNew(
            DEFAULT_PROPERTY_ID, DEFAULT_NAME, DEFAULT_DESCRIPTION,
            DEFAULT_AREA_SQM, DEFAULT_AREA_PYEONG,
            DEFAULT_BASE_OCCUPANCY, DEFAULT_MAX_OCCUPANCY, DEFAULT_BASE_INVENTORY,
            DEFAULT_CHECK_IN, DEFAULT_CHECK_OUT, now
        );
    }

    /**
     * DB에서 복원된 ACTIVE 객실 유형
     */
    public static RoomType reconstitutedRoomType() {
        return RoomType.reconstitute(
            RoomTypeId.of(1L), DEFAULT_PROPERTY_ID, DEFAULT_NAME, DEFAULT_DESCRIPTION,
            DEFAULT_AREA_SQM, DEFAULT_AREA_PYEONG,
            DEFAULT_BASE_OCCUPANCY, DEFAULT_MAX_OCCUPANCY, DEFAULT_BASE_INVENTORY,
            DEFAULT_CHECK_IN, DEFAULT_CHECK_OUT,
            RoomTypeStatus.ACTIVE, DEFAULT_NOW, DEFAULT_NOW
        );
    }

    /**
     * 지정 ID의 DB 복원 객실 유형
     */
    public static RoomType reconstitutedRoomTypeWithId(long id) {
        return RoomType.reconstitute(
            RoomTypeId.of(id), DEFAULT_PROPERTY_ID, DEFAULT_NAME, DEFAULT_DESCRIPTION,
            DEFAULT_AREA_SQM, DEFAULT_AREA_PYEONG,
            DEFAULT_BASE_OCCUPANCY, DEFAULT_MAX_OCCUPANCY, DEFAULT_BASE_INVENTORY,
            DEFAULT_CHECK_IN, DEFAULT_CHECK_OUT,
            RoomTypeStatus.ACTIVE, DEFAULT_NOW, DEFAULT_NOW
        );
    }

    // === RoomTypeBed Fixture ===

    /**
     * 기본 침대 구성 (더블 침대 1개)
     */
    public static RoomTypeBed aRoomTypeBed() {
        return RoomTypeBed.forNew(RoomTypeId.of(1L), BedTypeId.of(1L), 1, DEFAULT_NOW);
    }

    /**
     * Pending 상태의 침대 구성 (부모 ID 미할당)
     */
    public static RoomTypeBed aPendingRoomTypeBed(long bedTypeId, int quantity) {
        return RoomTypeBed.forPending(BedTypeId.of(bedTypeId), quantity, DEFAULT_NOW);
    }

    /**
     * 기본 Pending 침대 구성 리스트
     */
    public static List<RoomTypeBed> defaultPendingBeds() {
        return List.of(
            RoomTypeBed.forPending(BedTypeId.of(1L), 1, DEFAULT_NOW),
            RoomTypeBed.forPending(BedTypeId.of(2L), 2, DEFAULT_NOW)
        );
    }

    // === RoomTypeView Fixture ===

    /**
     * 기본 전망 (도시 전망)
     */
    public static RoomTypeView aRoomTypeView() {
        return RoomTypeView.forNew(RoomTypeId.of(1L), ViewTypeId.of(1L), DEFAULT_NOW);
    }

    /**
     * Pending 상태의 전망 (부모 ID 미할당)
     */
    public static RoomTypeView aPendingRoomTypeView(long viewTypeId) {
        return RoomTypeView.forPending(ViewTypeId.of(viewTypeId), DEFAULT_NOW);
    }

    /**
     * 기본 Pending 전망 리스트
     */
    public static List<RoomTypeView> defaultPendingViews() {
        return List.of(
            RoomTypeView.forPending(ViewTypeId.of(1L), DEFAULT_NOW)
        );
    }
}
