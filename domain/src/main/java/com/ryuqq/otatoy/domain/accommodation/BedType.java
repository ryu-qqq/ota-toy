package com.ryuqq.otatoy.domain.accommodation;

public record BedType(
        Long id,
        String code,
        String name
) {

    public BedType {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("침대 유형 코드는 필수입니다");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("침대 유형명은 필수입니다");
        }
    }

    public static BedType of(String code, String name) {
        return new BedType(null, code, name);
    }

    public static BedType reconstitute(Long id, String code, String name) {
        return new BedType(id, code, name);
    }
}
