package com.ryuqq.otatoy.domain.property;

import com.ryuqq.otatoy.domain.accommodation.PhotoType;
import com.ryuqq.otatoy.domain.common.vo.CdnUrl;
import com.ryuqq.otatoy.domain.common.vo.OriginUrl;

import java.time.Instant;

/**
 * PropertyPhoto 테스트용 Fixture.
 * 다양한 유형의 PropertyPhoto 객체를 생성한다.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public final class PropertyPhotoFixture {

    private PropertyPhotoFixture() {}

    // === 기본 상수 ===
    public static final Instant DEFAULT_NOW = Instant.parse("2026-04-04T00:00:00Z");
    public static final PropertyId DEFAULT_PROPERTY_ID = PropertyId.of(1L);
    public static final OriginUrl DEFAULT_ORIGIN_URL = OriginUrl.of("https://example.com/photo.jpg");
    public static final CdnUrl DEFAULT_CDN_URL = CdnUrl.of("https://cdn.example.com/photo.jpg");

    // === 신규 생성 Fixture ===

    /**
     * 기본 외관 사진
     */
    public static PropertyPhoto aPropertyPhoto() {
        return PropertyPhoto.forNew(
            DEFAULT_PROPERTY_ID, PhotoType.EXTERIOR,
            DEFAULT_ORIGIN_URL, DEFAULT_CDN_URL, 1, DEFAULT_NOW
        );
    }

    /**
     * 지정 유형의 사진
     */
    public static PropertyPhoto aPropertyPhotoWithType(PhotoType photoType, int sortOrder) {
        return PropertyPhoto.forNew(
            DEFAULT_PROPERTY_ID, photoType,
            DEFAULT_ORIGIN_URL, DEFAULT_CDN_URL, sortOrder, DEFAULT_NOW
        );
    }

    /**
     * 지정 숙소 ID의 사진
     */
    public static PropertyPhoto aPropertyPhotoForProperty(long propertyId, PhotoType photoType, int sortOrder) {
        return PropertyPhoto.forNew(
            PropertyId.of(propertyId), photoType,
            OriginUrl.of("https://example.com/" + sortOrder + ".jpg"),
            CdnUrl.of("https://cdn.example.com/" + sortOrder + ".jpg"),
            sortOrder, DEFAULT_NOW
        );
    }

    /**
     * CDN URL 없는 사진
     */
    public static PropertyPhoto aPropertyPhotoWithoutCdn() {
        return PropertyPhoto.forNew(
            DEFAULT_PROPERTY_ID, PhotoType.EXTERIOR,
            DEFAULT_ORIGIN_URL, null, 1, DEFAULT_NOW
        );
    }
}
