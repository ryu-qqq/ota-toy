-- =============================================================================
-- 랜드마크 + PropertyLandmark 매핑 시드
-- =============================================================================

-- Landmark
INSERT INTO landmark (id, name, landmark_type, latitude, longitude, created_at, updated_at, deleted)
VALUES
    (1, '강남역',          'STATION', 37.4979, 127.0276, NOW(6), NOW(6), 0),
    (2, '해운대해수욕장',   'TOURIST', 35.1587, 129.1604, NOW(6), NOW(6), 0),
    (3, '제주국제공항',     'AIRPORT', 33.5104, 126.4914, NOW(6), NOW(6), 0);

-- PropertyLandmark
INSERT INTO property_landmark (id, property_id, landmark_id, distance_km, walking_minutes, created_at, updated_at, deleted)
VALUES
    (1, 1, 1, 0.5,  7,  NOW(6), NOW(6), 0),   -- 호텔 → 강남역 (도보 7분)
    (2, 2, 2, 0.3,  5,  NOW(6), NOW(6), 0),   -- 리조트 → 해운대해수욕장 (도보 5분)
    (3, 3, 3, 15.0, 0,  NOW(6), NOW(6), 0);   -- 펜션 → 제주공항 (차량 이동)
