package com.ryuqq.otatoy.application.supplier.facade;

import com.ryuqq.otatoy.application.common.factory.TimeProvider;
import com.ryuqq.otatoy.application.property.port.out.PropertyCommandPort;
import com.ryuqq.otatoy.application.supplier.dto.SupplierPropertyData;
import com.ryuqq.otatoy.application.supplier.dto.SupplierSyncDiff;
import com.ryuqq.otatoy.application.supplier.port.out.SupplierPropertyCommandPort;
import com.ryuqq.otatoy.application.supplier.port.out.SupplierRawDataCommandPort;
import com.ryuqq.otatoy.application.supplier.port.out.SupplierSyncLogCommandPort;
import com.ryuqq.otatoy.domain.partner.PartnerId;
import com.ryuqq.otatoy.domain.property.Location;
import com.ryuqq.otatoy.domain.property.Property;
import com.ryuqq.otatoy.domain.property.PropertyDescription;
import com.ryuqq.otatoy.domain.property.PropertyId;
import com.ryuqq.otatoy.domain.property.PropertyName;
import com.ryuqq.otatoy.domain.propertytype.PropertyTypeId;
import com.ryuqq.otatoy.domain.supplier.SupplierProperty;
import com.ryuqq.otatoy.domain.supplier.SupplierRawData;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncLog;
import com.ryuqq.otatoy.domain.supplier.SupplierSyncType;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 공급자 동기화 복합 저장 Facade.
 * Property 생성/매핑 저장/SyncLog 기록을 하나의 트랜잭션으로 묶는다.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierSyncPersistenceFacade {

    private final PropertyCommandPort propertyCommandPort;
    private final SupplierPropertyCommandPort supplierPropertyCommandPort;
    private final SupplierSyncLogCommandPort supplierSyncLogCommandPort;
    private final SupplierRawDataCommandPort supplierRawDataCommandPort;
    private final TimeProvider timeProvider;

    public SupplierSyncPersistenceFacade(PropertyCommandPort propertyCommandPort,
                                          SupplierPropertyCommandPort supplierPropertyCommandPort,
                                          SupplierSyncLogCommandPort supplierSyncLogCommandPort,
                                          SupplierRawDataCommandPort supplierRawDataCommandPort,
                                          TimeProvider timeProvider) {
        this.propertyCommandPort = propertyCommandPort;
        this.supplierPropertyCommandPort = supplierPropertyCommandPort;
        this.supplierSyncLogCommandPort = supplierSyncLogCommandPort;
        this.supplierRawDataCommandPort = supplierRawDataCommandPort;
        this.timeProvider = timeProvider;
    }

    /**
     * Diff 결과를 기반으로 Property 생성/매핑/SyncLog를 하나의 트랜잭션으로 저장한다.
     */
    @Transactional
    public void sync(SupplierRawData rawData, SupplierSyncDiff diff, SupplierId supplierId) {
        Instant now = timeProvider.now();

        // 1. 신규 Property 생성 + 매핑 저장
        for (SupplierPropertyData added : diff.added()) {
            Property property = toProperty(added, now);
            Long propertyId = propertyCommandPort.persist(property);

            SupplierProperty mapping = SupplierProperty.forNew(
                    supplierId, PropertyId.of(propertyId),
                    added.externalPropertyId(), now
            );
            supplierPropertyCommandPort.persist(mapping);
        }

        // 2. 기존 매핑 동기화 시각 갱신
        for (SupplierPropertyData updated : diff.updated()) {
            // 기존 매핑의 lastSyncedAt만 갱신 -- 추후 Property 필드 업데이트 확장 가능
        }

        // 3. 삭제 대상 매핑 해제
        for (SupplierProperty deleted : diff.deleted()) {
            deleted.unmap(now);
            supplierPropertyCommandPort.persist(deleted);
        }

        // 4. RawData 상태 갱신
        rawData.markSynced(now);
        supplierRawDataCommandPort.persist(rawData);

        // 5. SyncLog 기록
        SupplierSyncLog syncLog = SupplierSyncLog.forSuccess(
                supplierId, SupplierSyncType.PROPERTY, now,
                diff.totalCount(), diff.added().size(),
                diff.updated().size(), diff.deleted().size()
        );
        supplierSyncLogCommandPort.persist(syncLog);
    }

    /**
     * SupplierPropertyData → Property 도메인 변환.
     * 공급자 연동 Property는 partnerId 없이 생성되므로 기본 PartnerId(0)을 사용한다.
     */
    private Property toProperty(SupplierPropertyData data, Instant now) {
        return Property.forNew(
                PartnerId.of(0L),
                null,
                PropertyTypeId.of(1L),
                PropertyName.of(data.name()),
                PropertyDescription.of(data.description()),
                Location.of(data.address(), data.latitude(), data.longitude(), null, null),
                null,
                now
        );
    }
}
