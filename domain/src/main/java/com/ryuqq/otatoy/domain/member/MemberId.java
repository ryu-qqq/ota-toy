package com.ryuqq.otatoy.domain.member;

/**
 * 회원 식별자. null이면 아직 DB에 저장되지 않은 신규 회원.
 *
 * @author ryu-qqq
 * @since 2026-04-05
 */
public record MemberId(Long value) {

    public static MemberId of(Long value) {
        return new MemberId(value);
    }

    public static MemberId forNew() {
        return new MemberId(null);
    }

    public boolean isNew() {
        return value == null;
    }
}
