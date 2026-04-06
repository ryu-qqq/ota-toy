-- inventory_decrement.lua
-- 여러 날짜의 재고를 원자적으로 차감한다.
-- 하나라도 0 미만이면 전부 복구 후 실패(-1) 반환.
--
-- KEYS: inventory:{roomTypeId}:{date1}, inventory:{roomTypeId}:{date2}, ...
-- ARGV: (없음)
-- RETURN: 1 (성공), -1 (재고 소진)

local results = {}

-- 1. 전부 DECR
for i, key in ipairs(KEYS) do
    results[i] = redis.call('DECRBY', key, 1)
end

-- 2. 하나라도 0 미만이면 전부 복구
for i, val in ipairs(results) do
    if tonumber(val) < 0 then
        for j = 1, #KEYS do
            redis.call('INCRBY', KEYS[j], 1)
        end
        return -1
    end
end

return 1
