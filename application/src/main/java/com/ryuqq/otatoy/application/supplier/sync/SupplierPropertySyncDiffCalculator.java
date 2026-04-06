package com.ryuqq.otatoy.application.supplier.sync;

import com.ryuqq.otatoy.application.supplier.dto.SupplierPropertyData;
import com.ryuqq.otatoy.application.supplier.dto.SupplierSyncDiff;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.domain.supplier.SupplierMappingStatus;
import com.ryuqq.otatoy.domain.supplier.SupplierProperty;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 공급자 Property 스냅샷과 기존 매핑을 비교해 동기화 Diff를 계산한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierPropertySyncDiffCalculator {

    public SupplierSyncDiff calculate(SupplierId supplierId,
                                      List<SupplierPropertyData> newData,
                                      List<SupplierProperty> existingMappings,
                                      Instant now) {
        Map<String, SupplierProperty> existingMap = existingMappings.stream()
                .filter(sp -> sp.status() == SupplierMappingStatus.MAPPED)
                .collect(Collectors.toMap(SupplierProperty::supplierPropertyCode, sp -> sp));

        Set<String> newExternalIds = newData.stream()
                .map(SupplierPropertyData::externalPropertyId)
                .collect(Collectors.toSet());

        List<SupplierPropertyData> added = new ArrayList<>();
        List<SupplierPropertyData> updated = new ArrayList<>();

        for (SupplierPropertyData propertyData : newData) {
            if (existingMap.containsKey(propertyData.externalPropertyId())) {
                updated.add(propertyData);
            } else {
                added.add(propertyData);
            }
        }

        List<SupplierProperty> deleted = existingMappings.stream()
                .filter(sp -> sp.status() == SupplierMappingStatus.MAPPED)
                .filter(sp -> !newExternalIds.contains(sp.supplierPropertyCode()))
                .toList();

        return new SupplierSyncDiff(supplierId, added, updated, deleted, now);
    }
}
