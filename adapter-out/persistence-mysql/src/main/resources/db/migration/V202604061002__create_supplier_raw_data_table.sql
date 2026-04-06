-- SupplierRawData: 외부 공급자로부터 수집한 원시 데이터
CREATE TABLE supplier_raw_data (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    supplier_id   BIGINT       NOT NULL,
    raw_payload   LONGTEXT     NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'FETCHED',
    fetched_at    TIMESTAMP(6) NOT NULL,
    processed_at  TIMESTAMP(6) NULL,
    created_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_srd_status_supplier (status, supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
