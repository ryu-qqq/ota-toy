package com.ryuqq.otatoy.domain.accommodation;

public record ViewType(
        Long id,
        String code,
        String name
) {

    public ViewType {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("전망 유형 코드는 필수입니다");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("전망 유형명은 필수입니다");
        }
    }

    public static ViewType of(String code, String name) {
        return new ViewType(null, code, name);
    }

    public static ViewType reconstitute(Long id, String code, String name) {
        return new ViewType(id, code, name);
    }
}
