package com.ryuqq.otatoy.domain.roomattribute;

import java.util.Objects;

/**
 * 침대 유형을 나타내는 엔티티.
 * 싱글, 더블, 킹 등 침대 분류를 코드와 이름으로 관리한다.
 */
public class BedType {

    private final BedTypeId id;
    private final BedTypeCode code;
    private final BedTypeName name;

    private BedType(BedTypeId id, BedTypeCode code, BedTypeName name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public static BedType forNew(BedTypeCode code, BedTypeName name) {
        return new BedType(BedTypeId.of(null), code, name);
    }

    public static BedType reconstitute(BedTypeId id, BedTypeCode code, BedTypeName name) {
        return new BedType(id, code, name);
    }

    public BedTypeId id() { return id; }
    public BedTypeCode code() { return code; }
    public BedTypeName name() { return name; }

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
