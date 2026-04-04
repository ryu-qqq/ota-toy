package com.ryuqq.otatoy.domain.roomattribute;

import java.time.Instant;
import java.util.Objects;

/**
 * 전망 유형을 나타내는 엔티티.
 * 바다 전망, 산 전망 등 객실 전망 분류를 코드와 이름으로 관리한다.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public class ViewType {

    private final ViewTypeId id;
    private final ViewTypeCode code;
    private final ViewTypeName name;
    private final Instant createdAt;
    private Instant updatedAt;

    private ViewType(ViewTypeId id, ViewTypeCode code, ViewTypeName name,
                     Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ViewType forNew(ViewTypeCode code, ViewTypeName name, Instant now) {
        return new ViewType(ViewTypeId.of(null), code, name, now, now);
    }

    public static ViewType reconstitute(ViewTypeId id, ViewTypeCode code, ViewTypeName name,
                                         Instant createdAt, Instant updatedAt) {
        return new ViewType(id, code, name, createdAt, updatedAt);
    }

    public ViewTypeId id() { return id; }
    public ViewTypeCode code() { return code; }
    public ViewTypeName name() { return name; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ViewType v)) return false;
        return id != null && id.equals(v.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
