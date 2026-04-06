package com.ryuqq.otatoy.persistence.redis.adapter;

import com.ryuqq.otatoy.application.pricing.port.out.RateCachePort;
import com.ryuqq.otatoy.persistence.redis.config.RateCacheProperties;
import com.ryuqq.otatoy.persistence.redis.support.RateCacheKeyResolver;
import org.redisson.api.RBatch;
import org.redisson.api.BatchResult;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Rate 캐시 Redis Adapter.
 * RateCachePort를 구현하며, Redisson Batch로 MGET/MSET을 처리한다.
 * Key 생성은 KeyResolver, TTL 설정은 Properties에 위임한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class RateCacheAdapter implements RateCachePort {

    private final RedissonClient redissonClient;
    private final RateCacheKeyResolver keyResolver;
    private final RateCacheProperties properties;

    public RateCacheAdapter(RedissonClient redissonClient,
                             RateCacheKeyResolver keyResolver,
                             RateCacheProperties properties) {
        this.redissonClient = redissonClient;
        this.keyResolver = keyResolver;
        this.properties = properties;
    }

    @Override
    public Map<String, BigDecimal> multiGet(List<Long> ratePlanIds, List<LocalDate> dates) {
        if (ratePlanIds == null || ratePlanIds.isEmpty() || dates == null || dates.isEmpty()) {
            return Map.of();
        }

        RBatch batch = redissonClient.createBatch();
        List<String> resultKeys = new java.util.ArrayList<>();

        for (Long ratePlanId : ratePlanIds) {
            for (LocalDate date : dates) {
                batch.<String>getBucket(keyResolver.resolve(ratePlanId, date)).getAsync();
                resultKeys.add(keyResolver.resultKey(ratePlanId, date));
            }
        }

        BatchResult<?> batchResult = batch.execute();
        List<?> responses = batchResult.getResponses();

        Map<String, BigDecimal> cached = new HashMap<>();
        for (int i = 0; i < resultKeys.size(); i++) {
            Object value = responses.get(i);
            if (value != null) {
                cached.put(resultKeys.get(i), new BigDecimal(value.toString()));
            }
        }

        return cached;
    }

    @Override
    public void multiSet(Map<String, BigDecimal> rates) {
        if (rates == null || rates.isEmpty()) {
            return;
        }

        RBatch batch = redissonClient.createBatch();
        long ttlMillis = properties.getTtlMillis();

        for (Map.Entry<String, BigDecimal> entry : rates.entrySet()) {
            String redisKey = properties.getKeyPrefix() + entry.getKey();
            batch.getBucket(redisKey)
                    .setAsync(entry.getValue().toPlainString(), ttlMillis, TimeUnit.MILLISECONDS);
        }

        batch.execute();
    }
}
