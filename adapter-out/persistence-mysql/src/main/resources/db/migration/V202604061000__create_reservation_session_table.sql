-- 예약 세션 테이블: 2단계 예약 프로세스의 1단계 (재고 선점)
CREATE TABLE reservation_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    idempotency_key VARCHAR(64) NOT NULL,
    property_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    rate_plan_id BIGINT NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    guest_count INT NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reservation_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    UNIQUE INDEX uk_session_idempotency_key (idempotency_key),
    INDEX idx_session_status_created (status, created_at)
);
