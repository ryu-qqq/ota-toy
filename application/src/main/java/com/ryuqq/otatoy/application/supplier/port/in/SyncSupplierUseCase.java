package com.ryuqq.otatoy.application.supplier.port.in;

import com.ryuqq.otatoy.application.supplier.dto.SyncSupplierCommand;

/**
 * 공급자 데이터 가공/동기화 UseCase (Inbound Port).
 * Raw 데이터를 파싱하여 Diff 계산 후 Property/매핑을 동기화한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface SyncSupplierUseCase {

    void execute(SyncSupplierCommand command);
}
