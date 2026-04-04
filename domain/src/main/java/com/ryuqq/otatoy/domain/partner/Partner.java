package com.ryuqq.otatoy.domain.partner;

import java.time.Instant;
import java.util.Objects;

public class Partner {

    private final PartnerId id;
    private PartnerName name;
    private PartnerStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Partner(PartnerId id, PartnerName name, PartnerStatus status,
                    Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Partner forNew(PartnerName name, Instant now) {
        return new Partner(PartnerId.of(null), name, PartnerStatus.ACTIVE, now, now);
    }

    public static Partner reconstitute(PartnerId id, PartnerName name, PartnerStatus status,
                                        Instant createdAt, Instant updatedAt) {
        return new Partner(id, name, status, createdAt, updatedAt);
    }

    public void suspend(Instant now) {
        if (this.status == PartnerStatus.SUSPENDED) {
            throw new PartnerAlreadySuspendedException();
        }
        this.status = PartnerStatus.SUSPENDED;
        this.updatedAt = now;
    }

    public void activate(Instant now) {
        if (this.status == PartnerStatus.ACTIVE) {
            throw new PartnerAlreadyActiveException();
        }
        this.status = PartnerStatus.ACTIVE;
        this.updatedAt = now;
    }

    public boolean isActive() {
        return this.status == PartnerStatus.ACTIVE;
    }

    public PartnerId id() { return id; }
    public PartnerName name() { return name; }
    public PartnerStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Partner p)) return false;
        return id != null && !id.isNew() && id.equals(p.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hashCode(id) : System.identityHashCode(this);
    }
}
