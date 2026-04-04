-- Reservation
CREATE TABLE reservation (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    rate_plan_id     BIGINT        NOT NULL,
    reservation_no   VARCHAR(50)   NOT NULL,
    guest_name       VARCHAR(100)  NOT NULL,
    guest_phone      VARCHAR(30)   NOT NULL,
    guest_email      VARCHAR(200)  NULL,
    check_in_date    DATE          NOT NULL,
    check_out_date   DATE          NOT NULL,
    guest_count      INT           NOT NULL DEFAULT 1,
    total_amount     DECIMAL(12,2) NOT NULL DEFAULT 0,
    status           VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    cancel_reason    VARCHAR(500)  NULL,
    booking_snapshot JSON          NULL,
    cancelled_at     TIMESTAMP(6)  NULL,
    created_at       TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted          TINYINT(1)    NOT NULL DEFAULT 0,
    deleted_at       TIMESTAMP(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_reservation_no (reservation_no),
    INDEX idx_rsv_rate_plan (rate_plan_id),
    INDEX idx_rsv_status (status),
    INDEX idx_rsv_check_in (check_in_date),
    INDEX idx_rsv_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ReservationItem
CREATE TABLE reservation_item (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT       NOT NULL,
    inventory_id   BIGINT       NOT NULL,
    stay_date      DATE         NOT NULL,
    created_at     TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted        TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at     TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_ri_reservation (reservation_id),
    INDEX idx_ri_inventory (inventory_id),
    INDEX idx_ri_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
