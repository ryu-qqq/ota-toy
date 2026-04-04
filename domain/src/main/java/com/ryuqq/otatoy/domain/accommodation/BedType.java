package com.ryuqq.otatoy.domain.accommodation;

public record BedType(
        Long id,
        String code,
        String name
) {

    public static BedType of(String code, String name) {
        return new BedType(null, code, name);
    }

    public static BedType reconstitute(Long id, String code, String name) {
        return new BedType(id, code, name);
    }
}
