-- SupplierRawData: Entity와 DDL 동기화 (task_type, api_type 컬럼 추가)
ALTER TABLE supplier_raw_data
    ADD COLUMN task_type VARCHAR(30) NOT NULL DEFAULT 'FETCH' AFTER supplier_id,
    ADD COLUMN api_type VARCHAR(30) NOT NULL DEFAULT 'PROPERTY_CONTENT' AFTER task_type;
