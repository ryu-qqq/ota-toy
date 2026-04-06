package com.ryuqq.otatoy.application.supplier.dto;

import com.ryuqq.otatoy.domain.supplier.SupplierId;

/**
 * 공급자 데이터 수집 명령.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public record FetchSupplierCommand(SupplierId supplierId) {
}
