-- SupplierApiConfig: 전략 패턴 지원을 위한 api_type 컬럼 추가
-- 수집 주기 판별은 supplier_sync_log의 마지막 FETCH SUCCESS 기록으로 처리한다.
ALTER TABLE supplier_api_config
    ADD COLUMN api_type VARCHAR(30) NOT NULL DEFAULT 'MOCK' AFTER supplier_id;

CREATE INDEX idx_sac_api_type ON supplier_api_config (api_type);
