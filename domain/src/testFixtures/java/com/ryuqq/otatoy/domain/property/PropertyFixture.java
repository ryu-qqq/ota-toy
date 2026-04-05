package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.brand.BrandId;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;

import java.time.Instant;

/**
 * Property BC 테스트용 Fixture.
 * 다양한 상태의 Property 객체를 생성한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class PropertyFixture {

    private PropertyFixture() {}

    // === 기본 상수 ===
    public static final Instant DEFAULT_NOW = Instant.parse("2026-04-04T00:00:00Z");
    public static final PartnerId DEFAULT_PARTNER_ID = PartnerId.of(1L);
    public static final BrandId DEFAULT_BRAND_ID = BrandId.of(1L);
    public static final PropertyTypeId DEFAULT_PROPERTY_TYPE_ID = PropertyTypeId.of(1L);
    public static final PropertyName DEFAULT_NAME = PropertyName.of("테스트 호텔");
    public static final PropertyDescription DEFAULT_DESCRIPTION = PropertyDescription.of("테스트 설명");
    public static final Location DEFAULT_LOCATION = Location.of("서울시 강남구", 37.5665, 126.978, "강남", "서울");
    public static final PromotionText DEFAULT_PROMOTION_TEXT = PromotionText.of("프로모션");

    // === 신규 생성 Fixture ===

    /**
     * 기본 신규 ACTIVE 상태 숙소
     */
    public static Property aProperty() {
        return Property.forNew(
            DEFAULT_PARTNER_ID, DEFAULT_BRAND_ID, DEFAULT_PROPERTY_TYPE_ID,
            DEFAULT_NAME, DEFAULT_DESCRIPTION,
            DEFAULT_LOCATION, DEFAULT_PROMOTION_TEXT, DEFAULT_NOW
        );
    }

    /**
     * 지정 이름의 신규 숙소
     */
    public static Property aPropertyWithName(String name) {
        return Property.forNew(
            DEFAULT_PARTNER_ID, DEFAULT_BRAND_ID, DEFAULT_PROPERTY_TYPE_ID,
            PropertyName.of(name), DEFAULT_DESCRIPTION,
            DEFAULT_LOCATION, DEFAULT_PROMOTION_TEXT, DEFAULT_NOW
        );
    }

    /**
     * 지정 시간의 신규 숙소
     */
    public static Property aPropertyWithTime(Instant now) {
        return Property.forNew(
            DEFAULT_PARTNER_ID, DEFAULT_BRAND_ID, DEFAULT_PROPERTY_TYPE_ID,
            DEFAULT_NAME, DEFAULT_DESCRIPTION,
            DEFAULT_LOCATION, DEFAULT_PROMOTION_TEXT, now
        );
    }

    /**
     * nullable 필드가 null인 신규 숙소
     */
    public static Property aPropertyWithoutOptional(String name) {
        return Property.forNew(
            DEFAULT_PARTNER_ID, null, DEFAULT_PROPERTY_TYPE_ID,
            PropertyName.of(name), null,
            Location.of("서울시 강남구 테스트로 123", 37.5665, 126.9780, null, null),
            null, DEFAULT_NOW
        );
    }

    // === DB 복원 Fixture ===

    /**
     * DB에서 복원된 ACTIVE 숙소
     */
    public static Property reconstitutedProperty() {
        return Property.reconstitute(
            PropertyId.of(1L), DEFAULT_PARTNER_ID, DEFAULT_BRAND_ID, DEFAULT_PROPERTY_TYPE_ID,
            DEFAULT_NAME, DEFAULT_DESCRIPTION, DEFAULT_LOCATION, DEFAULT_PROMOTION_TEXT,
            PropertyStatus.ACTIVE, DEFAULT_NOW, DEFAULT_NOW
        );
    }

    /**
     * 지정 ID의 DB 복원 숙소
     */
    public static Property reconstitutedPropertyWithId(long id) {
        return Property.reconstitute(
            PropertyId.of(id), DEFAULT_PARTNER_ID, DEFAULT_BRAND_ID, DEFAULT_PROPERTY_TYPE_ID,
            DEFAULT_NAME, DEFAULT_DESCRIPTION, DEFAULT_LOCATION, DEFAULT_PROMOTION_TEXT,
            PropertyStatus.ACTIVE, DEFAULT_NOW, DEFAULT_NOW
        );
    }

    /**
     * INACTIVE 상태의 DB 복원 숙소
     */
    public static Property inactiveProperty() {
        return Property.reconstitute(
            PropertyId.of(1L), DEFAULT_PARTNER_ID, DEFAULT_BRAND_ID, DEFAULT_PROPERTY_TYPE_ID,
            DEFAULT_NAME, DEFAULT_DESCRIPTION, DEFAULT_LOCATION, DEFAULT_PROMOTION_TEXT,
            PropertyStatus.INACTIVE, DEFAULT_NOW, DEFAULT_NOW
        );
    }
}
