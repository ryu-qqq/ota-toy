-- Property
CREATE TABLE property (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    partner_id       BIGINT       NOT NULL,
    brand_id         BIGINT       NULL,
    property_type_id BIGINT       NOT NULL,
    name             VARCHAR(200) NOT NULL,
    description      TEXT         NULL,
    address          VARCHAR(500) NOT NULL,
    latitude         DOUBLE       NOT NULL DEFAULT 0,
    longitude        DOUBLE       NOT NULL DEFAULT 0,
    neighborhood     VARCHAR(100) NULL,
    region           VARCHAR(100) NULL,
    status           VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    promotion_text   VARCHAR(500) NULL,
    created_at       TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted          TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at       TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_property_partner (partner_id),
    INDEX idx_property_brand (brand_id),
    INDEX idx_property_type (property_type_id),
    INDEX idx_property_region_status (region, status),
    INDEX idx_property_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PropertyAttributeValue
CREATE TABLE property_attribute_value (
    id                         BIGINT       NOT NULL AUTO_INCREMENT,
    property_id                BIGINT       NOT NULL,
    property_type_attribute_id BIGINT       NOT NULL,
    value                      VARCHAR(500) NOT NULL,
    created_at                 TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at                 TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted                    TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at                 TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_pav_property (property_id),
    INDEX idx_pav_attribute (property_type_attribute_id),
    INDEX idx_pav_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PropertyAmenity
CREATE TABLE property_amenity (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    property_id      BIGINT        NOT NULL,
    amenity_type     VARCHAR(50)   NOT NULL,
    name             VARCHAR(200)  NOT NULL,
    additional_price DECIMAL(12,2) NOT NULL DEFAULT 0,
    sort_order       INT           NOT NULL DEFAULT 0,
    created_at       TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted          TINYINT(1)    NOT NULL DEFAULT 0,
    deleted_at       TIMESTAMP(6)  NULL,
    PRIMARY KEY (id),
    INDEX idx_pa_property (property_id),
    INDEX idx_pa_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PropertyPhoto
CREATE TABLE property_photo (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    property_id BIGINT        NOT NULL,
    photo_type  VARCHAR(50)   NOT NULL,
    origin_url  VARCHAR(1000) NOT NULL,
    cdn_url     VARCHAR(1000) NULL,
    sort_order  INT           NOT NULL DEFAULT 0,
    created_at  TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted     TINYINT(1)    NOT NULL DEFAULT 0,
    deleted_at  TIMESTAMP(6)  NULL,
    PRIMARY KEY (id),
    INDEX idx_pp_property (property_id),
    INDEX idx_pp_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
