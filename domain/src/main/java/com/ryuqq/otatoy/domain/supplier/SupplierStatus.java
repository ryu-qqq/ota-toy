package com.ryuqq.otatoy.domain.supplier;

import java.util.Map;
import java.util.Set;

/**
 * 공급자 상태.
 * ACTIVE: 활성, SUSPENDED: 정지, TERMINATED: 해지.
 * 상태 전이 규칙을 포함한다.
 */
public enum SupplierStatus {

    ACTIVE("활성"),
    SUSPENDED("정지"),
    TERMINATED("해지");

    private static final Map<SupplierStatus, Set<SupplierStatus>> TRANSITIONS = Map.of(
            ACTIVE, Set.of(SUSPENDED, TERMINATED),
            SUSPENDED, Set.of(ACTIVE, TERMINATED)
    );

    private final String displayName;

    SupplierStatus(String displayName) {
        this.displayName = displayName;
    }

    public boolean canTransitTo(SupplierStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public SupplierStatus transitTo(SupplierStatus target) {
        if (!canTransitTo(target)) {
            if (this == TERMINATED) {
                throw new SupplierAlreadyTerminatedException();
            }
            if (this == target && this == SUSPENDED) {
                throw new SupplierAlreadySuspendedException();
            }
            throw new InvalidSupplierStateTransitionException();
        }
        return target;
    }

    public boolean isTerminal() {
        return !TRANSITIONS.containsKey(this);
    }

    public String displayName() {
        return displayName;
    }
}
