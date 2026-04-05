package com.ryuqq.otatoy.persistence.partner.entity;

import com.ryuqq.otatoy.persistence.entity.SoftDeletableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Partner JPA Entity.
 * 파트너 정보를 매핑하는 순수 데이터 매핑 객체.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
@Entity
@Table(name = "partner")
public class PartnerJpaEntity extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String status;

    protected PartnerJpaEntity() {
        super();
    }

    private PartnerJpaEntity(Long id, String name, String status,
                              Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public static PartnerJpaEntity create(Long id, String name, String status,
                                           Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new PartnerJpaEntity(id, name, status, createdAt, updatedAt, deletedAt);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }
}
