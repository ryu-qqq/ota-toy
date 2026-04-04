package com.ryuqq.otatoy.domain.supplier;

import java.time.Instant;
import java.util.Objects;

public class Supplier {

    private final SupplierId id;
    private String name;
    private String nameKr;
    private String companyTitle;
    private String ownerName;
    private String businessNo;
    private String address;
    private String phone;
    private String email;
    private String termsUrl;
    private SupplierStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Supplier(SupplierId id, String name, String nameKr, String companyTitle,
                     String ownerName, String businessNo, String address,
                     String phone, String email, String termsUrl,
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

    public static Supplier forNew(String name, String nameKr, String companyTitle,
                                   String ownerName, String businessNo, String address,
                                   String phone, String email, String termsUrl, Instant now) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("공급자명은 필수입니다");
        }
        return new Supplier(null, name, nameKr, companyTitle, ownerName, businessNo,
                address, phone, email, termsUrl, SupplierStatus.ACTIVE, now, now);
    }

    public static Supplier reconstitute(SupplierId id, String name, String nameKr, String companyTitle,
                                         String ownerName, String businessNo, String address,
                                         String phone, String email, String termsUrl,
                                         SupplierStatus status, Instant createdAt, Instant updatedAt) {
        return new Supplier(id, name, nameKr, companyTitle, ownerName, businessNo,
                address, phone, email, termsUrl, status, createdAt, updatedAt);
    }

    public void suspend(Instant now) {
        this.status = SupplierStatus.SUSPENDED;
        this.updatedAt = now;
    }

    public void activate(Instant now) {
        this.status = SupplierStatus.ACTIVE;
        this.updatedAt = now;
    }

    public void terminate(Instant now) {
        this.status = SupplierStatus.TERMINATED;
        this.updatedAt = now;
    }

    public boolean isActive() {
        return this.status == SupplierStatus.ACTIVE;
    }

    public SupplierId id() { return id; }
    public String name() { return name; }
    public String nameKr() { return nameKr; }
    public String companyTitle() { return companyTitle; }
    public String ownerName() { return ownerName; }
    public String businessNo() { return businessNo; }
    public String address() { return address; }
    public String phone() { return phone; }
    public String email() { return email; }
    public String termsUrl() { return termsUrl; }
    public SupplierStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Supplier s)) return false;
        return id != null && id.equals(s.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
