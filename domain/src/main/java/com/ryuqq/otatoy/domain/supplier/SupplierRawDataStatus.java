package com.ryuqq.otatoy.domain.supplier;

/**
 * 공급자 원시 데이터 처리 상태.
 * FETCHED: 수집 완료, PROCESSING: 가공 중, SYNCED: 동기화 완료, FAILED: 실패.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public enum SupplierRawDataStatus {

    FETCHED("수집 완료"),
    PROCESSING("가공 중"),
    SYNCED("동기화 완료"),
    FAILED("실패");

    private final String displayName;

    SupplierRawDataStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
