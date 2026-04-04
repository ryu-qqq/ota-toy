-- BedType (lookup)
CREATE TABLE bed_type (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    code       VARCHAR(50)  NOT NULL,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted    TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_bed_type_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ViewType (lookup)
CREATE TABLE view_type (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    code       VARCHAR(50)  NOT NULL,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted    TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_view_type_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- RoomType
CREATE TABLE room_type (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    property_id    BIGINT       NOT NULL,
    name           VARCHAR(200) NOT NULL,
    description    TEXT         NULL,
    area_sqm       DECIMAL(6,2) NULL,
    area_pyeong    VARCHAR(20)  NULL,
    base_occupancy INT          NOT NULL DEFAULT 2,
    max_occupancy  INT          NOT NULL DEFAULT 2,
    base_inventory INT          NOT NULL DEFAULT 0,
    check_in_time  VARCHAR(10)  NULL,
    check_out_time VARCHAR(10)  NULL,
    status         VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted        TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at     TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_rt_property (property_id),
    INDEX idx_rt_status (status),
    INDEX idx_rt_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- RoomTypeAttribute
CREATE TABLE room_type_attribute (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    room_type_id    BIGINT       NOT NULL,
    attribute_key   VARCHAR(100) NOT NULL,
    attribute_value VARCHAR(500) NOT NULL,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted         TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at      TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_rta_room_type (room_type_id),
    INDEX idx_rta_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- RoomTypeBed
CREATE TABLE room_type_bed (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    room_type_id BIGINT       NOT NULL,
    bed_type_id  BIGINT       NOT NULL,
    quantity     INT          NOT NULL DEFAULT 1,
    created_at   TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted      TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at   TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_rtb_room_type (room_type_id),
    INDEX idx_rtb_bed_type (bed_type_id),
    INDEX idx_rtb_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- RoomTypeView
CREATE TABLE room_type_view (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    room_type_id BIGINT       NOT NULL,
    view_type_id BIGINT       NOT NULL,
    created_at   TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted      TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at   TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_rtv_room_type (room_type_id),
    INDEX idx_rtv_view_type (view_type_id),
    INDEX idx_rtv_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- RoomAmenity
CREATE TABLE room_amenity (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    room_type_id     BIGINT        NOT NULL,
    amenity_type     VARCHAR(50)   NOT NULL,
    name             VARCHAR(200)  NOT NULL,
    additional_price DECIMAL(12,2) NOT NULL DEFAULT 0,
    sort_order       INT           NOT NULL DEFAULT 0,
    created_at       TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted          TINYINT(1)    NOT NULL DEFAULT 0,
    deleted_at       TIMESTAMP(6)  NULL,
    PRIMARY KEY (id),
    INDEX idx_ra_room_type (room_type_id),
    INDEX idx_ra_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- RoomPhoto
CREATE TABLE room_photo (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    room_type_id BIGINT        NOT NULL,
    photo_type   VARCHAR(50)   NOT NULL,
    origin_url   VARCHAR(1000) NOT NULL,
    cdn_url      VARCHAR(1000) NULL,
    sort_order   INT           NOT NULL DEFAULT 0,
    created_at   TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted      TINYINT(1)    NOT NULL DEFAULT 0,
    deleted_at   TIMESTAMP(6)  NULL,
    PRIMARY KEY (id),
    INDEX idx_rp_room_type (room_type_id),
    INDEX idx_rp_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
