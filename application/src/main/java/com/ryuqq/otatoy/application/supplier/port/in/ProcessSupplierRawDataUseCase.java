package com.ryuqq.otatoy.application.supplier.port.in;

import com.ryuqq.otatoy.application.supplier.dto.command.ProcessSupplierRawDataCommand;

/**
 * FETCHED 상태의 SupplierRawData를 가공하여 도메인 동기화를 수행하는 UseCase (Inbound Port).
 * 스케줄러가 주기적으로 호출한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface ProcessSupplierRawDataUseCase {

    void execute(ProcessSupplierRawDataCommand command);
}
