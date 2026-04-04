package com.ryuqq.otatoy.domain.accommodation;

import java.util.Objects;

public class PropertyType {

    private final Long id;
    private final String code;
    private String name;
    private String description;

    private PropertyType(Long id, String code, String name, String description) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public static PropertyType forNew(String code, String name, String description) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("숙소 유형 코드는 필수입니다");
        }
        return new PropertyType(null, code, name, description);
    }

    public static PropertyType reconstitute(Long id, String code, String name, String description) {
        return new PropertyType(id, code, name, description);
    }

    public Long id() { return id; }
    public String code() { return code; }
    public String name() { return name; }
    public String description() { return description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyType p)) return false;
        return id != null && id.equals(p.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
