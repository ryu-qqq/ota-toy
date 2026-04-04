-- ReservationOutbox
CREATE TABLE reservation_outbox (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT       NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    payload        JSON         NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count    INT          NOT NULL DEFAULT 0,
    created_at     TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    processed_at   TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_ro_status_created (status, created_at),
    INDEX idx_ro_reservation (reservation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- SupplierOutbox
CREATE TABLE supplier_outbox (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    supplier_id  BIGINT       NOT NULL,
    event_type   VARCHAR(100) NOT NULL,
    payload      JSON         NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count  INT          NOT NULL DEFAULT 0,
    created_at   TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    processed_at TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_so_status_created (status, created_at),
    INDEX idx_so_supplier (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
