package com.ryuqq.otatoy.domain.common.infra;

public interface CacheKey {
    String key();
    long ttlSeconds();
}
