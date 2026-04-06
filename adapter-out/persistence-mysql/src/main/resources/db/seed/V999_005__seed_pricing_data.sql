-- =============================================================================
-- 요금 + 재고 시드
-- 각 RoomType에 RatePlan 1개, RateRule 1개, Rate 7일치, Inventory 7일치
-- 날짜는 CURDATE() + INTERVAL 로 항상 미래 날짜 생성
-- =============================================================================

-- RatePlan (각 RoomType별 1개, source_type=DIRECT, payment_policy=PREPAY, 무료취소)
INSERT INTO rate_plan (id, room_type_id, name, source_type, supplier_id, is_free_cancellation, is_non_refundable, free_cancellation_deadline_days, cancellation_policy_text, payment_policy, created_at, updated_at, deleted)
VALUES
    (1, 1, '디럭스 더블 기본 요금제',      'DIRECT', NULL, 1, 0, 3, '체크인 3일 전까지 무료 취소 가능', 'PREPAY', NOW(6), NOW(6), 0),
    (2, 2, '프리미엄 스위트 기본 요금제',   'DIRECT', NULL, 1, 0, 3, '체크인 3일 전까지 무료 취소 가능', 'PREPAY', NOW(6), NOW(6), 0),
    (3, 3, '오션뷰 디럭스 기본 요금제',     'DIRECT', NULL, 1, 0, 3, '체크인 3일 전까지 무료 취소 가능', 'PREPAY', NOW(6), NOW(6), 0),
    (4, 4, '오션뷰 스위트 기본 요금제',     'DIRECT', NULL, 1, 0, 3, '체크인 3일 전까지 무료 취소 가능', 'PREPAY', NOW(6), NOW(6), 0),
    (5, 5, '스탠다드룸 기본 요금제',        'DIRECT', NULL, 1, 0, 3, '체크인 3일 전까지 무료 취소 가능', 'PREPAY', NOW(6), NOW(6), 0),
    (6, 6, '패밀리룸 기본 요금제',          'DIRECT', NULL, 1, 0, 3, '체크인 3일 전까지 무료 취소 가능', 'PREPAY', NOW(6), NOW(6), 0);

-- RateRule (각 RatePlan별 1개, 오늘~30일 후)
INSERT INTO rate_rule (id, rate_plan_id, start_date, end_date, base_price, weekday_price, friday_price, saturday_price, sunday_price, created_at, updated_at, deleted)
VALUES
    (1, 1, CURDATE(), CURDATE() + INTERVAL 30 DAY, 150000.00, 150000.00, 180000.00, 200000.00, 130000.00, NOW(6), NOW(6), 0),
    (2, 2, CURDATE(), CURDATE() + INTERVAL 30 DAY, 300000.00, 300000.00, 350000.00, 400000.00, 280000.00, NOW(6), NOW(6), 0),
    (3, 3, CURDATE(), CURDATE() + INTERVAL 30 DAY, 200000.00, 200000.00, 240000.00, 280000.00, 180000.00, NOW(6), NOW(6), 0),
    (4, 4, CURDATE(), CURDATE() + INTERVAL 30 DAY, 400000.00, 400000.00, 450000.00, 500000.00, 380000.00, NOW(6), NOW(6), 0),
    (5, 5, CURDATE(), CURDATE() + INTERVAL 30 DAY, 100000.00, 100000.00, 120000.00, 150000.00,  90000.00, NOW(6), NOW(6), 0),
    (6, 6, CURDATE(), CURDATE() + INTERVAL 30 DAY, 180000.00, 180000.00, 210000.00, 250000.00, 160000.00, NOW(6), NOW(6), 0);

-- Rate (각 RatePlan별 7일치, 내일~7일 후)
-- RatePlan 1: 디럭스 더블 (base_price=150000)
INSERT INTO rate (id, rate_plan_id, rate_date, base_price, created_at, updated_at, deleted)
VALUES
    (1,  1, CURDATE() + INTERVAL 1 DAY, 150000.00, NOW(6), NOW(6), 0),
    (2,  1, CURDATE() + INTERVAL 2 DAY, 150000.00, NOW(6), NOW(6), 0),
    (3,  1, CURDATE() + INTERVAL 3 DAY, 150000.00, NOW(6), NOW(6), 0),
    (4,  1, CURDATE() + INTERVAL 4 DAY, 150000.00, NOW(6), NOW(6), 0),
    (5,  1, CURDATE() + INTERVAL 5 DAY, 180000.00, NOW(6), NOW(6), 0),
    (6,  1, CURDATE() + INTERVAL 6 DAY, 200000.00, NOW(6), NOW(6), 0),
    (7,  1, CURDATE() + INTERVAL 7 DAY, 130000.00, NOW(6), NOW(6), 0);

