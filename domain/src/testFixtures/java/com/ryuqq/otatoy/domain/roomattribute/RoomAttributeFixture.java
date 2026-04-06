package com.ryuqq.otatoy.domain.roomattribute;

import java.time.Instant;

/**
 * RoomAttribute BC 테스트용 Fixture.
 * BedType, ViewType 객체를 다양한 상태로 생성한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public final class RoomAttributeFixture {

    private RoomAttributeFixture() {}

    public static final Instant DEFAULT_NOW = Instant.parse("2026-04-04T00:00:00Z");

    // === BedType Fixture ===

    /**
     * 기본 싱글 침대 유형 (신규)
     */
    public static BedType aSingleBedType() {
        return BedType.forNew(BedTypeCode.of("SINGLE"), BedTypeName.of("싱글 침대"), DEFAULT_NOW);
    }

    /**
     * 기본 더블 침대 유형 (신규)
     */
    public static BedType aDoubleBedType() {
        return BedType.forNew(BedTypeCode.of("DOUBLE"), BedTypeName.of("더블 침대"), DEFAULT_NOW);
    }

    /**
     * DB에서 복원된 침대 유형
     */
    public static BedType reconstitutedBedType(long id, String code, String name) {
        return BedType.reconstitute(
                BedTypeId.of(id), BedTypeCode.of(code), BedTypeName.of(name),
                DEFAULT_NOW, DEFAULT_NOW
        );
    }

    // === ViewType Fixture ===

    /**
     * 기본 바다 전망 유형 (신규)
     */
    public static ViewType anOceanViewType() {
        return ViewType.forNew(ViewTypeCode.of("OCEAN"), ViewTypeName.of("바다 전망"), DEFAULT_NOW);
    }

    /**
     * 기본 산 전망 유형 (신규)
     */
    public static ViewType aMountainViewType() {
        return ViewType.forNew(ViewTypeCode.of("MOUNTAIN"), ViewTypeName.of("산 전망"), DEFAULT_NOW);
    }

    /**
     * DB에서 복원된 전망 유형
     */
    public static ViewType reconstitutedViewType(long id, String code, String name) {
        return ViewType.reconstitute(
                ViewTypeId.of(id), ViewTypeCode.of(code), ViewTypeName.of(name),
                DEFAULT_NOW, DEFAULT_NOW
        );
    }
}
