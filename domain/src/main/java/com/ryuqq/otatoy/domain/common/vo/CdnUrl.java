package com.ryuqq.otatoy.domain.common.vo;

public record CdnUrl(String value) {

    public CdnUrl {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException("CDN URL이 비어있을 수 없습니다");
        }
    }

    public static CdnUrl of(String value) {
        return new CdnUrl(value);
    }
}
