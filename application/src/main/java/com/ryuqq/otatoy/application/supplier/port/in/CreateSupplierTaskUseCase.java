package com.ryuqq.otatoy.application.supplier.port.in;

import com.ryuqq.otatoy.application.supplier.dto.command.CreateSupplierTaskCommand;

/**
 * 수집 주기가 도래한 공급자를 판별하여 SupplierTask(PENDING)를 생성하는 UseCase (Inbound Port).
 * 스케줄러가 주기적으로 호출한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface CreateSupplierTaskUseCase {

    void execute(CreateSupplierTaskCommand command);
}
