package com.ryuqq.otatoy.domain.accommodation;

public record PropertyPhotoId(Long value) {

    public static PropertyPhotoId of(Long value) {
        return new PropertyPhotoId(value);
    }

    public static PropertyPhotoId forNew() { return new PropertyPhotoId(null); }

    public boolean isNew() {
        return value == null;
    }
}
