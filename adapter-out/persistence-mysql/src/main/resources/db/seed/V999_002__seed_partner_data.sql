-- =============================================================================
-- 파트너 + 브랜드 시드
-- =============================================================================

-- Partner
INSERT INTO partner (id, name, status, created_at, updated_at, deleted)
VALUES
    (1, '파트너A', 'ACTIVE', NOW(6), NOW(6), 0),
    (2, '파트너B', 'ACTIVE', NOW(6), NOW(6), 0);

-- Brand
INSERT INTO brand (id, name, name_kr, logo_url, created_at, updated_at, deleted)
VALUES
    (1, 'Grand Hotel Chain',  '그랜드 호텔 체인',   'https://example.com/logos/grand-hotel.png',  NOW(6), NOW(6), 0),
    (2, 'Ocean Resort Chain', '오션 리조트 체인',    'https://example.com/logos/ocean-resort.png', NOW(6), NOW(6), 0);
