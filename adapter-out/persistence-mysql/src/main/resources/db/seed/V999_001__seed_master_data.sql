-- =============================================================================
-- 마스터 데이터 시드
-- BedType, ViewType, PropertyType, PropertyTypeAttribute
-- =============================================================================

-- BedType
INSERT INTO bed_type (id, code, name, created_at, updated_at, deleted)
VALUES
    (1, 'SINGLE',   '싱글',   NOW(6), NOW(6), 0),
    (2, 'DOUBLE',   '더블',   NOW(6), NOW(6), 0),
    (3, 'QUEEN',    '퀸',     NOW(6), NOW(6), 0),
    (4, 'KING',     '킹',     NOW(6), NOW(6), 0);

-- ViewType
INSERT INTO view_type (id, code, name, created_at, updated_at, deleted)
VALUES
    (1, 'CITY',     '시티뷰',     NOW(6), NOW(6), 0),
    (2, 'OCEAN',    '오션뷰',     NOW(6), NOW(6), 0),
    (3, 'MOUNTAIN', '마운틴뷰',   NOW(6), NOW(6), 0),
    (4, 'GARDEN',   '가든뷰',     NOW(6), NOW(6), 0);

-- PropertyType
INSERT INTO property_type (id, code, name, description, created_at, updated_at, deleted)
VALUES
    (1, 'HOTEL',  '호텔',   '일반 호텔 숙박 시설',   NOW(6), NOW(6), 0),
    (2, 'MOTEL',  '모텔',   '모텔 숙박 시설',         NOW(6), NOW(6), 0),
    (3, 'PENSION','펜션',   '펜션 숙박 시설',         NOW(6), NOW(6), 0),
    (4, 'RESORT', '리조트', '리조트 숙박 시설',       NOW(6), NOW(6), 0);

-- PropertyTypeAttribute (호텔에만 성급 속성)
INSERT INTO property_type_attribute (id, property_type_id, attribute_key, attribute_name, value_type, is_required, sort_order, created_at, updated_at, deleted)
VALUES
    (1, 1, 'star_rating', '성급', 'STRING', 1, 1, NOW(6), NOW(6), 0);
