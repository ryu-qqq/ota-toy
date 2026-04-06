package com.ryuqq.otatoy.application.supplier.processor;

import com.ryuqq.otatoy.application.pricing.manager.RatePlanReadManager;
import com.ryuqq.otatoy.application.supplier.dto.SupplierRateData;
import com.ryuqq.otatoy.application.supplier.dto.SupplierRateSyncBundle;
import com.ryuqq.otatoy.application.supplier.facade.SupplierRateSyncPersistenceFacade;
import com.ryuqq.otatoy.application.supplier.manager.SupplierRoomTypeReadManager;
import com.ryuqq.otatoy.application.supplier.parser.SupplierRawDataParser;
import com.ryuqq.otatoy.application.supplier.parser.SupplierRawDataParserProvider;
import com.ryuqq.otatoy.application.supplier.sync.SupplierRateSyncAssembler;
import com.ryuqq.otatoy.domain.pricing.RatePlans;
import com.ryuqq.otatoy.domain.roomtype.RoomTypeId;
import com.ryuqq.otatoy.domain.supplier.SupplierRawData;
import com.ryuqq.otatoy.domain.supplier.SupplierRoomType;
import com.ryuqq.otatoy.domain.supplier.SupplierTaskType;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RATE_AVAILABILITY 타입 RawData 가공 프로세서.
 * 파싱 → 매핑 조회 → RateSyncAssembler 조립 → Facade 저장.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class RateAvailabilityRawDataProcessor implements SupplierRawDataProcessor {

    private final SupplierRawDataParserProvider parserProvider;
    private final SupplierRoomTypeReadManager supplierRoomTypeReadManager;
    private final RatePlanReadManager ratePlanReadManager;
    private final SupplierRateSyncAssembler rateSyncAssembler;
    private final SupplierRateSyncPersistenceFacade rateSyncFacade;

    public RateAvailabilityRawDataProcessor(SupplierRawDataParserProvider parserProvider,
                                             SupplierRoomTypeReadManager supplierRoomTypeReadManager,
                                             RatePlanReadManager ratePlanReadManager,
                                             SupplierRateSyncAssembler rateSyncAssembler,
                                             SupplierRateSyncPersistenceFacade rateSyncFacade) {
        this.parserProvider = parserProvider;
        this.supplierRoomTypeReadManager = supplierRoomTypeReadManager;
        this.ratePlanReadManager = ratePlanReadManager;
        this.rateSyncAssembler = rateSyncAssembler;
        this.rateSyncFacade = rateSyncFacade;
    }

    @Override
    public SupplierTaskType supportedType() {
        return SupplierTaskType.RATE_AVAILABILITY;
    }

    @Override
    public void process(SupplierRawData rawData) {
        SupplierRawDataParser<SupplierRateData> parser =
                parserProvider.getParser(rawData.apiType(), rawData.taskType());
        List<SupplierRateData> rateDataList = parser.parse(rawData.rawPayload());

        List<SupplierRoomType> roomTypeMappings =
                supplierRoomTypeReadManager.findBySupplierId(rawData.supplierId());
        List<RoomTypeId> roomTypeIds = roomTypeMappings.stream().map(SupplierRoomType::roomTypeId).toList();
        RatePlans ratePlans = ratePlanReadManager.findByRoomTypeIds(roomTypeIds);

        SupplierRateSyncBundle bundle = rateSyncAssembler.assemble(
                rateDataList, roomTypeMappings, ratePlans, rawData.fetchedAt());

        if (!bundle.isEmpty()) {
            rateSyncFacade.persist(bundle.rates(), bundle.inventories(), bundle.cacheEntries());
        }
    }
}
