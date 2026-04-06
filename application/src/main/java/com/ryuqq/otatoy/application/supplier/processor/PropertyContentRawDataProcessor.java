package com.ryuqq.otatoy.application.supplier.processor;

import com.ryuqq.otatoy.application.supplier.dto.SupplierPropertyData;
import com.ryuqq.otatoy.application.supplier.dto.SupplierSyncDiff;
import com.ryuqq.otatoy.application.supplier.facade.SupplierSyncPersistenceFacade;
import com.ryuqq.otatoy.application.supplier.manager.SupplierPropertyReadManager;
import com.ryuqq.otatoy.application.supplier.parser.SupplierRawDataParser;
import com.ryuqq.otatoy.application.supplier.parser.SupplierRawDataParserProvider;
import com.ryuqq.otatoy.application.supplier.sync.SupplierPropertySyncDiffCalculator;
import com.ryuqq.otatoy.domain.supplier.SupplierProperty;
import com.ryuqq.otatoy.domain.supplier.SupplierRawData;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskType;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PROPERTY_CONTENT 타입 RawData 가공 프로세서.
 * rawData가 apiType, supplierId를 이미 갖고 있으므로 별도 Config 조회 불필요.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class PropertyContentRawDataProcessor implements SupplierRawDataProcessor {

    private final SupplierRawDataParserProvider parserProvider;
    private final SupplierPropertySyncDiffCalculator propertySyncDiffCalculator;
    private final SupplierPropertyReadManager supplierPropertyReadManager;
    private final SupplierSyncPersistenceFacade syncFacade;

    public PropertyContentRawDataProcessor(SupplierRawDataParserProvider parserProvider,
                                            SupplierPropertySyncDiffCalculator propertySyncDiffCalculator,
                                            SupplierPropertyReadManager supplierPropertyReadManager,
                                            SupplierSyncPersistenceFacade syncFacade) {
        this.parserProvider = parserProvider;
        this.propertySyncDiffCalculator = propertySyncDiffCalculator;
        this.supplierPropertyReadManager = supplierPropertyReadManager;
        this.syncFacade = syncFacade;
    }

    @Override
    public SupplierTaskType supportedType() {
        return SupplierTaskType.PROPERTY_CONTENT;
    }

    @Override
    public void process(SupplierRawData rawData) {
        SupplierRawDataParser<SupplierPropertyData> parser =
                parserProvider.getParser(rawData.apiType(), rawData.taskType());

        List<SupplierPropertyData> properties = parser.parse(rawData.rawPayload());

        List<SupplierProperty> existingMappings =
                supplierPropertyReadManager.findBySupplierId(rawData.supplierId());

        SupplierSyncDiff diff = propertySyncDiffCalculator.calculate(
                rawData.supplierId(), properties, existingMappings, rawData.fetchedAt());

        syncFacade.sync(diff);
    }
}
