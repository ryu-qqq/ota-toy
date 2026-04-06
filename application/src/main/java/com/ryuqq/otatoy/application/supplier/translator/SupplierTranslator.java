package com.ryuqq.otatoy.application.supplier.translator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.otatoy.application.supplier.dto.SupplierPropertyData;
import com.ryuqq.otatoy.application.supplier.dto.SupplierSyncDiff;
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
 * ACL(Anti-Corruption Layer) 핵심 변환기.
 * 외부 공급자의 Raw JSON을 내부 Application DTO로 변환하고,
 * 기존 매핑과 비교하여 Diff를 계산한다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierTranslator {

    private static final TypeReference<List<SupplierPropertyData>> PROPERTY_LIST_TYPE =
            new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public SupplierTranslator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Raw JSON 문자열을 SupplierPropertyData 목록으로 파싱한다.
     */
    public List<SupplierPropertyData> parse(String rawPayload) {
        try {
            return objectMapper.readValue(rawPayload, PROPERTY_LIST_TYPE);
        } catch (Exception e) {
            throw new IllegalArgumentException("공급자 Raw 데이터 파싱에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 외부에서 가져온 데이터와 기존 매핑을 비교하여 Diff를 계산한다.
     * externalPropertyId(supplierPropertyCode) 기준으로 added/updated/deleted를 분류한다.
     */
    public SupplierSyncDiff calculateDiff(List<SupplierPropertyData> newData,
                                           List<SupplierProperty> existingMappings,
                                           Instant now) {
        // 기존 매핑의 supplierPropertyCode → SupplierProperty 맵
        Map<String, SupplierProperty> existingMap = existingMappings.stream()
                .filter(sp -> sp.status() == SupplierMappingStatus.MAPPED)
                .collect(Collectors.toMap(SupplierProperty::supplierPropertyCode, sp -> sp));

        // 새 데이터의 externalPropertyId 집합
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

        // 기존에 있었지만 새 데이터에 없는 것 → 삭제 대상
        List<SupplierProperty> deleted = existingMappings.stream()
                .filter(sp -> sp.status() == SupplierMappingStatus.MAPPED)
                .filter(sp -> !newExternalIds.contains(sp.supplierPropertyCode()))
                .toList();

        return new SupplierSyncDiff(added, updated, deleted, now);
    }
}
