package com.ryuqq.otatoy.domain.common.vo;

public record CdnUrl(String value) {

    public static CdnUrl of(String value) {
        return new CdnUrl(value);
    }
}
