package com.ryuqq.otatoy.domain.accommodation;

public record ViewType(
        Long id,
        String code,
        String name
) {

    public static ViewType of(String code, String name) {
        return new ViewType(null, code, name);
    }

    public static ViewType reconstitute(Long id, String code, String name) {
        return new ViewType(id, code, name);
    }
}
