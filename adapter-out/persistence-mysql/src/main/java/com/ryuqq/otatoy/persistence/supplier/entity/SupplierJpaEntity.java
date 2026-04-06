package com.ryuqq.otatoy.persistence.supplier.entity;

import com.ryuqq.otatoy.persistence.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Supplier JPA Entity.
 * 공급자 정보를 매핑하는 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-06
 */
@Entity
@Table(name = "supplier")
public class SupplierJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 200)
    private String nameKr;

    @Column(nullable = false, length = 200)
    private String companyTitle;

    @Column(nullable = false, length = 100)
    private String ownerName;

    @Column(nullable = false, length = 50)
    private String businessNo;

    @Column(length = 500)
    private String address;

    @Column(length = 30)
    private String phone;

    @Column(length = 200)
    private String email;

    @Column(length = 500)
    private String termsUrl;

    @Column(nullable = false, length = 30)
    private String status;

    protected SupplierJpaEntity() {
        super();
    }

    private SupplierJpaEntity(Long id, String name, String nameKr, String companyTitle,
                               String ownerName, String businessNo, String address,
                               String phone, String email, String termsUrl, String status,
                               Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
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
    }

    public static SupplierJpaEntity create(Long id, String name, String nameKr, String companyTitle,
                                             String ownerName, String businessNo, String address,
                                             String phone, String email, String termsUrl, String status,
                                             Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new SupplierJpaEntity(id, name, nameKr, companyTitle, ownerName, businessNo,
                address, phone, email, termsUrl, status, createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getNameKr() { return nameKr; }
    public String getCompanyTitle() { return companyTitle; }
    public String getOwnerName() { return ownerName; }
    public String getBusinessNo() { return businessNo; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getTermsUrl() { return termsUrl; }
    public String getStatus() { return status; }
}
