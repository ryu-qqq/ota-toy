-- inventory_increment.lua
-- 여러 날짜의 재고를 원자적으로 복구한다.
-- 예약 취소 또는 실패 시 보상 용도.
--
-- KEYS: inventory:{roomTypeId}:{date1}, inventory:{roomTypeId}:{date2}, ...
-- ARGV: (없음)
-- RETURN: 1 (항상 성공)

for i, key in ipairs(KEYS) do
    redis.call('INCRBY', key, 1)
end

return 1
