package com.ryuqq.otatoy.domain.propertytype;

import java.time.Instant;

/**
 * PropertyType BC 테스트용 Fixture.
 * 다양한 상태의 PropertyType, PropertyTypeAttribute를 생성한다.
 */
public final class PropertyTypeFixture {

    private PropertyTypeFixture() {}

    // === 기본 상수 ===
    public static final Instant DEFAULT_NOW = Instant.parse("2026-04-04T00:00:00Z");
    public static final PropertyTypeCode DEFAULT_CODE = PropertyTypeCode.of("HOTEL");
    public static final PropertyTypeName DEFAULT_NAME = PropertyTypeName.of("호텔");
    public static final PropertyTypeDescription DEFAULT_DESCRIPTION = PropertyTypeDescription.of("일반 호텔");

    // === PropertyType Fixture ===

    /**
     * 신규 PropertyType (id = null)
     */
    public static PropertyType newPropertyType() {
        return PropertyType.forNew(DEFAULT_CODE, DEFAULT_NAME, DEFAULT_DESCRIPTION, DEFAULT_NOW);
    }

    /**
     * DB 복원된 PropertyType (id = 1L)
     */
    public static PropertyType reconstitutedPropertyType() {
        return PropertyType.reconstitute(
                PropertyTypeId.of(1L), DEFAULT_CODE, DEFAULT_NAME, DEFAULT_DESCRIPTION,
                DEFAULT_NOW, DEFAULT_NOW
        );
    }

    /**
     * 지정 ID의 PropertyType
     */
    public static PropertyType propertyTypeWithId(long id) {
        return PropertyType.reconstitute(
                PropertyTypeId.of(id), DEFAULT_CODE, DEFAULT_NAME, DEFAULT_DESCRIPTION,
                DEFAULT_NOW, DEFAULT_NOW
        );
    }

    // === PropertyTypeAttribute Fixture ===

    /**
     * 신규 필수 속성
     */
    public static PropertyTypeAttribute requiredAttribute() {
        return PropertyTypeAttribute.forNew(
                PropertyTypeId.of(1L), "star_rating", "성급", "INTEGER",
                true, 1, DEFAULT_NOW
        );
    }

    /**
     * 신규 선택 속성
     */
    public static PropertyTypeAttribute optionalAttribute() {
        return PropertyTypeAttribute.forNew(
                PropertyTypeId.of(1L), "pool_type", "수영장 유형", "STRING",
                false, 2, DEFAULT_NOW
        );
    }

    /**
     * DB 복원된 속성
     */
    public static PropertyTypeAttribute reconstitutedAttribute() {
        return PropertyTypeAttribute.reconstitute(
                PropertyTypeAttributeId.of(1L), PropertyTypeId.of(1L),
                "star_rating", "성급", "INTEGER",
                true, 1, DEFAULT_NOW, DEFAULT_NOW
        );
    }
}
