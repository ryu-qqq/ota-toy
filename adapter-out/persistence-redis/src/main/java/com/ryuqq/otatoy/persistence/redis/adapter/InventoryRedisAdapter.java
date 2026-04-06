package com.ryuqq.otatoy.persistence.redis.adapter;

import com.ryuqq.otatoy.application.inventory.port.out.redis.InventoryRedisPort;
import com.ryuqq.otatoy.domain.inventory.InventoryExhaustedException;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.persistence.redis.support.InventoryKeyResolver;
import com.ryuqq.otatoy.persistence.redis.support.InventoryLuaScriptHolder;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Inventory Redis Adapter.
 * Lua 스크립트로 다중 날짜 재고를 원자적으로 차감/복구/초기화한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class InventoryRedisAdapter implements InventoryRedisPort {

    private final RedissonClient redissonClient;
    private final InventoryLuaScriptHolder scriptHolder;

    public InventoryRedisAdapter(RedissonClient redissonClient,
                                  InventoryLuaScriptHolder scriptHolder) {
        this.redissonClient = redissonClient;
        this.scriptHolder = scriptHolder;
    }

    @Override
    public void decrementStock(RoomTypeId roomTypeId, List<LocalDate> dates) {
        List<String> keys = InventoryKeyResolver.resolveAll(roomTypeId, dates);

        Object result = redissonClient.getScript(StringCodec.INSTANCE)
            .eval(RScript.Mode.READ_WRITE,
                  scriptHolder.decrementScript(),
                  RScript.ReturnType.VALUE,
                  keys.stream().map(k -> (Object) k).toList());

        if ("-1".equals(String.valueOf(result))) {
            throw new InventoryExhaustedException();
        }
    }

    @Override
    public void incrementStock(RoomTypeId roomTypeId, List<LocalDate> dates) {
        List<String> keys = InventoryKeyResolver.resolveAll(roomTypeId, dates);

        redissonClient.getScript(StringCodec.INSTANCE)
            .eval(RScript.Mode.READ_WRITE,
                  scriptHolder.incrementScript(),
                  RScript.ReturnType.VALUE,
                  keys.stream().map(k -> (Object) k).toList());
    }

    @Override
    public void initializeStock(RoomTypeId roomTypeId, Map<LocalDate, Integer> dateStockMap) {
        List<LocalDate> sortedDates = dateStockMap.keySet().stream().sorted().toList();
        List<String> keys = InventoryKeyResolver.resolveAll(roomTypeId, sortedDates);
        Object[] args = sortedDates.stream()
            .map(date -> (Object) String.valueOf(dateStockMap.get(date)))
            .toArray();

        redissonClient.getScript(StringCodec.INSTANCE)
            .eval(RScript.Mode.READ_WRITE,
                  scriptHolder.initializeScript(),
                  RScript.ReturnType.VALUE,
                  keys.stream().map(k -> (Object) k).toList(),
                  args);
    }
}
