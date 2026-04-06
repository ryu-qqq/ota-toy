#!/bin/bash
# OTA 극한 성능 테스트 스크립트
# 사용법: bash infra/local-dev/load-test.sh
# Grafana(localhost:3000)에서 실시간 모니터링하면서 실행

CUSTOMER_URL="http://localhost:8081"
EXTRANET_URL="http://localhost:8080"
RATE_URL="$CUSTOMER_URL/api/v1/properties/1/rates?checkIn=2026-04-10&checkOut=2026-04-12&guests=2"
SESSION_URL="$CUSTOMER_URL/api/v1/reservation-sessions"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

timestamp() { date "+%H:%M:%S"; }

print_header() {
    echo ""
    echo -e "${CYAN}=================================================================${NC}"
    echo -e "${CYAN}  $(timestamp) | $1${NC}"
    echo -e "${CYAN}=================================================================${NC}"
}

print_metric() {
    local hit=$(curl -s "$CUSTOMER_URL/actuator/metrics/rate.cache.hit" 2>/dev/null | python3 -c "import sys,json; print(int(json.load(sys.stdin)['measurements'][0]['value']))" 2>/dev/null || echo "N/A")
    local miss=$(curl -s "$CUSTOMER_URL/actuator/metrics/rate.cache.miss" 2>/dev/null | python3 -c "import sys,json; print(int(json.load(sys.stdin)['measurements'][0]['value']))" 2>/dev/null || echo "N/A")
    local success=$(curl -s "$CUSTOMER_URL/actuator/metrics/inventory.decrement.success" 2>/dev/null | python3 -c "import sys,json; print(int(json.load(sys.stdin)['measurements'][0]['value']))" 2>/dev/null || echo "0")
    local fail=$(curl -s "$CUSTOMER_URL/actuator/metrics/inventory.decrement.failure" 2>/dev/null | python3 -c "import sys,json; print(int(json.load(sys.stdin)['measurements'][0]['value']))" 2>/dev/null || echo "0")

    if [ "$hit" != "N/A" ] && [ "$miss" != "N/A" ]; then
        local total=$((hit + miss))
        if [ "$total" -gt 0 ]; then
            local rate=$(python3 -c "print(f'{$hit/$total*100:.1f}')")
            echo -e "  ${GREEN}캐시 히트율: ${rate}%${NC} (히트: $hit / 미스: $miss)"
        fi
    fi
    echo -e "  ${YELLOW}재고 차감: 성공 $success / 실패 $fail${NC}"
}

reset_inventory() {
    local count=$1
    docker exec otatoy-mysql mysql -uroot -proot ota -e "UPDATE inventory SET available_count = $count WHERE room_type_id = 1;" 2>/dev/null
    docker exec otatoy-redis redis-cli SET "inventory:1:2026-04-10" "$count" > /dev/null 2>&1
    docker exec otatoy-redis redis-cli SET "inventory:1:2026-04-11" "$count" > /dev/null 2>&1
    echo -e "  재고 리셋: ${count}개"
}

flush_redis_cache() {
    docker exec otatoy-redis redis-cli KEYS "rate:*" 2>/dev/null | while read key; do
        docker exec otatoy-redis redis-cli DEL "$key" > /dev/null 2>&1
    done
    echo -e "  Redis 요금 캐시 삭제 완료"
}

concurrent_rate_query() {
    local count=$1
    local start_time=$(python3 -c "import time; print(time.time())")

    for i in $(seq 1 $count); do
        curl -s -o /dev/null -w "%{http_code}" "$RATE_URL" &
    done
    wait

    local end_time=$(python3 -c "import time; print(time.time())")
    local duration=$(python3 -c "print(f'{($end_time - $start_time)*1000:.0f}')")
    local rps=$(python3 -c "print(f'{$count/($end_time - $start_time):.0f}')")
    echo -e "  ${GREEN}${count}건 완료: ${duration}ms (${rps} RPS)${NC}"
}

concurrent_reservation() {
    local count=$1
    local success=0
    local fail=0
    local start_time=$(python3 -c "import time; print(time.time())")

    for i in $(seq 1 $count); do
        result=$(curl -s -o /dev/null -w "%{http_code}" \
            -X POST "$SESSION_URL" \
            -H "Content-Type: application/json" \
            -H "Idempotency-Key: load-$(uuidgen)" \
            -d "{
                \"propertyId\": 1,
                \"roomTypeId\": 1,
                \"ratePlanId\": 1,
                \"checkIn\": \"2026-04-10\",
                \"checkOut\": \"2026-04-12\",
                \"guestCount\": 2,
                \"totalAmount\": 100000
            }" &)
    done
    wait

    local end_time=$(python3 -c "import time; print(time.time())")
    local duration=$(python3 -c "print(f'{($end_time - $start_time)*1000:.0f}')")
    echo -e "  ${GREEN}${count}건 완료: ${duration}ms${NC}"
}

