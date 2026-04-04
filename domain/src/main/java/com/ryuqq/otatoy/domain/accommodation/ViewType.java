package com.ryuqq.otatoy.domain.accommodation;

import java.util.Objects;

public class ViewType {

    private final ViewTypeId id;
    private final String code;
    private final String name;

    private ViewType(ViewTypeId id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public static ViewType forNew(String code, String name) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("전망 유형 코드는 필수입니다");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("전망 유형명은 필수입니다");
        }
        return new ViewType(ViewTypeId.of(null), code, name);
    }

    public static ViewType reconstitute(ViewTypeId id, String code, String name) {
        return new ViewType(id, code, name);
    }

    public ViewTypeId id() { return id; }
    public String code() { return code; }
    public String name() { return name; }

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
