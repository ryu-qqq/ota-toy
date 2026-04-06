package com.ryuqq.otatoy.application.supplier.service;

import com.ryuqq.otatoy.application.supplier.dto.command.ProcessSupplierRawDataCommand;
import com.ryuqq.otatoy.application.supplier.manager.SupplierRawDataReadManager;
import com.ryuqq.otatoy.application.supplier.manager.SupplierRawDataTransactionManager;
import com.ryuqq.otatoy.application.supplier.port.in.ProcessSupplierRawDataUseCase;
import com.ryuqq.otatoy.application.supplier.processor.SupplierRawDataProcessor;
import com.ryuqq.otatoy.application.supplier.processor.SupplierRawDataProcessorProvider;
import com.ryuqq.otatoy.domain.supplier.SupplierRawData;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * FETCHED 상태의 SupplierRawData를 가공하여 도메인 동기화를 수행하는 Service.
 * TaskType별 전략 패턴으로 가공 로직을 분리한다.
 * Service에 @Transactional 없음 (APP-SVC-001).
 *
 * 흐름:
 * 1. FETCHED 상태 RawData 배치 조회
 * 2. 각 RawData:
 *    a. markProcessing → persist
 *    b. ProcessorProvider → TaskType별 전략 선택
 *    c. processor.process(rawData) → 도메인 통합
 *    d. 성공: markSynced → persist
 *    e. 실패: markFailed → persist
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Service
public class ProcessSupplierRawDataService implements ProcessSupplierRawDataUseCase {

    private final SupplierRawDataReadManager rawDataReadManager;
    private final SupplierRawDataTransactionManager rawDataTransactionManager;
    private final SupplierRawDataProcessorProvider processorProvider;

    public ProcessSupplierRawDataService(SupplierRawDataReadManager rawDataReadManager,
                                          SupplierRawDataTransactionManager rawDataTransactionManager,
                                          SupplierRawDataProcessorProvider processorProvider) {
        this.rawDataReadManager = rawDataReadManager;
        this.rawDataTransactionManager = rawDataTransactionManager;
        this.processorProvider = processorProvider;
    }

    @Override
    public void execute(ProcessSupplierRawDataCommand command) {
        List<SupplierRawData> rawDataList = rawDataReadManager.findFetchedBatch(command.batchSize());

        for (SupplierRawData rawData : rawDataList) {
            processRawData(rawData, command);
        }
    }

    private void processRawData(SupplierRawData rawData, ProcessSupplierRawDataCommand command) {
        rawData.markProcessing();
        rawDataTransactionManager.persist(rawData);

        try {
            SupplierRawDataProcessor processor = processorProvider.getProcessor(rawData.taskType());
            processor.process(rawData);

            rawData.markSynced(command.now());
            rawDataTransactionManager.persist(rawData);

        } catch (Exception e) {
            rawData.markFailed(command.now());
            rawDataTransactionManager.persist(rawData);
        }
    }
}
