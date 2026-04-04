-- Partner
CREATE TABLE partner (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(200) NOT NULL,
    status     VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted    TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_partner_status (status),
    INDEX idx_partner_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PartnerMember
CREATE TABLE partner_member (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    partner_id BIGINT       NOT NULL,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(200) NOT NULL,
    phone      VARCHAR(30)  NULL,
    role       VARCHAR(30)  NOT NULL,
    status     VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted    TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_pm_partner (partner_id),
    INDEX idx_pm_email (email),
    INDEX idx_pm_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