-- RatePlan 2: 프리미엄 스위트 (base_price=300000)
INSERT INTO rate (id, rate_plan_id, rate_date, base_price, created_at, updated_at, deleted)
VALUES
    (8,  2, CURDATE() + INTERVAL 1 DAY, 300000.00, NOW(6), NOW(6), 0),
    (9,  2, CURDATE() + INTERVAL 2 DAY, 300000.00, NOW(6), NOW(6), 0),
    (10, 2, CURDATE() + INTERVAL 3 DAY, 300000.00, NOW(6), NOW(6), 0),
    (11, 2, CURDATE() + INTERVAL 4 DAY, 300000.00, NOW(6), NOW(6), 0),
    (12, 2, CURDATE() + INTERVAL 5 DAY, 350000.00, NOW(6), NOW(6), 0),
    (13, 2, CURDATE() + INTERVAL 6 DAY, 400000.00, NOW(6), NOW(6), 0),
    (14, 2, CURDATE() + INTERVAL 7 DAY, 280000.00, NOW(6), NOW(6), 0);

-- RatePlan 3: 오션뷰 디럭스 (base_price=200000)
INSERT INTO rate (id, rate_plan_id, rate_date, base_price, created_at, updated_at, deleted)
VALUES
    (15, 3, CURDATE() + INTERVAL 1 DAY, 200000.00, NOW(6), NOW(6), 0),
    (16, 3, CURDATE() + INTERVAL 2 DAY, 200000.00, NOW(6), NOW(6), 0),
    (17, 3, CURDATE() + INTERVAL 3 DAY, 200000.00, NOW(6), NOW(6), 0),
    (18, 3, CURDATE() + INTERVAL 4 DAY, 200000.00, NOW(6), NOW(6), 0),
    (19, 3, CURDATE() + INTERVAL 5 DAY, 240000.00, NOW(6), NOW(6), 0),
    (20, 3, CURDATE() + INTERVAL 6 DAY, 280000.00, NOW(6), NOW(6), 0),
    (21, 3, CURDATE() + INTERVAL 7 DAY, 180000.00, NOW(6), NOW(6), 0);

-- RatePlan 4: 오션뷰 스위트 (base_price=400000)
INSERT INTO rate (id, rate_plan_id, rate_date, base_price, created_at, updated_at, deleted)
VALUES
    (22, 4, CURDATE() + INTERVAL 1 DAY, 400000.00, NOW(6), NOW(6), 0),
    (23, 4, CURDATE() + INTERVAL 2 DAY, 400000.00, NOW(6), NOW(6), 0),
    (24, 4, CURDATE() + INTERVAL 3 DAY, 400000.00, NOW(6), NOW(6), 0),
    (25, 4, CURDATE() + INTERVAL 4 DAY, 400000.00, NOW(6), NOW(6), 0),
    (26, 4, CURDATE() + INTERVAL 5 DAY, 450000.00, NOW(6), NOW(6), 0),
    (27, 4, CURDATE() + INTERVAL 6 DAY, 500000.00, NOW(6), NOW(6), 0),
    (28, 4, CURDATE() + INTERVAL 7 DAY, 380000.00, NOW(6), NOW(6), 0);

-- RatePlan 5: 스탠다드룸 (base_price=100000)
INSERT INTO rate (id, rate_plan_id, rate_date, base_price, created_at, updated_at, deleted)
VALUES
    (29, 5, CURDATE() + INTERVAL 1 DAY, 100000.00, NOW(6), NOW(6), 0),
    (30, 5, CURDATE() + INTERVAL 2 DAY, 100000.00, NOW(6), NOW(6), 0),
    (31, 5, CURDATE() + INTERVAL 3 DAY, 100000.00, NOW(6), NOW(6), 0),
    (32, 5, CURDATE() + INTERVAL 4 DAY, 100000.00, NOW(6), NOW(6), 0),
    (33, 5, CURDATE() + INTERVAL 5 DAY, 120000.00, NOW(6), NOW(6), 0),
    (34, 5, CURDATE() + INTERVAL 6 DAY, 150000.00, NOW(6), NOW(6), 0),
    (35, 5, CURDATE() + INTERVAL 7 DAY,  90000.00, NOW(6), NOW(6), 0);

