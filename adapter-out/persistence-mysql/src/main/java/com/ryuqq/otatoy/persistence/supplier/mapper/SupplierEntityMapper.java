package com.ryuqq.otatoy.persistence.supplier.mapper;

import com.ryuqq.otatoy.domain.common.vo.Email;
import com.ryuqq.otatoy.domain.common.vo.PhoneNumber;
import com.ryuqq.otatoy.domain.supplier.BusinessNo;
import com.ryuqq.otatoy.domain.supplier.CompanyTitle;
import com.ryuqq.otatoy.domain.supplier.OwnerName;
import com.ryuqq.otatoy.domain.supplier.Supplier;
import com.ryuqq.otatoy.domain.supplier.SupplierId;
import com.ryuqq.otatoy.domain.supplier.SupplierName;
import com.ryuqq.otatoy.domain.supplier.SupplierNameKr;
import com.ryuqq.otatoy.domain.supplier.SupplierStatus;
import com.ryuqq.otatoy.persistence.supplier.entity.SupplierJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Supplier Domain <-> Entity 변환 전담 Mapper.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Component
public class SupplierEntityMapper {

    /**
     * Entity -> Domain 변환 (조회 시).
     */
    public Supplier toDomain(SupplierJpaEntity entity) {
        return Supplier.reconstitute(
                SupplierId.of(entity.getId()),
                SupplierName.of(entity.getName()),
                SupplierNameKr.of(entity.getNameKr()),
                CompanyTitle.of(entity.getCompanyTitle()),
                OwnerName.of(entity.getOwnerName()),
                BusinessNo.of(entity.getBusinessNo()),
                entity.getAddress(),
                entity.getPhone() != null ? PhoneNumber.of(entity.getPhone()) : null,
                entity.getEmail() != null ? Email.of(entity.getEmail()) : null,
                entity.getTermsUrl(),
                SupplierStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
