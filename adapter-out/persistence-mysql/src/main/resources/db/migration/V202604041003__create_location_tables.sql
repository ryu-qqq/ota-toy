-- Landmark
CREATE TABLE landmark (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(200) NOT NULL,
    landmark_type VARCHAR(50)  NOT NULL,
    latitude      DOUBLE       NOT NULL DEFAULT 0,
    longitude     DOUBLE       NOT NULL DEFAULT 0,
    created_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted       TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at    TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_landmark_type (landmark_type),
    INDEX idx_landmark_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PropertyLandmark
CREATE TABLE property_landmark (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    property_id     BIGINT       NOT NULL,
    landmark_id     BIGINT       NOT NULL,
    distance_km     DOUBLE       NOT NULL DEFAULT 0,
    walking_minutes INT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted         TINYINT(1)   NOT NULL DEFAULT 0,
    deleted_at      TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_pl_property (property_id),
    INDEX idx_pl_landmark (landmark_id),
    INDEX idx_pl_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
