package com.ryuqq.otatoy.domain.partner;

/**
 * 파트너 멤버 식별자. null이면 아직 DB에 저장되지 않은 신규 엔티티.
 *
 * @author ryu-qqq
 * @since 2026-04-04
 */
public record PartnerMemberId(Long value) {

    public static PartnerMemberId of(Long value) {
        return new PartnerMemberId(value);
    }

    public static PartnerMemberId forNew() { return new PartnerMemberId(null); }

    public boolean isNew() {
        return value == null;
    }
}
