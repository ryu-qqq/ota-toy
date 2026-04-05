-- inventory 테이블에 total_inventory 컬럼 추가.
-- 도메인 모델(Inventory)의 totalInventory 필드를 매핑하기 위해 필요.
-- 전체 객실 수량을 관리하며, available_count와 함께 예약 현황을 추적한다.
ALTER TABLE inventory
    ADD COLUMN total_inventory INT NOT NULL DEFAULT 0 AFTER inventory_date;
