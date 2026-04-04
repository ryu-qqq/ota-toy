package com.ryuqq.otatoy.domain.supplier;

import com.ryuqq.otatoy.domain.common.vo.Email;
import com.ryuqq.otatoy.domain.common.vo.PhoneNumber;

import java.time.Instant;
import java.util.Objects;

/**
 * 외부 공급자(Supplier)를 나타내는 Aggregate Root.
 * 공급자의 사업자 정보, 연락처, 상태를 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 * @see SupplierProperty 공급자-숙소 매핑
 * @see SupplierRoomType 공급자-객실 매핑
 * @see SupplierSyncLog 동기화 로그
 */
public class Supplier {

    private final SupplierId id;
    private SupplierName name;
    private SupplierNameKr nameKr;
    private CompanyTitle companyTitle;
    private OwnerName ownerName;
    private BusinessNo businessNo;
    private String address;
    private PhoneNumber phone;
    private Email email;
    private String termsUrl;
    private SupplierStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Supplier(SupplierId id, SupplierName name, SupplierNameKr nameKr, CompanyTitle companyTitle,
                     OwnerName ownerName, BusinessNo businessNo, String address,
                     PhoneNumber phone, Email email, String termsUrl,
                     SupplierStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.nameKr = nameKr;
        this.companyTitle = companyTitle;
        this.ownerName = ownerName;
        this.businessNo = businessNo;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.termsUrl = termsUrl;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Supplier forNew(SupplierName name, SupplierNameKr nameKr, CompanyTitle companyTitle,
                                   OwnerName ownerName, BusinessNo businessNo, String address,
                                   PhoneNumber phone, Email email, String termsUrl, Instant now) {
        return new Supplier(SupplierId.of(null), name, nameKr, companyTitle, ownerName, businessNo,
                address, phone, email, termsUrl, SupplierStatus.ACTIVE, now, now);
    }

    public static Supplier reconstitute(SupplierId id, SupplierName name, SupplierNameKr nameKr, CompanyTitle companyTitle,
                                         OwnerName ownerName, BusinessNo businessNo, String address,
                                         PhoneNumber phone, Email email, String termsUrl,
                                         SupplierStatus status, Instant createdAt, Instant updatedAt) {
        return new Supplier(id, name, nameKr, companyTitle, ownerName, businessNo,
                address, phone, email, termsUrl, status, createdAt, updatedAt);
    }

    public void suspend(Instant now) {
        this.status = status.transitTo(SupplierStatus.SUSPENDED);
        this.updatedAt = now;
    }

    public void activate(Instant now) {
        this.status = status.transitTo(SupplierStatus.ACTIVE);
        this.updatedAt = now;
    }

    public void terminate(Instant now) {
        this.status = status.transitTo(SupplierStatus.TERMINATED);
        this.updatedAt = now;
    }

    public boolean isActive() {
        return this.status == SupplierStatus.ACTIVE;
    }

    public SupplierId id() { return id; }
    public SupplierName name() { return name; }
    public SupplierNameKr nameKr() { return nameKr; }
    public CompanyTitle companyTitle() { return companyTitle; }
    public OwnerName ownerName() { return ownerName; }
    public BusinessNo businessNo() { return businessNo; }
    public String address() { return address; }
    public PhoneNumber phone() { return phone; }
    public Email email() { return email; }
    public String termsUrl() { return termsUrl; }
    public SupplierStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Supplier s)) return false;
        return id != null && id.value() != null && id.equals(s.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hashCode(id) : System.identityHashCode(this);
    }
}
