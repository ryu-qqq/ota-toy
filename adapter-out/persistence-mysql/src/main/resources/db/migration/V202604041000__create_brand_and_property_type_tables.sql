-- Brand
CREATE TABLE brand (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    name_kr     VARCHAR(100) NULL,
    logo_url    VARCHAR(500) NULL,
    created_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted     TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at  TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_brand_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PropertyType
CREATE TABLE property_type (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    created_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted     TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at  TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_property_type_code (code),
    INDEX idx_property_type_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PropertyTypeAttribute
CREATE TABLE property_type_attribute (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    property_type_id  BIGINT       NOT NULL,
    attribute_key     VARCHAR(100) NOT NULL,
    attribute_name    VARCHAR(200) NOT NULL,
    value_type        VARCHAR(50)  NOT NULL,
    is_required       TINYINT(1)   NOT NULL DEFAULT 0,
    sort_order        INT          NOT NULL DEFAULT 0,
    created_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted           TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at        TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_pta_property_type (property_type_id),
    INDEX idx_pta_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
