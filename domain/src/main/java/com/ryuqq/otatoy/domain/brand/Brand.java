package com.ryuqq.otatoy.domain.brand;

import java.time.Instant;
import java.util.Objects;

/**
 * 숙박 브랜드를 나타내는 엔티티.
 * 호텔 체인이나 프랜차이즈 브랜드 정보를 관리한다.
 */
public class Brand {

    private final BrandId id;
    private BrandName name;
    private BrandNameKr nameKr;
    private LogoUrl logoUrl;
    private final Instant createdAt;
    private Instant updatedAt;

    private Brand(BrandId id, BrandName name, BrandNameKr nameKr, LogoUrl logoUrl,
                  Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.nameKr = nameKr;
        this.logoUrl = logoUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Brand forNew(BrandName name, BrandNameKr nameKr, LogoUrl logoUrl, Instant now) {
        return new Brand(null, name, nameKr, logoUrl, now, now);
    }

    public static Brand reconstitute(BrandId id, BrandName name, BrandNameKr nameKr, LogoUrl logoUrl,
                                      Instant createdAt, Instant updatedAt) {
        return new Brand(id, name, nameKr, logoUrl, createdAt, updatedAt);
    }

    public void rename(BrandName newName, Instant now) {
        this.name = newName;
        this.updatedAt = now;
    }

    public void updateLogoUrl(LogoUrl newLogoUrl, Instant now) {
        this.logoUrl = newLogoUrl;
        this.updatedAt = now;
    }

    public BrandId id() { return id; }
    public BrandName name() { return name; }
    public BrandNameKr nameKr() { return nameKr; }
    public LogoUrl logoUrl() { return logoUrl; }
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
