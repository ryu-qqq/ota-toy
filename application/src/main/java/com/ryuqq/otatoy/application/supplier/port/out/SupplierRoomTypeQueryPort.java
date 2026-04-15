package com.ryuqq.otatoy.application.supplier.port.out;

import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.domain.supplier.SupplierRoomType;

import java.util.List;

/**
 * SupplierRoomType 조회 전용 Outbound Port.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SupplierRoomTypeQueryPort {

    List<SupplierRoomType> findBySupplierId(SupplierId supplierId);
}
