-- =============================================================================
-- 객실 + 침대 + 전망 시드
-- 각 Property에 RoomType 2개씩 (총 6개)
-- =============================================================================

-- RoomType
-- 호텔 객실 (property_id=1)
INSERT INTO room_type (id, property_id, name, description, area_sqm, area_pyeong, base_occupancy, max_occupancy, base_inventory, check_in_time, check_out_time, status, created_at, updated_at, deleted)
VALUES
    (1, 1, '디럭스 더블',  '넓고 쾌적한 디럭스 객실입니다. 시티뷰를 감상할 수 있습니다.',  33.00, '10평', 2, 3, 5, '15:00', '11:00', 'ACTIVE', NOW(6), NOW(6), 0),
    (2, 1, '프리미엄 스위트', '최상의 편의를 제공하는 스위트 룸입니다. 별도의 거실 공간이 있습니다.', 66.00, '20평', 2, 4, 3, '15:00', '11:00', 'ACTIVE', NOW(6), NOW(6), 0);

-- 리조트 객실 (property_id=2)
INSERT INTO room_type (id, property_id, name, description, area_sqm, area_pyeong, base_occupancy, max_occupancy, base_inventory, check_in_time, check_out_time, status, created_at, updated_at, deleted)
VALUES
    (3, 2, '오션뷰 디럭스',  '해운대 해변이 정면으로 보이는 디럭스 객실입니다.',  40.00, '12평', 2, 3, 5, '15:00', '11:00', 'ACTIVE', NOW(6), NOW(6), 0),
    (4, 2, '오션뷰 스위트',  '넓은 발코니에서 오션뷰를 만끽할 수 있는 스위트 룸입니다.', 75.00, '23평', 2, 4, 3, '15:00', '11:00', 'ACTIVE', NOW(6), NOW(6), 0);

-- 펜션 객실 (property_id=3)
INSERT INTO room_type (id, property_id, name, description, area_sqm, area_pyeong, base_occupancy, max_occupancy, base_inventory, check_in_time, check_out_time, status, created_at, updated_at, deleted)
VALUES
    (5, 3, '스탠다드룸',    '아늑한 분위기의 스탠다드 객실입니다.',             26.00, '8평',  2, 2, 5, '15:00', '11:00', 'ACTIVE', NOW(6), NOW(6), 0),
    (6, 3, '패밀리룸',     '가족 단위 투숙에 적합한 넓은 객실입니다.',           50.00, '15평', 4, 6, 3, '15:00', '11:00', 'ACTIVE', NOW(6), NOW(6), 0);

-- RoomTypeBed
-- 호텔: 디럭스=퀸1, 스위트=킹1+싱글1
INSERT INTO room_type_bed (id, room_type_id, bed_type_id, quantity, created_at, updated_at, deleted)
VALUES
    (1, 1, 3, 1, NOW(6), NOW(6), 0),   -- 디럭스 더블 → 퀸 1
    (2, 2, 4, 1, NOW(6), NOW(6), 0),   -- 프리미엄 스위트 → 킹 1
    (3, 2, 1, 1, NOW(6), NOW(6), 0);   -- 프리미엄 스위트 → 싱글 1

-- 리조트: 디럭스=퀸1, 스위트=킹1+싱글1
INSERT INTO room_type_bed (id, room_type_id, bed_type_id, quantity, created_at, updated_at, deleted)
VALUES
    (4, 3, 3, 1, NOW(6), NOW(6), 0),   -- 오션뷰 디럭스 → 퀸 1
    (5, 4, 4, 1, NOW(6), NOW(6), 0),   -- 오션뷰 스위트 → 킹 1
    (6, 4, 1, 1, NOW(6), NOW(6), 0);   -- 오션뷰 스위트 → 싱글 1

-- 펜션: 스탠다드=더블1, 패밀리=퀸1+싱글2
INSERT INTO room_type_bed (id, room_type_id, bed_type_id, quantity, created_at, updated_at, deleted)
VALUES
    (7, 5, 2, 1, NOW(6), NOW(6), 0),   -- 스탠다드룸 → 더블 1
    (8, 6, 3, 1, NOW(6), NOW(6), 0),   -- 패밀리룸 → 퀸 1
    (9, 6, 1, 2, NOW(6), NOW(6), 0);   -- 패밀리룸 → 싱글 2

-- RoomTypeView
-- 호텔: 디럭스=시티뷰, 스위트=시티뷰
INSERT INTO room_type_view (id, room_type_id, view_type_id, created_at, updated_at, deleted)
VALUES
    (1, 1, 1, NOW(6), NOW(6), 0),   -- 디럭스 더블 → 시티뷰
    (2, 2, 1, NOW(6), NOW(6), 0);   -- 프리미엄 스위트 → 시티뷰

-- 리조트: 디럭스=오션뷰, 스위트=오션뷰
INSERT INTO room_type_view (id, room_type_id, view_type_id, created_at, updated_at, deleted)
VALUES
    (3, 3, 2, NOW(6), NOW(6), 0),   -- 오션뷰 디럭스 → 오션뷰
    (4, 4, 2, NOW(6), NOW(6), 0);   -- 오션뷰 스위트 → 오션뷰

-- 펜션: 스탠다드=가든뷰, 패밀리=마운틴뷰
INSERT INTO room_type_view (id, room_type_id, view_type_id, created_at, updated_at, deleted)
VALUES
    (5, 5, 4, NOW(6), NOW(6), 0),   -- 스탠다드룸 → 가든뷰
    (6, 6, 3, NOW(6), NOW(6), 0);   -- 패밀리룸 → 마운틴뷰