# ================================================================
echo -e "${RED}"
echo "  ╔═══════════════════════════════════════════╗"
echo "  ║     OTA 극한 성능 테스트 시작              ║"
echo "  ║     Grafana: http://localhost:3000         ║"
echo "  ╚═══════════════════════════════════════════╝"
echo -e "${NC}"

# ================================================================
print_header "시나리오 1: 요금 캐시 워밍 (콜드 → 웜)"
echo "  캐시 비우고 100건 요청 → 캐시 적재 후 100건 재요청"
flush_redis_cache
sleep 1

echo -e "\n  [1차: 캐시 콜드]"
concurrent_rate_query 100

echo -e "\n  [2차: 캐시 웜]"
concurrent_rate_query 100

print_metric
echo -e "\n  ${YELLOW}>> Grafana에서 캐시 히트/미스 Rate 그래프 확인 (10초 대기)${NC}"
sleep 10

# ================================================================
print_header "시나리오 2: 동시 500건 요금 조회 (캐시 웜 상태)"
echo "  캐시가 적재된 상태에서 500건 동시 요청 → DB 접근 0"
concurrent_rate_query 500
print_metric
echo -e "\n  ${YELLOW}>> Grafana에서 HTTP 요청률 스파이크 확인 (10초 대기)${NC}"
sleep 10

# ================================================================
print_header "시나리오 3: 캐시 스탬피드 시뮬레이션"
echo "  캐시 전체 삭제 → 즉시 300건 동시 요청 (전부 캐시 미스)"
flush_redis_cache
sleep 1
concurrent_rate_query 300
print_metric
echo -e "\n  ${YELLOW}>> Grafana에서 캐시 미스 스파이크 + DB 커넥션 사용량 확인 (10초 대기)${NC}"
sleep 10

# ================================================================
print_header "시나리오 4: 재고 50개 / 동시 200건 예약"
echo "  정확히 50건만 성공해야 함"
reset_inventory 50
sleep 1
concurrent_reservation 200
print_metric
echo -e "\n  ${YELLOW}>> Grafana에서 재고 차감 성공/실패 카운터 확인 (10초 대기)${NC}"
sleep 10

# ================================================================
print_header "시나리오 5: 재고 5개 / 동시 1000건 예약 (극한)"
echo "  1000건 중 5건만 성공해야 함. Virtual Thread 없이 curl 1000건"
reset_inventory 5
sleep 1
concurrent_reservation 1000
print_metric
echo -e "\n  ${YELLOW}>> Grafana에서 대량 실패 스파이크 확인 (10초 대기)${NC}"
sleep 10

# ================================================================
print_header "시나리오 6: 혼합 부하 (요금 조회 + 예약 동시)"
echo "  요금 조회 300건 + 예약 100건 동시 실행"
reset_inventory 20
sleep 1

echo -e "\n  [요금 조회 300건 + 예약 100건 동시 출발]"
for i in $(seq 1 300); do
    curl -s -o /dev/null "$RATE_URL" &
done
for i in $(seq 1 100); do
    curl -s -o /dev/null -w "" \
        -X POST "$SESSION_URL" \
        -H "Content-Type: application/json" \
        -H "Idempotency-Key: mixed-$(uuidgen)" \
        -d "{
            \"propertyId\": 1,
            \"roomTypeId\": 1,
            \"ratePlanId\": 1,
            \"checkIn\": \"2026-04-10\",
            \"checkOut\": \"2026-04-12\",
            \"guestCount\": 2,
            \"totalAmount\": 100000
        }" &
done
wait
echo -e "  ${GREEN}혼합 부하 400건 완료${NC}"
print_metric
echo -e "\n  ${YELLOW}>> Grafana에서 혼합 부하 패턴 확인 (10초 대기)${NC}"
sleep 10

# ================================================================
print_header "시나리오 7: 지속 부하 (30초간 연속)"
echo "  30초 동안 초당 ~20건 요금 조회 → 안정성 확인"
END=$((SECONDS + 30))
COUNT=0
while [ $SECONDS -lt $END ]; do
    for i in $(seq 1 20); do
        curl -s -o /dev/null "$RATE_URL" &
    done
    wait
    COUNT=$((COUNT + 20))
    echo -ne "\r  진행: ${COUNT}건 (남은 시간: $((END - SECONDS))초)  "
done
echo ""
echo -e "  ${GREEN}지속 부하 ${COUNT}건 완료${NC}"
print_metric

# ================================================================
print_header "최종 결과 요약"
print_metric

echo ""
echo -e "${RED}"
echo "  ╔═══════════════════════════════════════════╗"
echo "  ║     성능 테스트 완료!                      ║"
echo "  ║     Grafana에서 전체 추이를 확인하세요      ║"
echo "  ╚═══════════════════════════════════════════╝"
echo -e "${NC}"