-- RatePlan 6: 패밀리룸 (base_price=180000)
INSERT INTO rate (id, rate_plan_id, rate_date, base_price, created_at, updated_at, deleted)
VALUES
    (36, 6, CURDATE() + INTERVAL 1 DAY, 180000.00, NOW(6), NOW(6), 0),
    (37, 6, CURDATE() + INTERVAL 2 DAY, 180000.00, NOW(6), NOW(6), 0),
    (38, 6, CURDATE() + INTERVAL 3 DAY, 180000.00, NOW(6), NOW(6), 0),
    (39, 6, CURDATE() + INTERVAL 4 DAY, 180000.00, NOW(6), NOW(6), 0),
    (40, 6, CURDATE() + INTERVAL 5 DAY, 210000.00, NOW(6), NOW(6), 0),
    (41, 6, CURDATE() + INTERVAL 6 DAY, 250000.00, NOW(6), NOW(6), 0),
    (42, 6, CURDATE() + INTERVAL 7 DAY, 160000.00, NOW(6), NOW(6), 0);

-- Inventory (각 RoomType별 7일치, available_count=5, total_inventory=5)
-- RoomType 1: 디럭스 더블
INSERT INTO inventory (id, room_type_id, inventory_date, total_inventory, available_count, is_stop_sell, version, created_at, updated_at, deleted)
VALUES
    (1,  1, CURDATE() + INTERVAL 1 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (2,  1, CURDATE() + INTERVAL 2 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (3,  1, CURDATE() + INTERVAL 3 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (4,  1, CURDATE() + INTERVAL 4 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (5,  1, CURDATE() + INTERVAL 5 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (6,  1, CURDATE() + INTERVAL 6 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (7,  1, CURDATE() + INTERVAL 7 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0);

-- RoomType 2: 프리미엄 스위트
INSERT INTO inventory (id, room_type_id, inventory_date, total_inventory, available_count, is_stop_sell, version, created_at, updated_at, deleted)
VALUES
    (8,  2, CURDATE() + INTERVAL 1 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (9,  2, CURDATE() + INTERVAL 2 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (10, 2, CURDATE() + INTERVAL 3 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (11, 2, CURDATE() + INTERVAL 4 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (12, 2, CURDATE() + INTERVAL 5 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (13, 2, CURDATE() + INTERVAL 6 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (14, 2, CURDATE() + INTERVAL 7 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0);

-- RoomType 3: 오션뷰 디럭스
INSERT INTO inventory (id, room_type_id, inventory_date, total_inventory, available_count, is_stop_sell, version, created_at, updated_at, deleted)
VALUES
    (15, 3, CURDATE() + INTERVAL 1 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (16, 3, CURDATE() + INTERVAL 2 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (17, 3, CURDATE() + INTERVAL 3 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (18, 3, CURDATE() + INTERVAL 4 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (19, 3, CURDATE() + INTERVAL 5 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (20, 3, CURDATE() + INTERVAL 6 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (21, 3, CURDATE() + INTERVAL 7 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0);

-- RoomType 4: 오션뷰 스위트
INSERT INTO inventory (id, room_type_id, inventory_date, total_inventory, available_count, is_stop_sell, version, created_at, updated_at, deleted)
VALUES
    (22, 4, CURDATE() + INTERVAL 1 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (23, 4, CURDATE() + INTERVAL 2 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (24, 4, CURDATE() + INTERVAL 3 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (25, 4, CURDATE() + INTERVAL 4 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (26, 4, CURDATE() + INTERVAL 5 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (27, 4, CURDATE() + INTERVAL 6 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (28, 4, CURDATE() + INTERVAL 7 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0);

-- RoomType 5: 스탠다드룸
INSERT INTO inventory (id, room_type_id, inventory_date, total_inventory, available_count, is_stop_sell, version, created_at, updated_at, deleted)
VALUES
    (29, 5, CURDATE() + INTERVAL 1 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (30, 5, CURDATE() + INTERVAL 2 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (31, 5, CURDATE() + INTERVAL 3 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (32, 5, CURDATE() + INTERVAL 4 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (33, 5, CURDATE() + INTERVAL 5 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (34, 5, CURDATE() + INTERVAL 6 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (35, 5, CURDATE() + INTERVAL 7 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0);

-- RoomType 6: 패밀리룸
INSERT INTO inventory (id, room_type_id, inventory_date, total_inventory, available_count, is_stop_sell, version, created_at, updated_at, deleted)
VALUES
    (36, 6, CURDATE() + INTERVAL 1 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (37, 6, CURDATE() + INTERVAL 2 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (38, 6, CURDATE() + INTERVAL 3 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (39, 6, CURDATE() + INTERVAL 4 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (40, 6, CURDATE() + INTERVAL 5 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (41, 6, CURDATE() + INTERVAL 6 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0),
    (42, 6, CURDATE() + INTERVAL 7 DAY, 5, 5, 0, 0, NOW(6), NOW(6), 0);
