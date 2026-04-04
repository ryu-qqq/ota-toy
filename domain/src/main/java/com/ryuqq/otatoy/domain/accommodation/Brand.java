package com.ryuqq.otatoy.domain.accommodation;

import java.time.Instant;
import java.util.Objects;

public class Brand {

    private final BrandId id;
    private BrandName name;
    private String nameKr;
    private String logoUrl;
    private final Instant createdAt;
    private Instant updatedAt;

    private Brand(BrandId id, BrandName name, String nameKr, String logoUrl,
                  Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.nameKr = nameKr;
        this.logoUrl = logoUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Brand forNew(BrandName name, String nameKr, String logoUrl, Instant now) {
        return new Brand(null, name, nameKr, logoUrl, now, now);
    }

    public static Brand reconstitute(BrandId id, BrandName name, String nameKr, String logoUrl,
                                      Instant createdAt, Instant updatedAt) {
        return new Brand(id, name, nameKr, logoUrl, createdAt, updatedAt);
    }

    public void rename(BrandName newName, Instant now) {
        this.name = newName;
        this.updatedAt = now;
    }

    public void updateLogoUrl(String newLogoUrl, Instant now) {
        this.logoUrl = newLogoUrl;
        this.updatedAt = now;
    }

    public BrandId id() { return id; }
    public BrandName name() { return name; }
    public String nameKr() { return nameKr; }
    public String logoUrl() { return logoUrl; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Brand b)) return false;
        return id != null && id.equals(b.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
