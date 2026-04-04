package com.ryuqq.otatoy.domain.pricing;

/**
 * 취소 정책 VO.
 * 무료 취소 여부, 환불 불가 여부, 무료 취소 기한(일)을 관리한다.
 * 무료 취소와 환불 불가는 동시에 설정할 수 없다.
 */
public record CancellationPolicy(
        boolean freeCancellation,
        boolean nonRefundable,
        int deadlineDays,
        String policyText
) {

    public CancellationPolicy {
        if (freeCancellation && nonRefundable) {
            throw new IllegalArgumentException("무료 취소와 환불 불가는 동시에 설정할 수 없습니다");
        }
        if (deadlineDays < 0) {
            throw new IllegalArgumentException("무료 취소 기한은 0 이상이어야 합니다");
        }
    }

    public static CancellationPolicy of(boolean freeCancellation, boolean nonRefundable,
                                          int deadlineDays, String policyText) {
        return new CancellationPolicy(freeCancellation, nonRefundable, deadlineDays, policyText);
    }

    public static CancellationPolicy defaultPolicy() {
        return new CancellationPolicy(false, false, 0, null);
    }
}
