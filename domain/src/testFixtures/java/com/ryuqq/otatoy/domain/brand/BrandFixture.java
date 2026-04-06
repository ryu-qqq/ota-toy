package com.ryuqq.otatoy.domain.brand;

import java.time.Instant;

/**
 * Brand BC 테스트용 Fixture.
 * 다양한 상태의 Brand를 생성한다.
 */
public final class BrandFixture {

    private BrandFixture() {}

    // === 기본 상수 ===
    public static final Instant DEFAULT_NOW = Instant.parse("2026-04-04T00:00:00Z");
    public static final BrandName DEFAULT_NAME = BrandName.of("TestBrand");
    public static final BrandNameKr DEFAULT_NAME_KR = BrandNameKr.of("테스트브랜드");
    public static final LogoUrl DEFAULT_LOGO_URL = LogoUrl.of("https://example.com/logo.png");

    // === Brand Fixture ===

    /**
     * 신규 Brand (id = null)
     */
    public static Brand newBrand() {
        return Brand.forNew(DEFAULT_NAME, DEFAULT_NAME_KR, DEFAULT_LOGO_URL, DEFAULT_NOW);
    }

    /**
     * DB 복원된 Brand (id = 1L)
     */
    public static Brand reconstitutedBrand() {
        return Brand.reconstitute(
                BrandId.of(1L), DEFAULT_NAME, DEFAULT_NAME_KR, DEFAULT_LOGO_URL,
                DEFAULT_NOW, DEFAULT_NOW
        );
    }

    /**
     * 지정 ID의 Brand
     */
    public static Brand brandWithId(long id) {
        return Brand.reconstitute(
                BrandId.of(id), DEFAULT_NAME, DEFAULT_NAME_KR, DEFAULT_LOGO_URL,
                DEFAULT_NOW, DEFAULT_NOW
        );
    }

    /**
     * nullable 필드가 모두 null인 Brand
     */
    public static Brand minimalBrand() {
        return Brand.forNew(DEFAULT_NAME, BrandNameKr.of(null), LogoUrl.of(null), DEFAULT_NOW);
    }
}
