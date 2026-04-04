package com.ryuqq.otatoy.persistence.entity;

import jakarta.persistence.MappedSuperclass;

import java.time.Instant;

@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseAuditEntity {

    private boolean deleted;

    private Instant deletedAt;

    protected SoftDeletableEntity() {
        super();
    }

    protected SoftDeletableEntity(Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(createdAt, updatedAt);
        this.deleted = deletedAt != null;
        this.deletedAt = deletedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

}
