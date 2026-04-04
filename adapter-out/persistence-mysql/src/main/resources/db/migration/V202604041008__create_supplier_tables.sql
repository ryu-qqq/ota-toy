-- Supplier
CREATE TABLE supplier (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(200) NOT NULL,
    name_kr       VARCHAR(200) NOT NULL,
    company_title VARCHAR(200) NOT NULL,
    owner_name    VARCHAR(100) NOT NULL,
    business_no   VARCHAR(50)  NOT NULL,
    address       VARCHAR(500) NULL,
    phone         VARCHAR(30)  NULL,
    email         VARCHAR(200) NULL,
    terms_url     VARCHAR(500) NULL,
    status        VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted       TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at    TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_supplier_status (status),
    INDEX idx_supplier_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- SupplierApiConfig (도메인 미구현 — 인프라 전용 테이블)
CREATE TABLE supplier_api_config (
    id                     BIGINT       NOT NULL AUTO_INCREMENT,
    supplier_id            BIGINT       NOT NULL,
    api_base_url           VARCHAR(500) NOT NULL,
    api_key                VARCHAR(500) NOT NULL,
    auth_type              VARCHAR(30)  NOT NULL,
    sync_interval_minutes  INT          NOT NULL DEFAULT 60,
    status                 VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at             TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at             TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted                TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at             TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_sac_supplier (supplier_id),
    INDEX idx_sac_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- SupplierProperty
CREATE TABLE supplier_property (
    id                      BIGINT       NOT NULL AUTO_INCREMENT,
    supplier_id             BIGINT       NOT NULL,
    property_id             BIGINT       NOT NULL,
    supplier_property_code  VARCHAR(100) NOT NULL,
    last_synced_at          TIMESTAMP(6) NULL,
    status                  VARCHAR(30)  NOT NULL DEFAULT 'MAPPED',
    created_at              TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted                 TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at              TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_sp_supplier (supplier_id),
    INDEX idx_sp_property (property_id),
    INDEX idx_sp_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- SupplierRoomType
CREATE TABLE supplier_room_type (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    supplier_property_id  BIGINT       NOT NULL,
    room_type_id          BIGINT       NOT NULL,
    supplier_room_code    VARCHAR(100) NOT NULL,
    last_synced_at        TIMESTAMP(6) NULL,
    status                VARCHAR(30)  NOT NULL DEFAULT 'MAPPED',
    created_at            TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at            TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted               TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at            TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_srt_supplier_property (supplier_property_id),
    INDEX idx_srt_room_type (room_type_id),
    INDEX idx_srt_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- SupplierSyncLog
CREATE TABLE supplier_sync_log (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    supplier_id   BIGINT       NOT NULL,
    sync_type     VARCHAR(30)  NOT NULL,
    synced_at     TIMESTAMP(6) NOT NULL,
    status        VARCHAR(30)  NOT NULL,
    total_count   INT          NOT NULL DEFAULT 0,
    created_count INT          NOT NULL DEFAULT 0,
    updated_count INT          NOT NULL DEFAULT 0,
    deleted_count INT          NOT NULL DEFAULT 0,
    error_message TEXT         NULL,
    created_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted       TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at    TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_ssl_supplier (supplier_id),
    INDEX idx_ssl_sync_type (sync_type),
    INDEX idx_ssl_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
