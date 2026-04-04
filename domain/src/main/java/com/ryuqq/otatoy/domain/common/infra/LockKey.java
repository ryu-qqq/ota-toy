package com.ryuqq.otatoy.domain.common.infra;

public interface LockKey {
    String key();
    long waitTimeMillis();
    long leaseTimeMillis();
}
