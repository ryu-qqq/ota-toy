-- inventory_initialize.lua
-- 여러 날짜의 재고 카운터를 초기화한다.
-- 파트너가 재고를 설정할 때 호출.
--
-- KEYS: inventory:{roomTypeId}:{date1}, inventory:{roomTypeId}:{date2}, ...
-- ARGV: stock1, stock2, ... (각 키에 대응하는 재고 수량)
-- RETURN: 1 (항상 성공)

for i, key in ipairs(KEYS) do
    redis.call('SET', key, ARGV[i])
end

return 1
