package com.ryuqq.otatoy.application.supplier.port.in;

import com.ryuqq.otatoy.application.supplier.dto.command.ExecuteSupplierTaskCommand;

/**
 * PENDING 상태의 SupplierTask를 소비하여 외부 API 호출 + RawData 저장을 수행하는 UseCase (Inbound Port).
 * 스케줄러가 주기적으로 호출한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
public interface ExecuteSupplierTaskUseCase {

    void execute(ExecuteSupplierTaskCommand command);
}
