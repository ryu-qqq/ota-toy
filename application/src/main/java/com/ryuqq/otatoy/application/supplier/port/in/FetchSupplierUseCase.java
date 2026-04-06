package com.ryuqq.otatoy.application.supplier.port.in;

import com.ryuqq.otatoy.application.supplier.dto.FetchSupplierCommand;

/**
 * 공급자 데이터 수집 UseCase (Inbound Port).
 * 외부 API를 호출하여 Raw 데이터를 저장한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface FetchSupplierUseCase {

    void execute(FetchSupplierCommand command);
}
