package com.ryuqq.otatoy.domain.common.vo;

/**
 * CDN URL. nullable이며, 값이 있을 경우 blank 불가.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
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
