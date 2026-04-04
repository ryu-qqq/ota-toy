-- RatePlan
CREATE TABLE rate_plan (
    id                              BIGINT        NOT NULL AUTO_INCREMENT,
    room_type_id                    BIGINT        NOT NULL,
    name                            VARCHAR(200)  NOT NULL,
    source_type                     VARCHAR(30)   NOT NULL,
    supplier_id                     BIGINT        NULL,
    is_free_cancellation            TINYINT(1)    NOT NULL DEFAULT 0,
    is_non_refundable               TINYINT(1)    NOT NULL DEFAULT 0,
    free_cancellation_deadline_days INT           NOT NULL DEFAULT 0,
    cancellation_policy_text        TEXT          NULL,
    payment_policy                  VARCHAR(30)   NOT NULL,
    created_at                      TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at                      TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted                         TINYINT(1)    NOT NULL DEFAULT 0,
    deleted_at                      TIMESTAMP(6)  NULL,
    PRIMARY KEY (id),
    INDEX idx_rp_room_type (room_type_id),
    INDEX idx_rp_supplier (supplier_id),
    INDEX idx_rp_source_type (source_type),
    INDEX idx_rp_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- RatePlanAddOn
CREATE TABLE rate_plan_add_on (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    rate_plan_id BIGINT        NOT NULL,
    add_on_type  VARCHAR(50)   NOT NULL,
    name         VARCHAR(200)  NOT NULL,
    price        DECIMAL(12,2) NOT NULL DEFAULT 0,
    is_included  TINYINT(1)    NOT NULL DEFAULT 0,
    created_at   TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted      TINYINT(1)    NOT NULL DEFAULT 0,
    deleted_at   TIMESTAMP(6)  NULL,
    PRIMARY KEY (id),
    INDEX idx_rpao_rate_plan (rate_plan_id),
    INDEX idx_rpao_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- RateRule
CREATE TABLE rate_rule (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    rate_plan_id   BIGINT        NOT NULL,
    start_date     DATE          NOT NULL,
    end_date       DATE          NOT NULL,
    base_price     DECIMAL(12,2) NOT NULL DEFAULT 0,
    weekday_price  DECIMAL(12,2) NULL,
    friday_price   DECIMAL(12,2) NULL,
    saturday_price DECIMAL(12,2) NULL,
    sunday_price   DECIMAL(12,2) NULL,
    created_at     TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted        TINYINT(1)    NOT NULL DEFAULT 0,
    deleted_at     TIMESTAMP(6)  NULL,
    PRIMARY KEY (id),
    INDEX idx_rr_rate_plan (rate_plan_id),
    INDEX idx_rr_date_range (start_date, end_date),
    INDEX idx_rr_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- RateOverride
CREATE TABLE rate_override (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    rate_rule_id  BIGINT        NOT NULL,
    override_date DATE          NOT NULL,
    price         DECIMAL(12,2) NOT NULL,
    reason        VARCHAR(500)  NULL,
    created_at    TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted       TINYINT(1)    NOT NULL DEFAULT 0,
    deleted_at    TIMESTAMP(6)  NULL,
    PRIMARY KEY (id),
    INDEX idx_ro_rate_rule (rate_rule_id),
    INDEX idx_ro_date (override_date),
    INDEX idx_ro_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Rate (calculated daily rate)
CREATE TABLE rate (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    rate_plan_id BIGINT        NOT NULL,
    rate_date    DATE          NOT NULL,
    base_price   DECIMAL(12,2) NOT NULL DEFAULT 0,
    created_at   TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted      TINYINT(1)    NOT NULL DEFAULT 0,
    deleted_at   TIMESTAMP(6)  NULL,
    PRIMARY KEY (id),
    INDEX idx_rate_plan_date (rate_plan_id, rate_date),
    INDEX idx_rate_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
