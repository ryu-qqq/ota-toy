-- =============================================================================
-- 숙소 + 사진 + 편의시설 + 속성값 시드
-- =============================================================================

-- Property
INSERT INTO property (id, partner_id, brand_id, property_type_id, name, description, address, latitude, longitude, neighborhood, region, status, promotion_text, created_at, updated_at, deleted)
VALUES
    (1, 1, 1, 1, '서울 강남 그랜드 호텔', '강남 중심부에 위치한 5성급 럭셔리 호텔입니다. 비즈니스와 레저 모두에 최적화된 시설을 갖추고 있습니다.', '서울특별시 강남구 테헤란로 152', 37.5000, 127.0366, '강남', '서울', 'ACTIVE', '오픈 기념 20% 할인', NOW(6), NOW(6), 0),
    (2, 1, 2, 4, '부산 해운대 오션 리조트', '해운대 해변이 한눈에 보이는 프리미엄 리조트입니다. 인피니티 풀과 스파를 즐겨보세요.', '부산광역시 해운대구 해운대해변로 264', 35.1587, 129.1604, '해운대', '부산', 'ACTIVE', '얼리버드 15% 할인', NOW(6), NOW(6), 0),
    (3, 2, NULL, 3, '제주 서귀포 펜션', '서귀포 바다가 보이는 조용한 펜션입니다. 가족 여행에 적합합니다.', '제주특별자치도 서귀포시 중문관광로 72번길 35', 33.2541, 126.4100, '서귀포', '제주', 'ACTIVE', NULL, NOW(6), NOW(6), 0);

-- PropertyPhoto
-- 호텔 사진
INSERT INTO property_photo (id, property_id, photo_type, origin_url, cdn_url, sort_order, created_at, updated_at, deleted)
VALUES
    (1, 1, 'EXTERIOR',   'https://example.com/photos/hotel/exterior.jpg',   'https://cdn.example.com/photos/hotel/exterior.jpg',   1, NOW(6), NOW(6), 0),
    (2, 1, 'LOBBY',      'https://example.com/photos/hotel/lobby.jpg',      'https://cdn.example.com/photos/hotel/lobby.jpg',      2, NOW(6), NOW(6), 0),
    (3, 1, 'RESTAURANT', 'https://example.com/photos/hotel/restaurant.jpg', 'https://cdn.example.com/photos/hotel/restaurant.jpg', 3, NOW(6), NOW(6), 0);

-- 리조트 사진
INSERT INTO property_photo (id, property_id, photo_type, origin_url, cdn_url, sort_order, created_at, updated_at, deleted)
VALUES
    (4, 2, 'EXTERIOR', 'https://example.com/photos/resort/exterior.jpg', 'https://cdn.example.com/photos/resort/exterior.jpg', 1, NOW(6), NOW(6), 0),
    (5, 2, 'POOL',     'https://example.com/photos/resort/pool.jpg',     'https://cdn.example.com/photos/resort/pool.jpg',     2, NOW(6), NOW(6), 0),
    (6, 2, 'ROOM',     'https://example.com/photos/resort/room.jpg',     'https://cdn.example.com/photos/resort/room.jpg',     3, NOW(6), NOW(6), 0);

-- 펜션 사진
INSERT INTO property_photo (id, property_id, photo_type, origin_url, cdn_url, sort_order, created_at, updated_at, deleted)
VALUES
    (7, 3, 'EXTERIOR', 'https://example.com/photos/pension/exterior.jpg', 'https://cdn.example.com/photos/pension/exterior.jpg', 1, NOW(6), NOW(6), 0),
    (8, 3, 'VIEW',     'https://example.com/photos/pension/view.jpg',     'https://cdn.example.com/photos/pension/view.jpg',     2, NOW(6), NOW(6), 0);

-- PropertyAmenity
-- 호텔 편의시설
INSERT INTO property_amenity (id, property_id, amenity_type, name, additional_price, sort_order, created_at, updated_at, deleted)
VALUES
    (1, 1, 'WIFI',           '무료 와이파이',    0.00, 1, NOW(6), NOW(6), 0),
    (2, 1, 'PARKING',        '지하 주차장',      0.00, 2, NOW(6), NOW(6), 0),
    (3, 1, 'FITNESS',        '피트니스 센터',    0.00, 3, NOW(6), NOW(6), 0),
    (4, 1, 'RESTAURANT',     '뷔페 레스토랑',    0.00, 4, NOW(6), NOW(6), 0),
    (5, 1, 'FRONT_DESK_24H', '24시간 프런트',    0.00, 5, NOW(6), NOW(6), 0);

-- 리조트 편의시설
INSERT INTO property_amenity (id, property_id, amenity_type, name, additional_price, sort_order, created_at, updated_at, deleted)
VALUES
    (6,  2, 'WIFI',       '무료 와이파이',     0.00, 1, NOW(6), NOW(6), 0),
    (7,  2, 'PARKING',    '야외 주차장',       0.00, 2, NOW(6), NOW(6), 0),
    (8,  2, 'POOL',       '인피니티 풀',       0.00, 3, NOW(6), NOW(6), 0),
    (9,  2, 'FITNESS',    '피트니스 센터',     0.00, 4, NOW(6), NOW(6), 0),
    (10, 2, 'ROOM_SERVICE','룸서비스',         0.00, 5, NOW(6), NOW(6), 0);

-- 펜션 편의시설
INSERT INTO property_amenity (id, property_id, amenity_type, name, additional_price, sort_order, created_at, updated_at, deleted)
VALUES
    (11, 3, 'WIFI',    '무료 와이파이',   0.00, 1, NOW(6), NOW(6), 0),
    (12, 3, 'PARKING', '무료 주차',       0.00, 2, NOW(6), NOW(6), 0),
    (13, 3, 'KITCHEN', '공용 주방',       0.00, 3, NOW(6), NOW(6), 0);

-- PropertyAttributeValue (호텔 성급)
INSERT INTO property_attribute_value (id, property_id, property_type_attribute_id, value, created_at, updated_at, deleted)
VALUES
    (1, 1, 1, '5', NOW(6), NOW(6), 0);
