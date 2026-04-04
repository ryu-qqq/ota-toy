-- Inventory
CREATE TABLE inventory (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    room_type_id    BIGINT       NOT NULL,
    inventory_date  DATE         NOT NULL,
    available_count INT          NOT NULL DEFAULT 0,
    is_stop_sell    TINYINT(1)   NOT NULL DEFAULT 0,
    version         INT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted         TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at      TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_inventory_room_date (room_type_id, inventory_date),
    INDEX idx_inventory_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
