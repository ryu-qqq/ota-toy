package com.ryuqq.otatoy.domain.accommodation;

import java.time.Instant;
import java.util.Objects;

public class PropertyType {

    private final PropertyTypeId id;
    private final PropertyTypeCode code;
    private PropertyTypeName name;
    private PropertyTypeDescription description;
    private final Instant createdAt;
    private Instant updatedAt;

    private PropertyType(PropertyTypeId id, PropertyTypeCode code, PropertyTypeName name, PropertyTypeDescription description,
                         Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PropertyType forNew(PropertyTypeCode code, PropertyTypeName name, PropertyTypeDescription description, Instant now) {
        return new PropertyType(null, code, name, description, now, now);
    }

    public static PropertyType reconstitute(PropertyTypeId id, PropertyTypeCode code, PropertyTypeName name,
                                             PropertyTypeDescription description, Instant createdAt, Instant updatedAt) {
        return new PropertyType(id, code, name, description, createdAt, updatedAt);
    }

    public void updateInfo(PropertyTypeName name, PropertyTypeDescription description, Instant now) {
        this.name = name;
        this.description = description;
        this.updatedAt = now;
    }

    public PropertyTypeId id() { return id; }
    public PropertyTypeCode code() { return code; }
    public PropertyTypeName name() { return name; }
    public PropertyTypeDescription description() { return description; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

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
