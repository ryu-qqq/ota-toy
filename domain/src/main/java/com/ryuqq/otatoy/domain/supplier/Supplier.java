package com.ryuqq.otatoy.domain.supplier;

import com.ryuqq.otatoy.domain.common.vo.Email;
import com.ryuqq.otatoy.domain.common.vo.PhoneNumber;

import java.time.Instant;
import java.util.Objects;

public class Supplier {

    private final SupplierId id;
    private SupplierName name;
    private String nameKr;
    private String companyTitle;
    private String ownerName;
    private String businessNo;
    private String address;
    private PhoneNumber phone;
    private Email email;
    private String termsUrl;
    private SupplierStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Supplier(SupplierId id, SupplierName name, String nameKr, String companyTitle,
                     String ownerName, String businessNo, String address,
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

    public static Supplier forNew(SupplierName name, String nameKr, String companyTitle,
                                   String ownerName, String businessNo, String address,
                                   PhoneNumber phone, Email email, String termsUrl, Instant now) {
        validateCompanyInfo(nameKr, companyTitle, ownerName, businessNo);
        return new Supplier(SupplierId.of(null), name, nameKr, companyTitle, ownerName, businessNo,
                address, phone, email, termsUrl, SupplierStatus.ACTIVE, now, now);
    }

    private static void validateCompanyInfo(String nameKr, String companyTitle,
                                             String ownerName, String businessNo) {
        if (nameKr == null || nameKr.isBlank()) {
            throw new IllegalArgumentException("공급자 한글명은 필수입니다");
        }
        if (companyTitle == null || companyTitle.isBlank()) {
            throw new IllegalArgumentException("회사명은 필수입니다");
        }
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("대표자명은 필수입니다");
        }
        if (businessNo == null || businessNo.isBlank()) {
            throw new IllegalArgumentException("사업자번호는 필수입니다");
        }
    }

    public static Supplier reconstitute(SupplierId id, SupplierName name, String nameKr, String companyTitle,
                                         String ownerName, String businessNo, String address,
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
    public String nameKr() { return nameKr; }
    public String companyTitle() { return companyTitle; }
    public String ownerName() { return ownerName; }
    public String businessNo() { return businessNo; }
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
