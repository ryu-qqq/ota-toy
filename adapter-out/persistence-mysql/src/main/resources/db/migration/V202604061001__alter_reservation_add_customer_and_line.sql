-- reservation 테이블에 customer_id 추가, rate_plan_id 제거 (Line 단위로 이동)
ALTER TABLE reservation
    ADD COLUMN customer_id BIGINT NOT NULL DEFAULT 0 AFTER id,
    DROP COLUMN rate_plan_id;

ALTER TABLE reservation
    ADD INDEX idx_rsv_customer (customer_id);

-- reservation_line 테이블 생성
CREATE TABLE reservation_line (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    reservation_id    BIGINT        NOT NULL,
    rate_plan_id      BIGINT        NOT NULL,
    room_count        INT           NOT NULL DEFAULT 1,
    subtotal_amount   DECIMAL(19,2) NOT NULL DEFAULT 0,
    created_at        TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted           TINYINT(1)    NOT NULL DEFAULT 0,
    deleted_at        TIMESTAMP(6)  NULL,
    PRIMARY KEY (id),
    INDEX idx_rl_reservation (reservation_id),
    INDEX idx_rl_rate_plan (rate_plan_id),
    INDEX idx_rl_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- reservation_item 테이블에 reservation_line_id, nightly_rate 추가
ALTER TABLE reservation_item
    ADD COLUMN reservation_line_id BIGINT NOT NULL DEFAULT 0 AFTER reservation_id,
    ADD COLUMN nightly_rate DECIMAL(19,2) NOT NULL DEFAULT 0 AFTER stay_date;

ALTER TABLE reservation_item
    ADD INDEX idx_ri_reservation_line (reservation_line_id);
