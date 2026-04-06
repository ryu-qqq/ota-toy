package com.ryuqq.otatoy.application.supplier.processor;

import com.ryuqq.otatoy.domain.supplier.SupplierTaskType;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * TaskType에 맞는 RawDataProcessor를 제공하는 라우터.
 * Spring이 주입한 모든 SupplierRawDataProcessor 구현체를 TaskType으로 매핑한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierRawDataProcessorProvider {

    private final Map<SupplierTaskType, SupplierRawDataProcessor> processorMap;

    public SupplierRawDataProcessorProvider(List<SupplierRawDataProcessor> processors) {
        this.processorMap = processors.stream()
                .collect(Collectors.toMap(SupplierRawDataProcessor::supportedType, Function.identity()));
    }

    public SupplierRawDataProcessor getProcessor(SupplierTaskType taskType) {
        SupplierRawDataProcessor processor = processorMap.get(taskType);
        if (processor == null) {
            throw new IllegalArgumentException("지원하지 않는 TaskType: " + taskType);
        }
        return processor;
    }
}
