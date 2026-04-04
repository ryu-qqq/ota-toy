package com.ryuqq.otatoy.domain.accommodation;

import java.util.Objects;

public class Brand {

    private final Long id;
    private String name;
    private String nameKr;
    private String logoUrl;

    private Brand(Long id, String name, String nameKr, String logoUrl) {
        this.id = id;
        this.name = name;
        this.nameKr = nameKr;
        this.logoUrl = logoUrl;
    }

    public static Brand forNew(String name, String nameKr, String logoUrl) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("브랜드명은 필수입니다");
        }
        return new Brand(null, name, nameKr, logoUrl);
    }

    public static Brand reconstitute(Long id, String name, String nameKr, String logoUrl) {
        return new Brand(id, name, nameKr, logoUrl);
    }

    public Long id() { return id; }
    public String name() { return name; }
    public String nameKr() { return nameKr; }
    public String logoUrl() { return logoUrl; }

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
