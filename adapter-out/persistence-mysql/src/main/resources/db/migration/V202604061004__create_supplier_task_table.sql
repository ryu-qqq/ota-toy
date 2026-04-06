-- SupplierTask (Outbox 역할 — 스케줄러가 직접 소비)
CREATE TABLE supplier_task (
    id                     BIGINT       NOT NULL AUTO_INCREMENT,
    supplier_id            BIGINT       NOT NULL,
    supplier_api_config_id BIGINT       NOT NULL,
    task_type              VARCHAR(30)  NOT NULL,
    status                 VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    payload                TEXT         NULL,
    retry_count            INT          NOT NULL DEFAULT 0,
    max_retries            INT          NOT NULL DEFAULT 3,
    failure_reason         TEXT         NULL,
    processed_at           TIMESTAMP(6) NULL,
    created_at             TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at             TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_st_supplier (supplier_id),
    INDEX idx_st_status (status),
    INDEX idx_st_status_retry (status, retry_count, max_retries)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
