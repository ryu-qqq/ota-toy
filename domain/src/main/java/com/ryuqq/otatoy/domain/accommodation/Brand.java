package com.ryuqq.otatoy.domain.accommodation;

import java.time.Instant;
import java.util.Objects;

public class Brand {

    private final Long id;
    private String name;
    private String nameKr;
    private String logoUrl;
    private final Instant createdAt;

    private Brand(Long id, String name, String nameKr, String logoUrl, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.nameKr = nameKr;
        this.logoUrl = logoUrl;
        this.createdAt = createdAt;
    }

    public static Brand forNew(String name, String nameKr, String logoUrl, Instant now) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("브랜드명은 필수입니다");
        }
        return new Brand(null, name, nameKr, logoUrl, now);
    }

    public static Brand reconstitute(Long id, String name, String nameKr, String logoUrl, Instant createdAt) {
        return new Brand(id, name, nameKr, logoUrl, createdAt);
    }

    public Long id() { return id; }
    public String name() { return name; }
    public String nameKr() { return nameKr; }
    public String logoUrl() { return logoUrl; }
    public Instant createdAt() { return createdAt; }

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
