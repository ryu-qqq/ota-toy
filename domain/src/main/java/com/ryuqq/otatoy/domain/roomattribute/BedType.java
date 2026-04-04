package com.ryuqq.otatoy.domain.roomattribute;

import java.time.Instant;
import java.util.Objects;

/**
 * 침대 유형을 나타내는 엔티티.
 * 싱글, 더블, 킹 등 침대 분류를 코드와 이름으로 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class BedType {

    private final BedTypeId id;
    private final BedTypeCode code;
    private final BedTypeName name;
    private final Instant createdAt;
    private Instant updatedAt;

    private BedType(BedTypeId id, BedTypeCode code, BedTypeName name,
                    Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static BedType forNew(BedTypeCode code, BedTypeName name, Instant now) {
        return new BedType(BedTypeId.of(null), code, name, now, now);
    }

    public static BedType reconstitute(BedTypeId id, BedTypeCode code, BedTypeName name,
                                        Instant createdAt, Instant updatedAt) {
        return new BedType(id, code, name, createdAt, updatedAt);
    }

    public BedTypeId id() { return id; }
    public BedTypeCode code() { return code; }
    public BedTypeName name() { return name; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

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
