package com.ryuqq.otatoy.domain.accommodation;

import java.util.Objects;

public class BedType {

    private final BedTypeId id;
    private final String code;
    private final String name;

    private BedType(BedTypeId id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public static BedType forNew(String code, String name) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("침대 유형 코드는 필수입니다");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("침대 유형명은 필수입니다");
        }
        return new BedType(BedTypeId.of(null), code, name);
    }

    public static BedType reconstitute(BedTypeId id, String code, String name) {
        return new BedType(id, code, name);
    }

    public BedTypeId id() { return id; }
    public String code() { return code; }
    public String name() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BedType b)) return false;
        return id != null && id.equals(b.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
