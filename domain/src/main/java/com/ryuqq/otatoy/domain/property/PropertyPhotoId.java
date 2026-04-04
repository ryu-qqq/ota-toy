package com.ryuqq.otatoy.domain.property;

/**
 * 숙소 사진 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record PropertyPhotoId(Long value) {

    public static PropertyPhotoId of(Long value) {
        return new PropertyPhotoId(value);
    }

    public static PropertyPhotoId forNew() { return new PropertyPhotoId(null); }

    public boolean isNew() {
        return value == null;
    }
}
