package com.ryuqq.otatoy.domain.accommodation;

import java.util.Objects;

public class ViewType {

    private final ViewTypeId id;
    private final ViewTypeCode code;
    private final ViewTypeName name;

    private ViewType(ViewTypeId id, ViewTypeCode code, ViewTypeName name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public static ViewType forNew(ViewTypeCode code, ViewTypeName name) {
        return new ViewType(ViewTypeId.of(null), code, name);
    }

    public static ViewType reconstitute(ViewTypeId id, ViewTypeCode code, ViewTypeName name) {
        return new ViewType(id, code, name);
    }

    public ViewTypeId id() { return id; }
    public ViewTypeCode code() { return code; }
    public ViewTypeName name() { return name; }

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
